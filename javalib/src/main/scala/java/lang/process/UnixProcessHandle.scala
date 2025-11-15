package java.lang.process

import java.util.concurrent.{TimeUnit, TimeoutException}

import scala.scalanative.libc.{signal => csig}
import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.posix.{signal => psig}
import scala.scalanative.unsafe.CInt

private[process] class UnixProcessHandle(_pid: CInt)(
    override val builder: ProcessBuilder
) extends GenericProcessHandle {
  private val exitChecker: ProcessExitChecker = {
    ProcessExitChecker.factoryOpt
      .filterNot(
        _ == ProcessExitCheckerWaitpid && LinktimeInfo.isMultithreadingEnabled
      )
      .map { factory =>
        implicit val processRegistry: ProcessRegistry = new ProcessRegistry {
          override def completeWith(pid: Long)(ec: Int): Unit =
            setCachedExitCode(ec)
        }
        factory.createSingle(_pid)
      }
  }.getOrElse(ProcessExitCheckerCompletion)

  override final def pid(): Long = _pid.toLong
  override final def supportsNormalTermination(): Boolean = true

  override protected final def destroyImpl(force: Boolean): Boolean =
    psig.kill(_pid, if (force) psig.SIGKILL else csig.SIGTERM) == 0

  override protected def getExitCodeImpl: Option[Int] =
    UnixProcess.waitpidNoECHILD(_pid) match {
      case Right((_, ec)) => Some(ec)
      case _              => None
    }

  override protected def waitForImpl(): Boolean =
    exitChecker.waitAndReapSome(0, None)

  override protected def waitForImpl(timeout: Long, unit: TimeUnit): Boolean =
    exitChecker.waitAndReapSome(timeout, Some(unit))

  override protected final def close(): Unit =
    exitChecker.close()

}
