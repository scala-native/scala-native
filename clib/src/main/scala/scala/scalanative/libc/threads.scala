package scala.scalanative.libc

import scala.scalanative.unsafe._
import time.timespec
import scala.scalanative.runtime.Intrinsics
import scala.scalanative.runtime.RawPtr

@extern
object threads {
  //threads
  type thrd_start_t = CFuncPtr1[Ptr[Byte], CInt]
  trait thrd_t
  @name("scalanative_thrd_t_size")
  private val thrd_t_size: CSize = extern

  object thrd_t {
    implicit val tag: Tag[thrd_t] = new Tag[thrd_t] {
      override def size: CSize      = thrd_t_size
      override def alignment: CSize = thrd_t_size
    }
  }
  val thrd_success: CInt = extern

  val thrd_nomem: CInt = extern

  val thrd_timedout: CInt = extern

  val thrd_busy: CInt = extern

  val thrd_error: CInt = extern

  def thrd_create(thr: Ptr[thrd_t], func: thrd_start_t, arg: Ptr[Byte]): CInt =
    extern

  @name("scalanative_thrd_equal")
  def thrd_equal(lhs: Ptr[thrd_t], rhs: Ptr[thrd_t]): CInt = extern

  @name("scalanative_thrd_current")
  def thrd_current(current: Ptr[thrd_t]): Unit = extern
  def thrd_sleep(duration: Ptr[timespec], remaining: Ptr[timespec]): CInt =
    extern
  def thrd_yield(): Unit            = extern
  def thrd_exit(res: CInt): Nothing = extern

  @name("scalanative_thrd_detach")
  def thrd_detach(thr: Ptr[thrd_t]): CInt = extern
  @name("scalanative_thrd_join")
  def thrd_join(thr: Ptr[thrd_t], res: Ptr[CInt]): CInt = extern

  //MUTEXES
  trait mtx_t
  @name("scalanative_mtx_t_size")
  private val mtx_t_size: CSize = extern

  object mtx_t {
    implicit val tag: Tag[mtx_t] = new Tag[mtx_t] {
      override def size: CSize      = mtx_t_size
      override def alignment: CSize = mtx_t_size
    }
  }

  trait once_flag
  @name("scalanative_once_flag_size")
  private val once_flag_size = extern

  object once_flag {
    implicit val tag: Tag[once_flag] = new Tag[once_flag] {
      override def size      = once_flag_size
      override def alignment = once_flag_size
    }
  }

  def mtx_init(mutex: Ptr[mtx_t], `type`: CInt): CInt                   = extern
  def mtx_lock(mutex: Ptr[mtx_t]): CInt                                 = extern
  def mtx_timedlock(mutex: Ptr[mtx_t], time_point: Ptr[timespec]): CInt = extern
  def mtx_trylock(mutex: Ptr[mtx_t]): CInt                              = extern
  def mtx_unlock(mutex: Ptr[mtx_t]): CInt                               = extern
  def mtx_destroy(mutex: Ptr[mtx_t]): Unit                              = extern
  val mtx_plain: CInt                                                   = extern
  val mtx_recursive: CInt                                               = extern
  val mtx_timed: CInt                                                   = extern
  def call_once(flag: Ptr[once_flag], func: CFuncPtr0[Unit]): Unit      = extern

  //Condition variables
  trait cnd_t
  @name("scalanative_cnd_t_size")
  private val cnd_t_size = extern

  object cnd_t {
    implicit val tag: Tag[cnd_t] = new Tag[cnd_t] {
      override def size      = cnd_t_size
      override def alignment = cnd_t_size
    }
  }

  def cnd_init(cond: Ptr[cnd_t]): CInt                    = extern
  def cnd_signal(cond: Ptr[cnd_t]): CInt                  = extern
  def cnd_broadcast(cond: Ptr[cnd_t]): CInt               = extern
  def cnd_wait(cond: Ptr[cnd_t], mutex: Ptr[mtx_t]): CInt = extern
  def cnd_timedwait(cond: Ptr[cnd_t],
                    mutex: Ptr[mtx_t],
                    time_point: Ptr[timespec]): CInt = extern
  def cnd_destroy(cond: Ptr[cnd_t]): Unit            = extern

  //Thread-local storage
  type tss_dor_t = CFuncPtr1[Ptr[Byte], Unit]

  trait tss_t
  @name("scalanative_tss_t_size")
  private val tss_t_size = extern

  object tss_t {
    implicit val tag: Tag[tss_t] = new Tag[tss_t] {
      override def size      = tss_t_size
      override def alignment = tss_t_size
    }
  }

  @name("scalanative_tss_dtor_iterations")
  val TSS_DTOR_ITERATIONS: CInt = extern

  def tss_create(tss_key: Ptr[tss_t], destructor: tss_dor_t): CInt = extern
  @name("scalanative_tss_get")
  def tss_get(tss_ket: Ptr[tss_t]): Ptr[Byte] = extern
  @name("scalanative_tss_set")
  def tss_set(tss_id: Ptr[tss_t], `val`: Ptr[Byte]): CInt = extern
  @name("scalanative_tss_delete")
  def tss_delete(tss_id: Ptr[tss_t]): Unit = extern
}
