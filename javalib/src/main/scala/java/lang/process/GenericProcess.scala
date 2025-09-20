package java.lang.process

import scala.scalanative.unsafe._
import scala.scalanative.meta.LinktimeInfo

import java.io.{FileDescriptor, InputStream, OutputStream}
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.stream.Stream

private[process] abstract class GenericProcess() extends Process {
  protected val builder: ProcessBuilder
  protected def fdIn: FileDescriptor
  protected def fdOut: FileDescriptor
  protected def fdErr: FileDescriptor

  private val processInfo = GenericProcessInfo(builder)
  private lazy val handle = new GenericProcessHandle(this)

  def checkResult(): CInt

  private val outputStream =
    PipeIO[OutputStream](this, fdIn, builder.redirectInput())
  private val inputStream =
    PipeIO[PipeIO.Stream](this, fdOut, builder.redirectOutput())
  private val errorStream =
    PipeIO[PipeIO.Stream](this, fdErr, builder.redirectError())

  override def getInputStream(): InputStream = inputStream
  override def getErrorStream(): InputStream = errorStream
  override def getOutputStream(): OutputStream = outputStream

  override def toHandle(): ProcessHandle = handle

  override def onExit(): CompletableFuture[Process] =
    toHandle().onExit().thenApply(_ => this)

  override def info(): ProcessHandle.Info = processInfo

  def closeProcessStreams(): GenericProcess = {
    outputStream.close()
    inputStream.drain()
    errorStream.drain()
    this
  }

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

  override def onExit(): CompletableFuture[ProcessHandle] = {
    val completion = new CompletableFuture[ProcessHandle]()
    if (LinktimeInfo.isMultithreadingEnabled)
      GenericProcessWatcher.watchForTermination(completion, this)
    completion
  }

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

  def apply(pb: ProcessBuilder): GenericProcess =
    if (LinktimeInfo.isWindows) WindowsProcess(pb) else UnixProcess(pb)

}
