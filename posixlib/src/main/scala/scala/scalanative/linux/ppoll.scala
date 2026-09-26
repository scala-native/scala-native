package scala.scalanative
package linux

import posix._
import unsafe._

@extern
@define("__SCALANATIVE_POSIX_PPOLL")
object ppoll {

  @blocking def ppoll(
      fds: Ptr[poll.struct_pollfd],
      nfds: poll.nfds_t,
      tmo_p: Ptr[time.timespec],
      sigmask: Ptr[signal.sigset_t]
  ): CInt = extern

}
