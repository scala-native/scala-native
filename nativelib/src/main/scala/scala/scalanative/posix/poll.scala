package scala.scalanative.posix

import scalanative.native._

@extern
object poll {

  type pollfd = CStruct3[CInt,   // fd
                         CShort, // events
                         CShort] // revents
  
  type nfds_t = UInt

  @name("scalanative_POLLIN")
  def POLLIN: CInt = extern

  @name("scalanative_POLLRDNORM")
  def POLLRDNORM: CInt = extern

  @name("scalanative_POLLRDBAND")
  def POLLRDBAND: CInt = extern

  @name("scalanative_POLLPRI")
  def POLLPRI: CInt = extern

  @name("scalanative_POLLOUT")
  def POLLOUT: CInt = extern

  @name("scalanative_POLLWRNORM")
  def POLLWRNORM: CInt = extern

  @name("scalanative_POLLWRBAND")
  def POLLWRBAND: CInt = extern

  @name("scalanative_POLLERR")
  def POLLERR: CInt = extern

  @name("scalanative_POLLHUP")
  def POLLHUP: CInt = extern

  @name("scalanative_POLLNVAL")
  def POLLNVAL: CInt = extern

  def poll(fds: Ptr[pollfd], nfds: nfds_t, timeout: CInt): CInt = extern

}

object pollOps {
  import poll._

  implicit class pollfdOps(val ptr: Ptr[pollfd]) extends AnyVal {
    def fd: CInt = !ptr._1
    def events: CShort = !ptr._2
    def revents: CShort = !ptr._3

    def fd_=(v: CInt): Unit = !ptr._1 = v
    def events_=(v: CShort): Unit = !ptr._2 = v
    def revents_=(v: CShort): Unit = !ptr._3 = v
  }
}
