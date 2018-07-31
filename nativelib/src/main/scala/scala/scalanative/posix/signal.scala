package scala.scalanative
package posix

import scala.scalanative.native._

@extern
object signal {
  import sys.types
  // pthread_t, size_t, and uid_t types as described in <sys/types.h>
  type pthread_t = types.pthread_t
  type size_t    = types.size_t
  type uid_t     = types.uid_t

  // timespec structure as described in <time.h>
  type timespec = time.timespec

  // define the following data types
  type sig_atomic_t = CInt
  // Integer or structure type of an object used to represent sets of signals
  type sigset_t = CUnsignedInt // macOS
  type sigset_t_linux = CStruct1[CArray[CUnsignedLong, Nat.Digit[Nat._1, Nat._6]]] // Linux
  type pid_t = types.pid_t
  type pthread_attr_t = types.pthread_attr_t

  type sigevent = CStruct5[native.CInt,
    native.CInt,
    Ptr[sigval], // Ptr instead of value
    CFunctionPtr1[Ptr[sigval], Unit], // Ptr instead of value
    Ptr[pthread_attr_t]]

  // define the following symbolic constants for the values of sigev_notify:
  @name("scalanative_sigev_none")
  def SIGEV_NONE: CInt = extern
  @name("scalanative_sigev_signal")
  def SIGEV_SIGNAL: CInt = extern
  @name("scalanative_sigev_thread")
  def SIGEV_THREAD: CInt = extern

  // union of int sival_int and void *sival_ptr
  type sigval = CArray[Byte, Nat._8]


  // A machine-specific representation of the saved context.
  type mcontext_t = native.Ptr[Byte] //MCONTEXT platfrom dependent macro resolve to Ptr to some struct
  // The <ucontext.h> header defines the mcontext_t type through typedef
  // this means this points to a macro and some machine specific structures (portability should be fun)
  // type mcontext_t = native.Ptr[struct___darwin_mcontext64]
  // __darwin_mcontext64 -> _STRUCT_MCONTEXT64 -> typedef _STRUCT_MCONTEXT64	*mcontext_t;


  type stack_t = CStruct3[
    Ptr[Byte], // void *ss_sp Stack base or pointer
    size_t,
    CInt]

  type ucontext_t = CStruct4[Ptr[ucontext_t],
    sigset_t,
    Ptr[stack_t], // Ptr instead of value
    mcontext_t]

  type siginfo_t = native.CStruct9[CInt,
    CInt,
    CInt,
    pid_t,
    uid_t,
    Ptr[Byte], // void *si_addr Address of faulting instruction
    CInt,
    CLong,
    Ptr[sigval]  // Ptr instead of value
    ]

  // define the symbolic constants in the Code column of the following table for use as values of si_code
  // that are signal-specific or non-signal-specific reasons why the signal was generated.





  type sig_t = native.CFunctionPtr1[native.CInt, Unit]
  type struct___darwin_pthread_handler_rec = native.CStruct3[native.CFunctionPtr1[native.Ptr[Byte], Unit], native.Ptr[Byte], native.Ptr[native.CArray[Byte, native.Nat.Digit[native.Nat._1, native.Nat.Digit[native.Nat._9, native.Nat._2]]]]]


  type struct__opaque_pthread_t = native.CStruct3[native.CLong, native.Ptr[struct___darwin_pthread_handler_rec], native.CArray[native.CChar, native.Nat.Digit[native.Nat._8, native.Nat.Digit[native.Nat._1, native.Nat.Digit[native.Nat._7, native.Nat._6]]]]]

  type struct___sigaction = native.CStruct4[union___sigaction_u, native.CFunctionPtr5[native.Ptr[Byte], native.CInt, native.CInt, native.Ptr[siginfo_t], native.Ptr[Byte], Unit], sigset_t, native.CInt]

  type struct_sigaction = native.CStruct3[union___sigaction_u, sigset_t, native.CInt]

  type struct_sigvec = native.CStruct3[native.CFunctionPtr1[native.CInt, Unit], native.CInt, native.CInt]
  type struct_sigstack = native.CStruct2[native.CString, native.CInt]
  type union___mbstate_t = native.CArray[Byte, native.Nat.Digit[native.Nat._1, native.Nat.Digit[native.Nat._2, native.Nat._8]]]

  type union___sigaction_u = native.CArray[Byte, native.Nat._8]

  def signal(anonymous0: native.CInt, anonymous1: native.CFunctionPtr1[native.CInt, Unit]): native.CFunctionPtr1[native.CInt, Unit] = native.extern
  def raise(anonymous0: native.CInt): native.CInt = native.extern
  def bsd_signal(anonymous0: native.CInt, anonymous1: native.CFunctionPtr1[native.CInt, Unit]): native.CFunctionPtr1[native.CInt, Unit] = native.extern

