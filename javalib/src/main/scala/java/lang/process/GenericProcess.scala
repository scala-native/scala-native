package java.lang.process

import scala.scalanative.meta.LinktimeInfo

import java.io.{FileDescriptor, InputStream, OutputStream}
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.function
import java.util.stream.Stream

private[process] abstract class GenericProcess() extends Process {
  protected val builder: ProcessBuilder
  protected def fdIn: FileDescriptor
  protected def fdOut: FileDescriptor
  protected def fdErr: FileDescriptor

  private val processInfo = GenericProcessInfo(builder)
  private lazy val handle = new GenericProcessHandle(this)

  private val outputStream =
    PipeIO[OutputStream](this, fdIn, builder.redirectInput())
  private val inputStream =
    PipeIO[PipeIO.Stream](this, fdOut, builder.redirectOutput())
  private val errorStream =
    PipeIO[PipeIO.Stream](this, fdErr, builder.redirectError())

  override def getInputStream(): InputStream = inputStream
  override def getErrorStream(): InputStream = errorStream
  override def getOutputStream(): OutputStream = outputStream

  override def exitValue(): Int = getCachedExitCode.getOrElse {
    throw new IllegalThreadStateException(
      s"Process ${pid()} has not exited yet"
    )
  }

  override def toHandle(): ProcessHandle = handle

  override def onExit(): CompletableFuture[Process] =
    onExitApply(_ => this: Process)

  override def info(): ProcessHandle.Info = processInfo

  def closeProcessStreams(): GenericProcess = {
    outputStream.close()
    inputStream.drain()
    errorStream.drain()
    this
  }

  protected def close(): Unit
  protected def getExitCodeImpl: Option[Int]

  private val completion = new CompletableFuture[java.lang.Integer]()
  onExitHandleSync((_, _) => closeProcessStreams())

  override final def isAlive(): Boolean = !hasExited

  final def checkIfExited(): Boolean =
    hasExited || checkAndSetExitCode() || hasExited

  final def hasExited: Boolean = completion.isDone()

  final def getCachedExitCode: Option[Int] = {
    if (!completion.isDone()) None
    else if (completion.isCompletedExceptionally()) Some(-1)
    else {
      val res = completion.getNow(null)
      Some(if (res == null) -1 else res.intValue())
    }
  }

  protected final def setCachedExitCode(value: Int): Boolean = {
    val ok = completion.complete(value)
    if (ok) close()
    ok
  }

  protected final def checkAndSetExitCode(): Boolean =
    synchronized(getExitCodeImpl.exists(setCachedExitCode))

  def onExitApply[A <: AnyRef](
      fn: function.Function[java.lang.Integer, A]
  ): CompletableFuture[A] =
    completion.thenApplyAsync(fn)

  def onExitHandleSync[A <: AnyRef](
      fn: function.BiFunction[java.lang.Integer, Throwable, A]
  ): CompletableFuture[A] =
    completion.handle(fn)

}

// Represents ProcessHandle for process started by Scala Native runtime
// Cannot be used with processes started by other programs
private[process] class GenericProcessHandle(val process: GenericProcess)
    extends ProcessHandle {
  private val createdAt = System.nanoTime()

  override def parent(): Optional[ProcessHandle] = Optional.empty()

  // We don't track transitive children
  override def children(): Stream[ProcessHandle] = Stream.empty()
  override def descendants(): Stream[ProcessHandle] = Stream.empty()

  override def destroy(): Boolean = {
    if (isAlive()) process.destroy()
    true // all implementations are always successful
  }
  override def destroyForcibly(): Boolean = {
    if (isAlive()) process.destroyForcibly()
    true // all implementations are always successful
  }
  override def supportsNormalTermination(): Boolean =
    process.supportsNormalTermination()

  override def onExit(): CompletableFuture[ProcessHandle] =
    process.onExitApply(_ => this: ProcessHandle)

  override def pid(): scala.Long = process.pid()
  override def isAlive(): Boolean = process.isAlive()
  override def info(): ProcessHandle.Info = process.info()

  override def compareTo(other: ProcessHandle): Int = other match {
    case handle: GenericProcessHandle =>
      this.process.pid().compareTo(handle.process.pid()) match {
        case 0     => this.createdAt.compareTo(handle.createdAt)
        case value => value
      }
    case _ => -1
  }
  override def equals(that: Any): Boolean = that match {
    case other: ProcessHandle => this.compareTo(other) == 0
    case _                    => false
  }
  override def hashCode(): Int =
    ((31 * this.pid().##) * 31) + this.createdAt.##
  override def toString(): String = process.pid().toString() // JVM compliance
}

private[lang] object GenericProcess {

  def apply(pb: ProcessBuilder): GenericProcess = {
    val process =
      if (LinktimeInfo.isWindows) WindowsProcess(pb) else UnixProcess(pb)
    if (LinktimeInfo.isMultithreadingEnabled)
      GenericProcessWatcher.watchForTermination(process)
    process
  }

}
