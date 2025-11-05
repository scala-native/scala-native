package scala.scalanative
package posix

import unsafe._

@extern
@define("__SCALANATIVE_POSIX_POLL")
object poll {

  // See Usage note below. Valid values capped by FOPEN_MAX in underlying OS.

  type nfds_t = CUnsignedLongInt

  type pollEvent_t = CShort

  type struct_pollfd = CStruct3[
    CInt, // file descriptor
    pollEvent_t, // requested events
    pollEvent_t // returned events
  ]

  @blocking
  def poll(fds: Ptr[struct_pollfd], nfds: nfds_t, timeout: CInt): CInt = extern

  // TL;DR
  //
  //   Use as: poll(fds, fds.size.toUInt, timeout)
  //   and do not worry about the UInt to nfsd_t conversion. Act as though
  //   there is an implicit conversion.
  //
  // Usage note, bridging the C and ScalaNative universes:
  //
  //   Long comments are anathema but someone trying to trace the
  //   interacting constraints may appreciate this information.
  //
  //   A common use case, which ought to be easy to use, is to declare
  //   the first argument of the poll method as an array, fds, and the
  //   second argument as fds.size.
  //
  //   OpenGroup (posix) defines nfsd_t as an unsigned integral type but
  //   neither defines nor constrains the type before that.  Linux
  //   nfsd_t is defined on some systems as "unsigned long int". Other
  //   systems define it with fewer bytes.  SN nfsd_t above is defined
  //   to allow the maximum with seen in the wild.
  //
  //   Although nfsd_t appears to allow very large numbers of nfsd elements,
  //   it is capped to a much lower value. Posix FOPEN_MAX is currently
  //   defined in nativelib/src/main/resources scalanative wrap.c
  //   as an 'unsigned int'. Operating system implementations almost always
  //   cap the value to a much lower value the maximum unsigned int.
  //
  //   Array.size returns a signed int. The convention in SN is that all
  //   signed to unsigned conversions must be explicit The .toUInt method
  //   makes the most sense, since any bits above #31 in a ULong will
  //   almost certainly be beyond FOPEN_MAX on curent & foreseeable systems.
  //
  //   The end programmer almost certainly wants to use the fds.size.toUInt
  //   idiom.
  //
  //   Behind the curtain the type abstraction works because a UInt can be
  //   promoted, when necessary, to a ULong, which is also a CUnsignedLongInt,
  //   which is an nfds_t. In effect, one has an implicit conversion from
  //   UInt to nfsd_t.
  //
  //   The C and SN world are in harmony:

}

@extern
@define("__SCALANATIVE_POSIX_POLL")
object pollEvents {

  @name("scalanative_pollin")
  def POLLIN: CInt = extern // Data ready to be read

  @name("scalanative_pollpri")
  def POLLPRI: CInt = extern // Urgent data ready to be read

  @name("scalanative_pollout")
  def POLLOUT: CInt = extern // Writing now will not block

  // XOpen events

  @name("scalanative_pollrdnorm")
  def POLLRDNORM: CInt = extern // Normal data may be read

  @name("scalanative_pollrdband")
  def POLLRDBAND: CInt = extern // Priority data may be read

  @name("scalanative_pollwrnorm")
  def POLLWRNORM: CInt = extern // Writing now will not block

  @name("scalanative_pollwrband")
  def POLLWRBAND: CInt = extern // Priority data may be written

  // Always checked in revents

  @name("scalanative_pollerr")
  def POLLERR: CInt = extern // Error condition

  @name("scalanative_pollhup")
  def POLLHUP: CInt = extern // Hung up

  @name("scalanative_pollnval")
  def POLLNVAL: CInt = extern // Invalid polling request

}

object pollOps {
  import poll._

  // scalafmt: { align.preset = more }

  implicit class pollOps(val ptr: Ptr[struct_pollfd]) extends AnyVal {
    def fd: CInt             = ptr._1
    def events: pollEvent_t  = ptr._2
    def revents: pollEvent_t = ptr._3

    def fd_=(v: CInt): Unit             = ptr._1 = v
    def events_=(v: pollEvent_t): Unit  = ptr._2 = v
    def revents_=(v: pollEvent_t): Unit = ptr._3 = v
  }

  // scalafmt: { align.preset = none }

}
