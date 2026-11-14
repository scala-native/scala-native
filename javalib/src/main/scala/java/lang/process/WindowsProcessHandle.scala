package java.lang.process

import java.nio.file.WindowsException
import java.util.concurrent.TimeUnit

import scala.annotation.tailrec
// Required only for cross-compilation with Scala 2
import scala.language.existentials

import scala.scalanative.javalib.io.ObjectHandle
import scala.scalanative.meta.LinktimeInfo
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
      ): Boolean = unitOpt match {
        case None       => waitUntil(None)
        case Some(unit) =>
          waitUntil(
            Some(System.nanoTime() + Math.max(0L, unit.toNanos(timeout)))
          )
      }

      /* Constants.Infinite (0xFFFFFFFF) must never be passed for finite waits.
       * With multithreading, wait in short slices to notice interrupts.
       */
      private final val MaxWaitMillis =
        if (LinktimeInfo.isMultithreadingEnabled) 100L else 0xfffffffeL

      @tailrec
      private def waitUntil(deadline: Option[Long]): Boolean = {
        val remainingMillis = deadline.fold(Long.MaxValue) { deadline =>
          val remainingNanos = deadline - System.nanoTime()
          // round up, so that a sub-millisecond remainder still waits
          if (remainingNanos <= 0L) 0L
          else (remainingNanos - 1L) / 1000000L + 1L
        }
        val capped = remainingMillis > MaxWaitMillis
        val millis = Math.min(remainingMillis, MaxWaitMillis)
        waitOnce(millis.toUInt) match {
          case Some(res)      => res
          case None if capped =>
            if (Thread.interrupted()) throw new InterruptedException()
            waitUntil(deadline)
          case None => false
        }
      }

      // None on timeout
      private def waitOnce(millis: UInt): Option[Boolean] =
        SynchApi.WaitForSingleObject(handle, millis) match {
          case SynchApiExt.WAIT_TIMEOUT => None
          case SynchApiExt.WAIT_FAILED  =>
            if (!checkIfExited())
              throw WindowsException("Failed to wait on process handle")
            Some(true) // someone may have closed the handle
          case _ => Some(checkIfExited())
        }
    }

  }

}
