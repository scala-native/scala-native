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
    private val _pid: DWord,
    private val handle: Handle,
    override val builder: ProcessBuilder
) extends GenericProcessHandle {
  override final def pid(): Long = _pid.toLong

  override def supportsNormalTermination(): Boolean = false

  override protected def destroyImpl(force: Boolean): Boolean =
    ProcessThreadsApi.TerminateProcess(handle, 1.toUInt)

  override protected def close(): Unit = CloseHandle(handle)

  private val exitChecker = {
    implicit val pr: ProcessRegistry = new ProcessRegistry {
      override def completeWith(pid: Long)(ec: Int): Unit =
        setCachedExitCode(ec)
    }
    WindowsProcessHandle.ProcessExitCheckerFactory.createSingle(this)
  }

  override protected def waitForImpl(): Boolean =
    exitChecker.waitAndReapSome(0, None)

  override protected def waitForImpl(timeout: Long, unit: TimeUnit): Boolean =
    exitChecker.waitAndReapSome(timeout, Some(unit))

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

    override def createSingle(
        handle: GenericProcessHandle
    )(implicit pr: ProcessRegistry): ProcessExitChecker =
      new Single(handle.asInstanceOf[WindowsProcessHandle])

    override def createMulti(implicit
        pr: ProcessRegistry
    ): ProcessExitChecker.Multi = throw new UnsupportedOperationException()

    private class Single(handle: WindowsProcessHandle)
        extends ProcessExitChecker {
      override def close(): Unit = {}

      override def waitAndReapSome(
          timeout: Long,
          unitOpt: Option[TimeUnit]
      ): Boolean = {
        val timeoutMillis = unitOpt
          .fold(Constants.Infinite)(_.toMillis(timeout).toUInt)
        SynchApi.WaitForSingleObject(handle.handle, timeoutMillis) match {
          case SynchApiExt.WAIT_TIMEOUT => false
          case SynchApiExt.WAIT_FAILED  =>
            if (!handle.hasExited)
              throw WindowsException("Failed to wait on process handle")
            true // someone may have closed the handle
          case _ => handle.checkIfExited()
        }
      }
    }

  }

}