  def kill(anonymous0: pid_t, anonymous1: native.CInt): native.CInt = native.extern
  def killpg(anonymous0: pid_t, anonymous1: native.CInt): native.CInt = native.extern
  def pthread_kill(anonymous0: native.Ptr[pthread_t], anonymous1: native.CInt): native.CInt = native.extern
  def pthread_sigmask(anonymous0: native.CInt, anonymous1: native.Ptr[sigset_t], anonymous2: native.Ptr[sigset_t]): native.CInt = native.extern
  def sigaction(anonymous0: native.CInt, anonymous1: native.Ptr[struct_sigaction], anonymous2: native.Ptr[struct_sigaction]): native.CInt = native.extern
  def sigaddset(anonymous0: native.Ptr[sigset_t], anonymous1: native.CInt): native.CInt = native.extern
  def sigaltstack(anonymous0: native.Ptr[stack_t], anonymous1: native.Ptr[stack_t]): native.CInt = native.extern
  def sigdelset(anonymous0: native.Ptr[sigset_t], anonymous1: native.CInt): native.CInt = native.extern
  def sigemptyset(anonymous0: native.Ptr[sigset_t]): native.CInt = native.extern
  def sigfillset(anonymous0: native.Ptr[sigset_t]): native.CInt = native.extern
  def sighold(anonymous0: native.CInt): native.CInt = native.extern
  def sigignore(anonymous0: native.CInt): native.CInt = native.extern
  def siginterrupt(anonymous0: native.CInt, anonymous1: native.CInt): native.CInt = native.extern
  def sigismember(anonymous0: native.Ptr[sigset_t], anonymous1: native.CInt): native.CInt = native.extern
  def sigpause(anonymous0: native.CInt): native.CInt = native.extern
  def sigpending(anonymous0: native.Ptr[sigset_t]): native.CInt = native.extern
  def sigprocmask(anonymous0: native.CInt, anonymous1: native.Ptr[sigset_t], anonymous2: native.Ptr[sigset_t]): native.CInt = native.extern
  def sigrelse(anonymous0: native.CInt): native.CInt = native.extern
  def sigset(anonymous0: native.CInt, anonymous1: native.CFunctionPtr1[native.CInt, Unit]): native.CFunctionPtr1[native.CInt, Unit] = native.extern
  def sigsuspend(anonymous0: native.Ptr[sigset_t]): native.CInt = native.extern
  def sigwait(anonymous0: native.Ptr[sigset_t], anonymous1: native.Ptr[native.CInt]): native.CInt = native.extern
  def psignal(anonymous0: native.CUnsignedInt, anonymous1: native.CString): Unit = native.extern
  def sigblock(anonymous0: native.CInt): native.CInt = native.extern
  def sigsetmask(anonymous0: native.CInt): native.CInt = native.extern
  def sigvec(anonymous0: native.CInt, anonymous1: native.Ptr[struct_sigvec], anonymous2: native.Ptr[struct_sigvec]): native.CInt = native.extern
  def __sigbits(__signo: native.CInt): native.CInt = native.extern
}

import signal._

object signalHelpers {

  implicit class struct___darwin_pthread_handler_rec_ops(val p: native.Ptr[struct___darwin_pthread_handler_rec]) extends AnyVal {
    def __routine: native.CFunctionPtr1[native.Ptr[Byte], Unit] = !p._1
    def __routine_=(value: native.CFunctionPtr1[native.Ptr[Byte], Unit]):Unit = !p._1 = value
    def __arg: native.Ptr[Byte] = !p._2
    def __arg_=(value: native.Ptr[Byte]):Unit = !p._2 = value
    def __next: native.Ptr[native.CArray[Byte, native.Nat.Digit[native.Nat._1, native.Nat.Digit[native.Nat._9, native.Nat._2]]]] = !p._3
    def __next_=(value: native.Ptr[native.CArray[Byte, native.Nat.Digit[native.Nat._1, native.Nat.Digit[native.Nat._9, native.Nat._2]]]]):Unit = !p._3 = value
  }

  def struct___darwin_pthread_handler_rec()(implicit z: native.Zone): native.Ptr[struct___darwin_pthread_handler_rec] = native.alloc[struct___darwin_pthread_handler_rec]

