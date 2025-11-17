package java.lang.process

import java.nio.file.WindowsException
import java.util.concurrent.TimeUnit

// Required only for cross-compilation with Scala 2
import scala.language.existentials

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows._

import HandleApi._

private[process] class WindowsProcessHandle(
    _pid: DWord,
    handle: Handle,
    override val builder: ProcessBuilder
) extends GenericProcessHandle {
  override final def pid(): Long = _pid.toLong

  override def supportsNormalTermination(): Boolean = false

  override protected def destroyImpl(force: Boolean): Boolean =
    ProcessThreadsApi.TerminateProcess(handle, 1.toUInt)

  override protected def close(): Unit = CloseHandle(handle)

  override protected def waitForImpl(): Boolean =
    osWaitForImpl(Constants.Infinite)

  override protected def waitForImpl(timeout: Long, unit: TimeUnit): Boolean =
    osWaitForImpl(unit.toMillis(timeout).toUInt)

  private def osWaitForImpl(timeoutMillis: DWord): Boolean =
    SynchApi.WaitForSingleObject(handle, timeoutMillis) match {
      case SynchApiExt.WAIT_TIMEOUT => false
      case SynchApiExt.WAIT_FAILED  =>
        if (!hasExited)
          throw WindowsException("Failed to wait on process handle")
        true // someone may have closed the handle
      case _ => checkAndSetExitCode(); true
    }

  override protected def getExitCodeImpl: Option[Int] = {
    val exitCode: Ptr[DWord] = stackalloc[DWord]()
    if (ProcessThreadsApi.GetExitCodeProcess(handle, exitCode)) {
      val code = !exitCode
      if (code != ProcessThreadsApiExt.STILL_ACTIVE) Some(code.toInt) else None
    } else None
  }

}
