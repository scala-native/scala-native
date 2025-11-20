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

  object ProcessExitCheckerFactory extends ProcessExitChecker.MultiFactory {

    override def createSingle(processId: ObjectHandle)(implicit
        pr: ProcessRegistry
    ): ProcessExitChecker =
      new Single(processId.asHandle)

    override def createMulti(implicit
        pr: ProcessRegistry
    ): ProcessExitChecker.Multi = new Multi

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
        val pid = ProcessMonitorQueuePull(
          iocp = iocp,
          timeoutMillis =
            unitOpt.fold(Constants.Infinite)(_.toMillis(timeout).toUInt)
        ).toInt
        pid != -1 && { pr.completeWith(pid)(-1); true }
      }
    }

  }

}