  implicit class struct__opaque_pthread_t_ops(val p: native.Ptr[struct__opaque_pthread_t]) extends AnyVal {
    def __sig: native.CLong = !p._1
    def __sig_=(value: native.CLong):Unit = !p._1 = value
    def __cleanup_stack: native.Ptr[struct___darwin_pthread_handler_rec] = !p._2
    def __cleanup_stack_=(value: native.Ptr[struct___darwin_pthread_handler_rec]):Unit = !p._2 = value
    def __opaque: native.CArray[native.CChar, native.Nat.Digit[native.Nat._8, native.Nat.Digit[native.Nat._1, native.Nat.Digit[native.Nat._7, native.Nat._6]]]] = !p._3
    def __opaque_=(value: native.CArray[native.CChar, native.Nat.Digit[native.Nat._8, native.Nat.Digit[native.Nat._1, native.Nat.Digit[native.Nat._7, native.Nat._6]]]]):Unit = !p._3 = value
  }

  def struct__opaque_pthread_t()(implicit z: native.Zone): native.Ptr[struct__opaque_pthread_t] = native.alloc[struct__opaque_pthread_t]

  implicit class struct_sigevent_ops(val p: native.Ptr[sigevent]) extends AnyVal {
    def sigev_notify: native.CInt = !p._1
    def sigev_notify_=(value: native.CInt):Unit = !p._1 = value
    def sigev_signo: native.CInt = !p._2
    def sigev_signo_=(value: native.CInt):Unit = !p._2 = value
    def sigev_value: sigval = !p._3
    def sigev_value_=(value: sigval):Unit = !p._3 = value
    def sigev_notify_function: native.CFunctionPtr1[sigval, Unit] = !p._4
    def sigev_notify_function_=(value: native.CFunctionPtr1[sigval, Unit]):Unit = !p._4 = value
    def sigev_notify_attributes: native.Ptr[pthread_attr_t] = !p._5
    def sigev_notify_attributes_=(value: native.Ptr[pthread_attr_t]):Unit = !p._5 = value
  }

  def struct_sigevent()(implicit z: native.Zone): native.Ptr[sigevent] = native.alloc[sigevent]

  implicit class struct___siginfo_ops(val p: native.Ptr[struct___siginfo]) extends AnyVal {
    def si_signo: native.CInt = !p._1
    def si_signo_=(value: native.CInt):Unit = !p._1 = value
    def si_errno: native.CInt = !p._2
    def si_errno_=(value: native.CInt):Unit = !p._2 = value
    def si_code: native.CInt = !p._3
    def si_code_=(value: native.CInt):Unit = !p._3 = value
    def si_pid: pid_t = !p._4
    def si_pid_=(value: pid_t):Unit = !p._4 = value
    def si_uid: uid_t = !p._5
    def si_uid_=(value: uid_t):Unit = !p._5 = value
    def si_status: native.CInt = !p._6
    def si_status_=(value: native.CInt):Unit = !p._6 = value
    def si_addr: native.Ptr[Byte] = !p._7
    def si_addr_=(value: native.Ptr[Byte]):Unit = !p._7 = value
    def si_value: sigval = !p._8
    def si_value_=(value: sigval):Unit = !p._8 = value
    def si_band: native.CLong = !p._9
    def si_band_=(value: native.CLong):Unit = !p._9 = value
    def __pad: native.CArray[native.CUnsignedLong, native.Nat._7] = !p._10
    def __pad_=(value: native.CArray[native.CUnsignedLong, native.Nat._7]):Unit = !p._10 = value
  }

  def struct___siginfo()(implicit z: native.Zone): native.Ptr[struct___siginfo] = native.alloc[struct___siginfo]

  implicit class struct___sigaction_ops(val p: native.Ptr[struct___sigaction]) extends AnyVal {
    def __sigaction_u: union___sigaction_u = !p._1
    def __sigaction_u_=(value: union___sigaction_u):Unit = !p._1 = value
    def sa_tramp: native.CFunctionPtr5[native.Ptr[Byte], native.CInt, native.CInt, native.Ptr[siginfo_t], native.Ptr[Byte], Unit] = !p._2
    def sa_tramp_=(value: native.CFunctionPtr5[native.Ptr[Byte], native.CInt, native.CInt, native.Ptr[siginfo_t], native.Ptr[Byte], Unit]):Unit = !p._2 = value
    def sa_mask: sigset_t = !p._3
    def sa_mask_=(value: sigset_t):Unit = !p._3 = value
    def sa_flags: native.CInt = !p._4
    def sa_flags_=(value: native.CInt):Unit = !p._4 = value
  }

  def struct___sigaction()(implicit z: native.Zone): native.Ptr[struct___sigaction] = native.alloc[struct___sigaction]

