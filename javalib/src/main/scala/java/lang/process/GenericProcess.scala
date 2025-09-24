package java.lang.process

import scala.scalanative.meta.LinktimeInfo

import java.io.{FileDescriptor, InputStream, OutputStream}
import java.util.Optional
import java.util.concurrent.{CompletableFuture, TimeUnit}
import java.util.function
import java.util.stream.Stream

private[process] abstract class GenericProcess(val handle: GenericProcessHandle)
    extends Process {
  protected def fdIn: FileDescriptor
  protected def fdOut: FileDescriptor
  protected def fdErr: FileDescriptor

  handle.onExitHandleSync((_, _) => closeProcessStreams())

  private val outputStream =
    PipeIO[OutputStream](handle, fdIn, handle.builder.redirectInput())
  private val inputStream =
    PipeIO[PipeIO.Stream](handle, fdOut, handle.builder.redirectOutput())
  private val errorStream =
    PipeIO[PipeIO.Stream](handle, fdErr, handle.builder.redirectError())

  override def getInputStream(): InputStream = inputStream
  override def getErrorStream(): InputStream = errorStream
  override def getOutputStream(): OutputStream = outputStream

  override def exitValue(): Int = handle.getCachedExitCode.getOrElse {
    throw new IllegalThreadStateException(
      s"Process ${pid()} has not exited yet"
    )
  }

  override def toHandle(): ProcessHandle = handle

  override def onExit(): CompletableFuture[Process] =
    handle.onExitApply(_ => this: Process)

  override def info(): ProcessHandle.Info = handle.info()

  def closeProcessStreams(): GenericProcess = {
    outputStream.close()
    inputStream.drain()
    errorStream.drain()
    this
  }

  override final def pid(): Long = handle.pid()
  override final def children(): Stream[ProcessHandle] = handle.children()
  override final def descendants(): Stream[ProcessHandle] = handle.descendants()

  override final def isAlive(): Boolean = handle.isAlive()
  override final def supportsNormalTermination(): Boolean =
    handle.supportsNormalTermination()

  override final def destroy(): Unit = handle.destroy()
  override final def destroyForcibly(): Process = {
    handle.destroyForcibly()
    this
  }

  override final def waitFor(): Int = {
    handle.waitFor()
    handle.getCachedExitCode.getOrElse(-1)
  }

  override final def waitFor(timeout: scala.Long, unit: TimeUnit): Boolean =
    handle.waitFor(timeout, unit)

  override def equals(that: Any): Boolean = that match {
    case other: GenericProcess => handle.compareTo(other.handle) == 0
    case _                     => false
  }
  override def hashCode(): Int = handle.hashCode()
  override def toString: String = handle.toString

}

// Represents ProcessHandle for process started by Scala Native runtime
// Cannot be used with processes started by other programs
private[process] abstract class GenericProcessHandle extends ProcessHandle {
  protected def close(): Unit
  protected def getExitCodeImpl: Option[Int]
  protected def destroyImpl(force: Boolean): Boolean
  protected def waitForImpl(): Boolean
  protected def waitForImpl(timeout: scala.Long, unit: TimeUnit): Boolean

  val builder: ProcessBuilder

  private val processInfo: GenericProcessInfo = GenericProcessInfo(builder)
  private val completion = new CompletableFuture[java.lang.Integer]()

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

  override def parent(): Optional[ProcessHandle] = Optional.empty()

  // We don't track transitive children
  override def children(): Stream[ProcessHandle] = Stream.empty()
  override def descendants(): Stream[ProcessHandle] = Stream.empty()

  override final def destroy(): Boolean = destroy(force = false)
  override final def destroyForcibly(): Boolean = destroy(force = true)
  @inline private def destroy(force: Boolean) =
    hasExited || destroyImpl(force = force)

  private def waitForWith(check: => Boolean) = hasExited || check && hasExited
  def waitFor(): Boolean = waitForWith(synchronized(waitForImpl()))
  def waitFor(timeout: scala.Long, unit: TimeUnit): Boolean =
    waitForWith(timeout > 0L && synchronized(waitForImpl(timeout, unit)))

  override def onExit(): CompletableFuture[ProcessHandle] =
    onExitApply(_ => this: ProcessHandle)

  override def info(): ProcessHandle.Info = processInfo

  override def compareTo(other: ProcessHandle): Int = other match {
    case other: GenericProcessHandle =>
      val res = pid().compareTo(other.pid())
      if (res != 0) res
      else processInfo.createdAt.compareTo(other.processInfo.createdAt)
    case _ => -1
  }
  override def equals(that: Any): Boolean = that match {
    case other: ProcessHandle => this.compareTo(other) == 0
    case _                    => false
  }
  override def hashCode(): Int =
    ((31 * this.pid().##) * 31) + processInfo.createdAt.##
  override def toString: String =
    s"Process[pid=${pid()}, exitValue=${getCachedExitCode.getOrElse("\"not exited\"")}"
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
