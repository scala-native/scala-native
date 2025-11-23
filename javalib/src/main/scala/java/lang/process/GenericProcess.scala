package java.lang.process

import java.io.{FileDescriptor, InputStream, OutputStream}
import java.util.concurrent.{CompletableFuture, TimeUnit}
import java.util.stream.Stream

import scala.scalanative.meta.LinktimeInfo

private[process] abstract class GenericProcess(val handle: GenericProcessHandle)
    extends Process {
  protected def fdIn: FileDescriptor
  protected def fdOut: FileDescriptor
  protected def fdErr: FileDescriptor

  private val outputStream =
    PipeIO[OutputStream](fdIn, handle.builder.redirectInput())
  private val inputStream =
    PipeIO[PipeIO.Stream](fdOut, handle.builder.redirectOutput())
  private val errorStream =
    PipeIO[PipeIO.Stream](fdErr, handle.builder.redirectError())

  handle.onExitHandle((_, _) => { outputStream.close(); null })
  handle.onExitHandle((_, _) => { inputStream.drain(); null })
  handle.onExitHandle((_, _) => { errorStream.drain(); null })

  GenericProcessWatcher.watchForTermination(handle)

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

private[lang] object GenericProcess {

  def apply(pb: ProcessBuilder): GenericProcess = {
    if (LinktimeInfo.isWindows) WindowsProcessFactory(pb)
    else UnixProcessFactory(pb)
  }

}
