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

    override def createSingle(
        handle: GenericProcessHandle
    )(implicit pr: ProcessRegistry): ProcessExitChecker =
      new Single(handle.asInstanceOf[WindowsProcessHandle])

    override def createMulti(implicit
        pr: ProcessRegistry
    ): ProcessExitChecker.Multi = new Multi

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

    private class Multi(implicit pr: ProcessRegistry)
        extends ProcessExitChecker.Multi {
      import ProcessMonitorApi._

      private val iocp: Handle = ProcessMonitorQueueCreate()

      /** If the process is running, register it and return true.
       *
       *  If the process isn't running, reap the process and return false.
       *
       *  Make sure to add it to the process registry before checker can reap
       *  this process and call `complete` on the registry.
       */
      override def addOrReap(handle: GenericProcessHandle): Boolean = {
        val wh = handle.asInstanceOf[WindowsProcessHandle]
        val ok = ProcessMonitorQueueRegister(
          iocp = iocp,
          process = wh.handle,
          pid = wh._pid
        )
        if (!ok) handle.checkIfExited()
        ok
      }

      override def close(): Unit = CloseHandle(iocp)

      override def waitAndReapSome(
          timeout: Long,
          unitOpt: Option[TimeUnit]
      ): Boolean = {
        val timeoutMillis = unitOpt
          .fold(Constants.Infinite)(_.toMillis(timeout).toUInt)
        val pid =
          ProcessMonitorQueuePull(iocp = iocp, timeoutMillis = timeoutMillis)
        pid != -1 && { pr.completeWith(pid.toLong)(-1); true }
      }
    }

  }

}
