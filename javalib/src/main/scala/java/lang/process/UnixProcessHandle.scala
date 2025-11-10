package java.lang.process

import scala.scalanative.libc.{signal => csig}
import scala.scalanative.posix.{signal => psig}
import scala.scalanative.unsafe.CInt

private[process] abstract class UnixProcessHandle extends GenericProcessHandle {
  protected val _pid: CInt

  override final def pid(): Long = _pid.toLong
  override final def supportsNormalTermination(): Boolean = true

  override protected final def destroyImpl(force: Boolean): Boolean =
    psig.kill(_pid, if (force) psig.SIGKILL else csig.SIGTERM) == 0

  override protected def getExitCodeImpl: Option[Int] =
    UnixProcess.waitpidNoECHILD(_pid) match {
      case Right((_, ec)) => Some(ec)
      case _              => None
    }
}
