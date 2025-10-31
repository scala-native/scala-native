package java.lang.process

import scala.scalanative.libc.stdint
import scala.scalanative.posix.time.timespec
//
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

object BsdOsSpecific {
  // Beware: FreeBSD and other BSD layouts have not been tested.

  /* This file is intended for use by UnixProcessGen2 on 64 bit macOS
   * and FreeBSD only.
   * IT IS BOTH INCOMPLETE on any OS AND ENTIRELY UNTESTED ON OTHER
   * BSD DERIVATIVES.
   *
   * It contains the minimal declarations, plus a few extras, needed by
   * UnixProcessGen2. It is fit for service for that purpose only.
   *
   * This file gives access to operating specific features.
   * It is not POSIX and not IEEE/ISO C. It fits into neither posixlib nor
   * clib, so it is taking up temporary (?) home here is javalib.
   *
   * In posixlib or clib, the constants below would be determined at
   * runtime, to match with the executing operating system.
   * Since the current intention is start first getting macOS working,
   * they are hard-coded (and may be DEAD WRONG on other BSDs).
   */

  /* kqueue/kevent usage reference, slightly old but useful:
   *   https://wiki.netbsd.org/tutorials/kqueue_tutorial/
   */

  // Beware: BSD layouts other than macOS & FreeBSD have not been tested.

  type intptr_t = stdint.intptr_t
  type uintptr_t = stdint.uintptr_t

  // scalafmt: { align.preset = more }
  type kevent = CStruct6[
    uintptr_t,      // ident       /* identifier for this event: uintptr_t */
    CShort,         // filter      /* filter for event */
    CUnsignedShort, // flags       /* action flags for kqueue */
    CUnsignedInt,   // fflags      /* filter flag value */
    intptr_t,       // data        /* filter data value: intptr_t */
    CVoidPtr        // void *udata 	/* opaque user data identifier */
  ]

  implicit class keventOps(val ptr: Ptr[kevent]) extends AnyVal {
    def ident  = ptr._1
    def filter = ptr._2
    def flags  = ptr._3
    def fflags = ptr._4
    def data   = ptr._5
    def udata  = ptr._6

    def ident_=(v: uintptr_t): Unit      = ptr._1 = v
    def filter_=(v: CShort): Unit        = ptr._2 = v
    def flags_=(v: CUnsignedShort): Unit = ptr._3 = v
    def fflags_=(v: CUnsignedInt): Unit  = ptr._4 = v
    def data_=(v: intptr_t): Unit        = ptr._5 = v
    def udata_=(v: CVoidPtr): Unit       = ptr._6 = v

    /* Convenience methods for common conversions hide and rely upon
     * some abstraction layer jumping illicit knowledge that CSSize is
     * a typedef for CSize.
     */

    def ident_=(v: CInt): Unit         = ptr._1 = v.toCSize
    def ident_=(v: CUnsignedInt): Unit = ptr._1 = v.toInt.toCSize

    def data_=(v: CInt): Unit         = ptr._1 = v.toCSize
    def data_=(v: CUnsignedInt): Unit = ptr._1 = v.toInt.toCSize
  }

  type kevent64_s = CStruct8[
    CUnsignedLongInt, // ident 	/* identifier for this event */
    CShort,           // filter /* filter for event */
    CUnsignedShort,   // flags 	/* action flags for kqueue */
    CUnsignedInt,     // fflags /* filter flag value */
    CLongInt,         // data 	/* filter data value */
    CUnsignedLongInt, // udata 	/* opaque user data identifier */
    CUnsignedLongInt, // ext[1]	/* filter-specific extensions */
    CUnsignedLongInt  // ext[2]	/* filter-specific extensions */
  ]

  implicit class kevent64Ops(val ptr: Ptr[kevent64_s]) extends AnyVal {
    def ident  = ptr._1
    def filter = ptr._2
    def flags  = ptr._3
    def fflags = ptr._4
    def data   = ptr._5
    def udata  = ptr._6
    def ext1   = ptr._7
    def ext2   = ptr._8

    def ident_=(v: CUnsignedLongInt): Unit = ptr._1 = v
    def filter_=(v: CShort): Unit          = ptr._2 = v
    def flags_=(v: CUnsignedShort): Unit   = ptr._3 = v
    def fflags_=(v: CUnsignedInt): Unit    = ptr._4 = v
    def data_=(v: CLongInt): Unit          = ptr._5 = v
    def udata_=(v: CUnsignedLongInt): Unit = ptr._6 = v
    def ext1_=(v: CUnsignedLongInt): Unit  = ptr._7 = v
    def ext2_=(v: CUnsignedLongInt): Unit  = ptr._8 = v
  }

  // scalafmt: { align.preset = none }

  /*
   * Filter types
   */

  final val EVFILT_READ = (-1)
  final val EVFILT_WRITE = (-2)
  final val EVFILT_PROC = (-5) /* attached to struct proc */

  /* actions */
  final val EV_ADD = 0x0001 /* add event to kq (implies enable) */
  final val EV_DELETE = 0x0002 /* delete event from kq */
  final val EV_ENABLE = 0x0004 /* enable event */
  final val EV_DISABLE = 0x0008 /* disable event (not reported) */

  /* flags */
  final val EV_ONESHOT = 0x0010 /* only report one occurrence */
  final val EV_CLEAR = 0x0020 /* clear event state after reporting */
  final val EV_RECEIPT = 0x0040 /* force immediate event output */
  /* ... with or without EV_ERROR */
  /* ... use KEVENT_FLAG_ERROR_EVENTS */
  /*     on syscalls supporting flags */

  final val EV_DISPATCH = 0x0080 /* disable event after reporting */

  // returned values
  final val EV_EOF = 0x8000 /* EOF detected */
  final val EV_ERROR = 0x4000 /* error, data contains errno */

  // for EVFILT_PROC, partial
  final val NOTE_EXIT = 0x80000000 /* process exited */
  final val NOTE_EXITSTATUS = 0x04000000 // exit status to be returned

  @extern
  object Extern {
    def kqueue(): CInt = extern

    @blocking def kevent(
        kq: CInt,
        changelist: Ptr[kevent],
        nchanges: CInt,
        eventlist: Ptr[kevent],
        nevents: CInt,
        timeout: Ptr[timespec]
    ): CInt = extern

    @blocking def kevent64(
        kq: CInt,
        changelist: Ptr[kevent64_s],
        nchanges: CInt,
        eventlist: Ptr[kevent64_s],
        nevents: CInt,
        flags: CUnsignedInt,
        timeout: Ptr[timespec]
    ): CInt = extern
  }
}
