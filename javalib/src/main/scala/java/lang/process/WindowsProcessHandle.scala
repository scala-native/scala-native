package java.lang.process

import java.nio.file.WindowsException
import java.util.concurrent.TimeUnit

// Required only for cross-compilation with Scala 2
import scala.language.existentials

import scala.scalanative.javalib.io.ObjectHandle
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows._

import HandleApi._

private[process] class WindowsProcessHandle(
    private val _pid: DWord,
    private val handle: Handle,
    override val builder: ProcessBuilder
) extends GenericProcessHandle(ObjectHandle(handle)) {
  override final def pid(): Long = _pid.toLong

  override def supportsNormalTermination(): Boolean = false

  override protected def destroyImpl(force: Boolean): Boolean =
    ProcessThreadsApi.TerminateProcess(handle, 1.toUInt)

  override protected def close(): Unit = {
    CloseHandle(handle)
    super.close()
  }

  override protected def getExitCodeImpl: Option[Int] = {
    val exitCode: Ptr[DWord] = stackalloc[DWord]()
    if (ProcessThreadsApi.GetExitCodeProcess(handle, exitCode)) {
      val code = !exitCode
      if (code != ProcessThreadsApiExt.STILL_ACTIVE) Some(code.toInt) else None
    } else None
  }

}

object WindowsProcessHandle {

  object ProcessExitCheckerFactory extends ProcessExitChecker.Factory {

    override def createSingle(processId: ObjectHandle)(implicit
        pr: ProcessRegistry
    ): ProcessExitChecker =
      new Single(processId.asHandle)

    private class Single(handle: Handle)(implicit pr: ProcessRegistry)
        extends ProcessExitChecker {
      override def close(): Unit = {}

      @inline
      private def checkIfExited(): Boolean =
        pr.completeWith(0)(-1) // pid doesn't matter here

      override def waitAndReapSome(
          timeout: Long,
          unitOpt: Option[TimeUnit]
      ): Boolean = {
        val timeoutMillis = unitOpt
          .fold(Constants.Infinite)(_.toMillis(timeout).toUInt)
        SynchApi.WaitForSingleObject(handle, timeoutMillis) match {
          case SynchApiExt.WAIT_TIMEOUT => false
          case SynchApiExt.WAIT_FAILED  =>
            if (!checkIfExited())
              throw WindowsException("Failed to wait on process handle")
            true // someone may have closed the handle
          case _ => checkIfExited()
        }
      }
    }

  }

}
