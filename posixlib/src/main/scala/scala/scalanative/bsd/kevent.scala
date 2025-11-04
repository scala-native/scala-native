package scala.scalanative
package bsd

import posix._
import unsafe._
import unsigned._

@extern
@define("__SCALANATIVE_POSIX_KEVENT")
object kevent {

  type intptr_t = stdint.intptr_t
  type uintptr_t = stdint.uintptr_t

  // scalafmt: { align.preset = more }

  type kevent = CStruct6[
    uintptr_t,      // ident       /* identifier for this event: uintptr_t */
    CShort,         // filter      /* filter for event */
    CUnsignedShort, // flags       /* action flags for kqueue */
    CUnsignedInt,   // fflags      /* filter flag value */
    intptr_t,       // data        /* filter data value: intptr_t */
    CVoidPtr        // void *udata /* opaque user data identifier */
  ]

  // scalafmt: { align.preset = none }

  @name("scalanative_kevent_evfilt_read")
  def EVFILT_READ: CInt = extern
  @name("scalanative_kevent_evfilt_write")
  def EVFILT_WRITE: CInt = extern
  @name("scalanative_kevent_evfilt_proc")
  def EVFILT_PROC: CInt = extern

  @name("scalanative_kevent_ev_add")
  def EV_ADD: CInt = extern
  @name("scalanative_kevent_ev_delete")
  def EV_DELETE: CInt = extern
  @name("scalanative_kevent_ev_enable")
  def EV_ENABLE: CInt = extern
  @name("scalanative_kevent_ev_disable")
  def EV_DISABLE: CInt = extern
  @name("scalanative_kevent_ev_dispatch")
  def EV_DISPATCH: CInt = extern

  @name("scalanative_kevent_ev_oneshot")
  def EV_ONESHOT: CInt = extern
  @name("scalanative_kevent_ev_clear")
  def EV_CLEAR: CInt = extern
  @name("scalanative_kevent_ev_receipt")
  def EV_RECEIPT: CInt = extern

  @name("scalanative_kevent_ev_eof")
  def EV_EOF: CInt = extern
  @name("scalanative_kevent_ev_error")
  def EV_ERROR: CInt = extern

  @name("scalanative_kevent_note_exit")
  def NOTE_EXIT: CInt = extern
  @name("scalanative_kevent_note_exitstatus")
  def NOTE_EXITSTATUS: CInt = extern

  def kqueue(): CInt = extern

  @blocking
  def kevent(
      kq: CInt,
      changelist: Ptr[kevent],
      nchanges: CInt,
      eventlist: Ptr[kevent],
      nevents: CInt,
      timeout: Ptr[time.timespec]
  ): CInt = extern

  // scalafmt: { align.preset = more }

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

  // scalafmt: { align.preset = none }

}
