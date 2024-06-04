package java.lang.process

import scala.scalanative.unsafe._
import scala.scalanative.posix.time.timespec

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

// format: off

  type kevent = CStruct6[
    Ptr[CUnsignedInt],	// ident 	/* identifier for this event */
    CShort, 		// filter 	/* filter for event */
    CUnsignedShort, 	// flags 	/* action flags for kqueue */
    CUnsignedInt, 	// fflags 	/* filter flag value */
    Ptr[CInt],	 	// data		/* filter data value */
    Ptr[Byte] 		// void *udata 	/* opaque user data identifier */
    ]

  type kevent64_s = CStruct7[
    CUnsignedLongInt, 	// ident 	/* identifier for this event */
    CShort, 		// filter 	/* filter for event */
    CUnsignedShort, 	// flags 	/* action flags for kqueue */
    CUnsignedInt, 	// fflags 	/* filter flag value */
    CLongInt, 	        // data 	/* filter data value */
    CUnsignedLongInt,   // udata 	/* opaque user data identifier */
    Ptr[CUnsignedLongInt] // ext[2]	/* filter-specific extensions */
  ]

// format: on

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
