package java.lang.process

import java.util.concurrent.{TimeUnit, TimeoutException}

import scala.scalanative.libc.{signal => csig}
import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.posix.{signal => psig}
import scala.scalanative.unsafe.CInt

private[process] class UnixProcessHandle(_pid: CInt)(
    override val builder: ProcessBuilder
) extends GenericProcessHandle {
  private val exitChecker: Option[ProcessExitChecker] =
    if (LinktimeInfo.isMultithreadingEnabled) None
    else
      ProcessExitChecker.factoryOpt.flatMap { factory =>
        implicit val processRegistry: ProcessRegistry = new ProcessRegistry {
          override def completeWith(pid: Long)(ec: Int): Unit =
            setCachedExitCode(ec)
        }
        val res = factory.createSingle(_pid)
        if (res.isEmpty) checkIfExited()
        res
      }

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
    exitChecker.fold { completion.get(); true }(_.waitAndReapSome(0, None))

  override protected def waitForImpl(timeout: Long, unit: TimeUnit): Boolean =
    exitChecker.fold {
      try { completion.get(timeout, unit); true }
      catch { case _: TimeoutException => false }
    }(_.waitAndReapSome(timeout, Some(unit)))

  override protected final def close(): Unit =
    exitChecker.foreach(_.close())

}
