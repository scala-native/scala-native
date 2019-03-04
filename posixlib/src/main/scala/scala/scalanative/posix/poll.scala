package scala.scalanative
package posix

import scalanative.native._

object pollNfds {

  type nfds_t = CUnsignedLongInt

  // Keep the breakage in the nfds_t abstraction confined to here.
  // The following code will work on both 64 and 32 bit machines.
  //
  // posix FOPEN_MAX is currently defined in scalanative wrap.c
  // as an unsigned int. So any scala Long with bits greater than #31
  // set are going to fail anyway. Some/most systems will probably
  // exceed FOPEN_MAX earlier than that.
  //
  // Revisit & simplify when better 32/64 bit support arrives.

  @inline
  def toNfsd_t(in: Int): nfds_t = in.toUInt

  def toNfsd_t(in: ULong): nfds_t = {
    val uIntMax = UInt.MaxValue
    if (in > uIntMax) {
      throw new Exception(s"pollNfds: too many nfds: ${in}  > ${uIntMax}")
    } else {
      // may still fail later if greater than FOPEN_MAX. Let code
      // more closely concerned test for that.
      // On 64 bit machines UInt will get silently promoted match the
      // ULong nfsd_t on that machine, so types work out.
      in.toUInt // truncation is now harmless.
    }
  }
}

@extern
object poll {

  type pollEvent_t = CShort

  type struct_pollfd = CStruct3[CInt, // file descriptor
                                pollEvent_t, // requested events
                                pollEvent_t] // returned events

  import pollNfds.nfds_t

  def poll(fds: Ptr[struct_pollfd], nfds: nfds_t, timeout: CInt): CInt = extern

}

object pollEvents {

  final val POLLIN  = 0x001 // Data ready to be read
  final val POLLPRI = 0x002 // Urgent data ready to be read
  final val POLLOUT = 0x004 // Writing now will not block

  // XOpen events
  final val POLLRDNORM = 0x040 // Normal data may be read
  final val POLLRDBAND = 0x080 // Priority data may be read
  final val POLLWRNORM = 0x100 // Writing now will not block
  final val POLLWRBAND = 0x200 // Priority data may be written

  // Always checked in revents
  final val POLLERR  = 0x008 // Error condition
  final val POLLHUP  = 0x010 // Hung up
  final val POLLNVAL = 0x020 // Invalid polling request
}

object pollOps {
  import poll._

  implicit class pollOps(val ptr: Ptr[struct_pollfd]) extends AnyVal {
    def fd: CInt             = !(ptr._1)
    def events: pollEvent_t  = !(ptr._2)
    def revents: pollEvent_t = !(ptr._3)

    def fd_=(v: CInt): Unit             = !ptr._1 = v
    def events_=(v: pollEvent_t): Unit  = !ptr._2 = v
    def revents_=(v: pollEvent_t): Unit = !ptr._3 = v
  }
}