  implicit class struct_sigaction_ops(val p: native.Ptr[struct_sigaction]) extends AnyVal {
    def __sigaction_u: union___sigaction_u = !p._1
    def __sigaction_u_=(value: union___sigaction_u):Unit = !p._1 = value
    def sa_mask: sigset_t = !p._2
    def sa_mask_=(value: sigset_t):Unit = !p._2 = value
    def sa_flags: native.CInt = !p._3
    def sa_flags_=(value: native.CInt):Unit = !p._3 = value
  }

  def struct_sigaction()(implicit z: native.Zone): native.Ptr[struct_sigaction] = native.alloc[struct_sigaction]

  implicit class struct_sigvec_ops(val p: native.Ptr[struct_sigvec]) extends AnyVal {
    def sv_handler: native.CFunctionPtr1[native.CInt, Unit] = !p._1
    def sv_handler_=(value: native.CFunctionPtr1[native.CInt, Unit]):Unit = !p._1 = value
    def sv_mask: native.CInt = !p._2
    def sv_mask_=(value: native.CInt):Unit = !p._2 = value
    def sv_flags: native.CInt = !p._3
    def sv_flags_=(value: native.CInt):Unit = !p._3 = value
  }

  def struct_sigvec()(implicit z: native.Zone): native.Ptr[struct_sigvec] = native.alloc[struct_sigvec]

  implicit class struct_sigstack_ops(val p: native.Ptr[struct_sigstack]) extends AnyVal {
    def ss_sp: native.CString = !p._1
    def ss_sp_=(value: native.CString):Unit = !p._1 = value
    def ss_onstack: native.CInt = !p._2
    def ss_onstack_=(value: native.CInt):Unit = !p._2 = value
  }

  def struct_sigstack()(implicit z: native.Zone): native.Ptr[struct_sigstack] = native.alloc[struct_sigstack]

  implicit class union___mbstate_t_pos(val p: native.Ptr[union___mbstate_t]) extends AnyVal {
    def __mbstate8: native.Ptr[native.CArray[native.CChar, native.Nat.Digit[native.Nat._1, native.Nat.Digit[native.Nat._2, native.Nat._8]]]] = p.cast[native.Ptr[native.CArray[native.CChar, native.Nat.Digit[native.Nat._1, native.Nat.Digit[native.Nat._2, native.Nat._8]]]]]
    def __mbstate8_=(value: native.CArray[native.CChar, native.Nat.Digit[native.Nat._1, native.Nat.Digit[native.Nat._2, native.Nat._8]]]): Unit = !p.cast[native.Ptr[native.CArray[native.CChar, native.Nat.Digit[native.Nat._1, native.Nat.Digit[native.Nat._2, native.Nat._8]]]]] = value
    def _mbstateL: native.Ptr[native.CLongLong] = p.cast[native.Ptr[native.CLongLong]]
    def _mbstateL_=(value: native.CLongLong): Unit = !p.cast[native.Ptr[native.CLongLong]] = value
  }

  implicit class union_sigval_pos(val p: native.Ptr[sigval]) extends AnyVal {
    def sival_int: native.Ptr[native.CInt] = p.cast[native.Ptr[native.CInt]]
    def sival_int_=(value: native.CInt): Unit = !p.cast[native.Ptr[native.CInt]] = value
    def sival_ptr: native.Ptr[native.Ptr[Byte]] = p.cast[native.Ptr[native.Ptr[Byte]]]
    def sival_ptr_=(value: native.Ptr[Byte]): Unit = !p.cast[native.Ptr[native.Ptr[Byte]]] = value
  }

  implicit class union___sigaction_u_pos(val p: native.Ptr[union___sigaction_u]) extends AnyVal {
    def __sa_handler: native.Ptr[native.CFunctionPtr1[native.CInt, Unit]] = p.cast[native.Ptr[native.CFunctionPtr1[native.CInt, Unit]]]
    def __sa_handler_=(value: native.CFunctionPtr1[native.CInt, Unit]): Unit = !p.cast[native.Ptr[native.CFunctionPtr1[native.CInt, Unit]]] = value
    def __sa_sigaction: native.Ptr[native.CFunctionPtr3[native.CInt, native.Ptr[struct___siginfo], native.Ptr[Byte], Unit]] = p.cast[native.Ptr[native.CFunctionPtr3[native.CInt, native.Ptr[struct___siginfo], native.Ptr[Byte], Unit]]]
    def __sa_sigaction_=(value: native.CFunctionPtr3[native.CInt, native.Ptr[struct___siginfo], native.Ptr[Byte], Unit]): Unit = !p.cast[native.Ptr[native.CFunctionPtr3[native.CInt, native.Ptr[struct___siginfo], native.Ptr[Byte], Unit]]] = value
  }
}

