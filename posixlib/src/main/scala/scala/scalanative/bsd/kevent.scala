package scala.scalanative
package bsd

import posix._
import unsafe._

@extern
@define("__SCALANATIVE_POSIX_KEVENT")
object kevent {

  import stdint._

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
      changelist: CVoidPtr,
      nchanges: CInt,
      eventlist: CVoidPtr,
      nevents: CInt,
      timeout: Ptr[time.timespec]
  ): CInt = extern

  def scalanative_kevent_size(): CSize = extern

  def scalanative_kevent_set(
      ev: CVoidPtr,
      idx: CInt,
      ident: uintptr_t,
      filter: int16_t,
      flags: uint16_t,
      fflags: uint32_t,
      data: intptr_t,
      udata: CVoidPtr
  ): Unit = extern

  def scalanative_kevent_get(
      ev: CVoidPtr,
      idx: CInt,
      ident: Ptr[uintptr_t],
      filter: Ptr[int16_t],
      flags: Ptr[uint16_t],
      fflags: Ptr[uint32_t],
      data: Ptr[intptr_t],
      udata: Ptr[CVoidPtr]
  ): Unit = extern

}
