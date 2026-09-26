package scala.scalanative
package linux

import unsafe._
import unsigned._

@extern
@define("__SCALANATIVE_POSIX_EVENTFD")
object eventfd {

  @name("scalanative_efd_cloexec")
  def EFD_CLOEXEC: CInt = extern
  @name("scalanative_efd_nonblock")
  def EFD_NONBLOCK: CInt = extern
  @name("scalanative_efd_semaphore")
  def EFD_SEMAPHORE: CInt = extern

  def eventfd(initval: UInt, flags: CInt): CInt = extern

}
