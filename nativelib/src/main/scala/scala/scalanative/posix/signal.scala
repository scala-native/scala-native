package scala.scalanative
package posix

import scala.scalanative.native._

/**
 * Some of the functionality described on this reference page extends the ISO C standard.
 * Applications shall define the appropriate feature test macro (see XSH The Compilation Environment )
 * to enable the visibility of these symbols in this header.
 *
 * Note 1: The functionality described may be removed in a future version of this volume of POSIX.1-2017
 */
@extern
object signal {
  // define the following macros, which shall expand to constant expressions with distinct values
  // that have a type compatible with the second argument to, and the return value of, the signal() function,
  // and whose values shall compare unequal to the address of any declarable function.
  @name("scalanative_sig_dfl")
  def SIG_DFL: CFunctionPtr1[CInt, Unit] = extern
  @name("scalanative_sig_err")
  def SIG_ERR: CFunctionPtr1[CInt, Unit] = extern
  // Note 1: Linux
//  @name("scalanative_sig_hold")
//  def SIG_HOLD: CFunctionPtr1[CInt, Unit] = extern
  @name("scalanative_sig_ign")
  def SIG_IGN: CFunctionPtr1[CInt, Unit] = extern

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
  // macOS CUnsignedInt
  // Linux CStruct1[CArray[CUnsignedLong, Nat.Digit[Nat._1, Nat._6]]]
  type sigset_t = Ptr[Byte]

  type pid_t          = types.pid_t
  type pthread_attr_t = types.pthread_attr_t

  type sigevent = CStruct5[
    CInt, // sigev_notify
    CInt, // sigev_signo
    Ptr[sigval], // sigev_value - Ptr instead of value
    CFunctionPtr1[Ptr[sigval], Unit], // sigev_notify_function - Ptr instead of value
    Ptr[pthread_attr_t] // sigev_notify_attributes
  ]
  // define the following symbolic constants for the values of sigev_notify:
  @name("scalanative_sigev_none")
  def SIGEV_NONE: CInt = extern
  @name("scalanative_sigev_signal")
  def SIGEV_SIGNAL: CInt = extern
  @name("scalanative_sigev_thread")
  def SIGEV_THREAD: CInt = extern

  // union of int sival_int and void *sival_ptr
  type sigval = CArray[Byte, Nat._8]

  // manditory signals
  @name("scalanative_sigabrt")
  def SIGABRT: CInt = extern
  @name("scalanative_sigalrm")
  def SIGALRM: CInt = extern
  @name("scalanative_sigbus")
  def SIGBUS: CInt = extern
  @name("scalanative_sigchld")
  def SIGCHLD: CInt = extern
  @name("scalanative_sigcont")
  def SIGCONT: CInt = extern
  @name("scalanative_sigfpe")
  def SIGFPE: CInt = extern
  @name("scalanative_sighup")
  def SIGHUP: CInt = extern
  @name("scalanative_sigill")
  def SIGILL: CInt = extern
  @name("scalanative_sigint")
  def SIGINT: CInt = extern
  @name("scalanative_sigkill")
  def SIGKILL: CInt = extern
  @name("scalanative_sigpipe")
  def SIGPIPE: CInt = extern
  @name("scalanative_sigquit")
  def SIGQUIT: CInt = extern
  @name("scalanative_sigsegv")
  def SIGSEGV: CInt = extern
  @name("scalanative_sigstop")
  def SIGSTOP: CInt = extern
  @name("scalanative_sigterm")
  def SIGTERM: CInt = extern
  @name("scalanative_sigtstp")
  def SIGTSTP: CInt = extern
  @name("scalanative_sigttin")
  def SIGTTIN: CInt = extern
  @name("scalanative_sigttou")
  def SIGTTOU: CInt = extern
  @name("scalanative_sigusr1")
  def SIGUSR1: CInt = extern
  @name("scalanative_sigusr2")
  def SIGUSR2: CInt = extern
  // Note 1: macOS
//  @name("scalanative_sigpoll")
//  def SIGPOLL: CInt = extern
  @name("scalanative_sigprof")
  def SIGPROF: CInt = extern
  @name("scalanative_sigsys")
  def SIGSYS: CInt = extern
  @name("scalanative_sigtrap")
  def SIGTRAP: CInt = extern
  @name("scalanative_sigurg")
  def SIGURG: CInt = extern
  @name("scalanative_sigtalrm")
  def SIGVTALRM: CInt = extern
  @name("scalanative_sigxcpu")
  def SIGXCPU: CInt = extern
  @name("scalanative_sigxfsz")
  def SIGXFSZ: CInt = extern

  // A machine-specific representation of the saved context
  // mac OS type mcontext_t = Ptr[struct___darwin_mcontext64]
  // __darwin_mcontext64 -> _STRUCT_MCONTEXT64 -> typedef _STRUCT_MCONTEXT64	*mcontext_t;
  type mcontext_t = Ptr[Byte]

  type stack_t = CStruct3[
    Ptr[Byte], // void *ss_sp Stack base or pointer
    size_t,
    CInt
  ]

  type ucontext_t = CStruct4[
    Ptr[Byte], // ptr to ucontext_t
    sigset_t,
    Ptr[stack_t], // Ptr instead of value
    mcontext_t
  ]

  type siginfo_t = CStruct9[
    CInt,
    CInt,
    CInt,
    pid_t,
    uid_t,
    Ptr[Byte], // void *si_addr Address of faulting instruction
    CInt,
    CLong,
    Ptr[sigval] // Ptr instead of value
  ]

  // define the symbolic constants in the Code column of the following table for use as values of si_code
  // that are signal-specific or non-signal-specific reasons why the signal was generated.

  type sig_t = CFunctionPtr1[CInt, Unit]
  type struct___darwin_pthread_handler_rec =
    CStruct3[CFunctionPtr1[Ptr[Byte], Unit],
             Ptr[Byte],
             Ptr[CArray[Byte, Nat.Digit[Nat._1, Nat.Digit[Nat._9, Nat._2]]]]]

  type struct__opaque_pthread_t = CStruct3[
    CLong,
    Ptr[struct___darwin_pthread_handler_rec],
    CArray[CChar,
           Nat.Digit[Nat._8, Nat.Digit[Nat._1, Nat.Digit[Nat._7, Nat._6]]]]]

  type struct___sigaction = CStruct4[
    union___sigaction_u,
    CFunctionPtr5[Ptr[Byte], CInt, CInt, Ptr[siginfo_t], Ptr[Byte], Unit],
    sigset_t,
    CInt]

  type struct_sigaction = CStruct3[union___sigaction_u, sigset_t, CInt]

  type struct_sigvec   = CStruct3[CFunctionPtr1[CInt, Unit], CInt, CInt]
  type struct_sigstack = CStruct2[CString, CInt]
  type union___mbstate_t =
    CArray[Byte, Nat.Digit[Nat._1, Nat.Digit[Nat._2, Nat._8]]]

  type union___sigaction_u = CArray[Byte, Nat._8]

  def signal(p0: CInt,
             p1: CFunctionPtr1[CInt, Unit]): CFunctionPtr1[CInt, Unit] = extern
  def raise(p0: CInt): CInt                                            = extern
  def bsd_signal(p0: CInt,
                 p1: CFunctionPtr1[CInt, Unit]): CFunctionPtr1[CInt, Unit] =
    extern
  def kill(p0: pid_t, p1: CInt): CInt                  = extern
  def killpg(p0: pid_t, p1: CInt): CInt                = extern
  def psiginfo(p0: Ptr[siginfo_t], p1: CString): Unit  = extern
  def psignal(p0: CInt, p1: CString): Unit             = extern
  def pthread_kill(p0: Ptr[pthread_t], p1: CInt): CInt = extern
  def pthread_sigmask(p0: CInt, p1: Ptr[sigset_t], p2: Ptr[sigset_t]): CInt =
    extern
  def sigaction(p0: CInt,
                p1: Ptr[struct_sigaction],
                p2: Ptr[struct_sigaction]): CInt                        = extern
  def sigaddset(p0: Ptr[sigset_t], p1: CInt): CInt                      = extern
  def sigaltstack(p0: Ptr[stack_t], p1: Ptr[stack_t]): CInt             = extern
  def sigdelset(p0: Ptr[sigset_t], p1: CInt): CInt                      = extern
  def sigemptyset(p0: Ptr[sigset_t]): CInt                              = extern
  def sigfillset(p0: Ptr[sigset_t]): CInt                               = extern
  def sighold(p0: CInt): CInt                                           = extern
  def sigignore(p0: CInt): CInt                                         = extern
  def siginterrupt(p0: CInt, p1: CInt): CInt                            = extern
  def sigismember(p0: Ptr[sigset_t], p1: CInt): CInt                    = extern
  def sigpause(p0: CInt): CInt                                          = extern
  def sigpending(p0: Ptr[sigset_t]): CInt                               = extern
  def sigprocmask(p0: CInt, p1: Ptr[sigset_t], p2: Ptr[sigset_t]): CInt = extern
  def sigrelse(p0: CInt): CInt                                          = extern
  def sigset(p0: CInt,
             p1: CFunctionPtr1[CInt, Unit]): CFunctionPtr1[CInt, Unit] = extern
  def sigsuspend(p0: Ptr[sigset_t]): CInt                              = extern
  def sigwait(p0: Ptr[sigset_t], p1: Ptr[CInt]): CInt                  = extern
  def sigblock(p0: CInt): CInt                                         = extern
  def sigsetmask(p0: CInt): CInt                                       = extern
  def sigvec(p0: CInt, p1: Ptr[struct_sigvec], p2: Ptr[struct_sigvec]): CInt =
    extern
  def __sigbits(__signo: CInt): CInt = extern
}

import signal._

//object signalOps {
//
//  implicit class struct___darwin_pthread_handler_rec_ops(
//      val p: Ptr[struct___darwin_pthread_handler_rec])
//      extends AnyVal {
//    def __routine: CFunctionPtr1[Ptr[Byte], Unit]                = !p._1
//    def __routine_=(value: CFunctionPtr1[Ptr[Byte], Unit]): Unit = !p._1 = value
//    def __arg: Ptr[Byte]                                         = !p._2
//    def __arg_=(value: Ptr[Byte]): Unit                          = !p._2 = value
//    def __next
//      : Ptr[CArray[Byte, Nat.Digit[Nat._1, Nat.Digit[Nat._9, Nat._2]]]] = !p._3
//    def __next_=(
//        value: Ptr[CArray[Byte, Nat.Digit[Nat._1, Nat.Digit[Nat._9, Nat._2]]]])
//      : Unit = !p._3 = value
//  }
//
//  def struct___darwin_pthread_handler_rec()(
//      implicit z: Zone): Ptr[struct___darwin_pthread_handler_rec] =
//    alloc[struct___darwin_pthread_handler_rec]
//
//  implicit class struct__opaque_pthread_t_ops(
//      val p: Ptr[struct__opaque_pthread_t])
//      extends AnyVal {
//    def __sig: CLong                                              = !p._1
//    def __sig_=(value: CLong): Unit                               = !p._1 = value
//    def __cleanup_stack: Ptr[struct___darwin_pthread_handler_rec] = !p._2
//    def __cleanup_stack_=(
//        value: Ptr[struct___darwin_pthread_handler_rec]): Unit = !p._2 = value
//    def __opaque: CArray[
//      CChar,
//      Nat.Digit[Nat._8, Nat.Digit[Nat._1, Nat.Digit[Nat._7, Nat._6]]]] = !p._3
//    def __opaque_=(
//        value: CArray[
//          CChar,
//          Nat.Digit[Nat._8, Nat.Digit[Nat._1, Nat.Digit[Nat._7, Nat._6]]]])
//      : Unit = !p._3 = value
//  }
//
//  def struct__opaque_pthread_t()(
//      implicit z: Zone): Ptr[struct__opaque_pthread_t] =
//    alloc[struct__opaque_pthread_t]
//
//  implicit class struct_sigevent_ops(val p: Ptr[sigevent]) extends AnyVal {
//    def sigev_notify: CInt                                 = !p._1
//    def sigev_notify_=(value: CInt): Unit                  = !p._1 = value
//    def sigev_signo: CInt                                  = !p._2
//    def sigev_signo_=(value: CInt): Unit                   = !p._2 = value
//    def sigev_value: sigval                                = !p._3
//    def sigev_value_=(value: sigval): Unit                 = !p._3 = value
//    def sigev_notify_function: CFunctionPtr1[sigval, Unit] = !p._4
//    def sigev_notify_function_=(value: CFunctionPtr1[sigval, Unit]): Unit =
//      !p._4 = value
//    def sigev_notify_attributes: Ptr[pthread_attr_t] = !p._5
//    def sigev_notify_attributes_=(value: Ptr[pthread_attr_t]): Unit =
//      !p._5 = value
//  }
//
//  def struct_sigevent()(implicit z: Zone): Ptr[sigevent] = alloc[sigevent]
//
//  implicit class siginfo_ops(val p: Ptr[siginfo_t]) extends AnyVal {
//    def si_signo: CInt                                      = !p._1
//    def si_signo_=(value: CInt): Unit                       = !p._1 = value
//    def si_errno: CInt                                      = !p._2
//    def si_errno_=(value: CInt): Unit                       = !p._2 = value
//    def si_code: CInt                                       = !p._3
//    def si_code_=(value: CInt): Unit                        = !p._3 = value
//    def si_pid: pid_t                                       = !p._4
//    def si_pid_=(value: pid_t): Unit                        = !p._4 = value
//    def si_uid: uid_t                                       = !p._5
//    def si_uid_=(value: uid_t): Unit                        = !p._5 = value
//    def si_status: CInt                                     = !p._6
//    def si_status_=(value: CInt): Unit                      = !p._6 = value
//    def si_addr: Ptr[Byte]                                  = !p._7
//    def si_addr_=(value: Ptr[Byte]): Unit                   = !p._7 = value
//    def si_value: sigval                                    = !p._8
//    def si_value_=(value: sigval): Unit                     = !p._8 = value
//    def si_band: CLong                                      = !p._9
//    def si_band_=(value: CLong): Unit                       = !p._9 = value
//    def __pad: CArray[CUnsignedLong, Nat._7]                = !p._10
//    def __pad_=(value: CArray[CUnsignedLong, Nat._7]): Unit = !p._10 = value
//  }
//
//  def struct___siginfo()(implicit z: Zone): Ptr[siginfo_t] = alloc[siginfo_t]
//
//  implicit class struct___sigaction_ops(val p: Ptr[struct___sigaction])
//      extends AnyVal {
//    def __sigaction_u: union___sigaction_u                = !p._1
//    def __sigaction_u_=(value: union___sigaction_u): Unit = !p._1 = value
//    def sa_tramp
//      : CFunctionPtr5[Ptr[Byte], CInt, CInt, Ptr[siginfo_t], Ptr[Byte], Unit] =
//      !p._2
//    def sa_tramp_=(
//        value: CFunctionPtr5[Ptr[Byte],
//                             CInt,
//                             CInt,
//                             Ptr[siginfo_t],
//                             Ptr[Byte],
//                             Unit]): Unit = !p._2 = value
//    def sa_mask: sigset_t                 = !p._3
//    def sa_mask_=(value: sigset_t): Unit  = !p._3 = value
//    def sa_flags: CInt                    = !p._4
//    def sa_flags_=(value: CInt): Unit     = !p._4 = value
//  }
//
//  def struct___sigaction()(implicit z: Zone): Ptr[struct___sigaction] =
//    alloc[struct___sigaction]
//
//  implicit class struct_sigaction_ops(val p: Ptr[struct_sigaction])
//      extends AnyVal {
//    def __sigaction_u: union___sigaction_u                = !p._1
//    def __sigaction_u_=(value: union___sigaction_u): Unit = !p._1 = value
//    def sa_mask: sigset_t                                 = !p._2
//    def sa_mask_=(value: sigset_t): Unit                  = !p._2 = value
//    def sa_flags: CInt                                    = !p._3
//    def sa_flags_=(value: CInt): Unit                     = !p._3 = value
//  }
//
//  def struct_sigaction()(implicit z: Zone): Ptr[struct_sigaction] =
//    alloc[struct_sigaction]
//
//  implicit class struct_sigvec_ops(val p: Ptr[struct_sigvec]) extends AnyVal {
//    def sv_handler: CFunctionPtr1[CInt, Unit]                = !p._1
//    def sv_handler_=(value: CFunctionPtr1[CInt, Unit]): Unit = !p._1 = value
//    def sv_mask: CInt                                        = !p._2
//    def sv_mask_=(value: CInt): Unit                         = !p._2 = value
//    def sv_flags: CInt                                       = !p._3
//    def sv_flags_=(value: CInt): Unit                        = !p._3 = value
//  }
//
//  def struct_sigvec()(implicit z: Zone): Ptr[struct_sigvec] =
//    alloc[struct_sigvec]
//
//  implicit class struct_sigstack_ops(val p: Ptr[struct_sigstack])
//      extends AnyVal {
//    def ss_sp: CString                  = !p._1
//    def ss_sp_=(value: CString): Unit   = !p._1 = value
//    def ss_onstack: CInt                = !p._2
//    def ss_onstack_=(value: CInt): Unit = !p._2 = value
//  }
//
//  def struct_sigstack()(implicit z: Zone): Ptr[struct_sigstack] =
//    alloc[struct_sigstack]
//
//  implicit class union___mbstate_t_pos(val p: Ptr[union___mbstate_t])
//      extends AnyVal {
//    def __mbstate8
//      : Ptr[CArray[CChar, Nat.Digit[Nat._1, Nat.Digit[Nat._2, Nat._8]]]] =
//      p.cast[Ptr[CArray[CChar, Nat.Digit[Nat._1, Nat.Digit[Nat._2, Nat._8]]]]]
//    def __mbstate8_=(
//        value: CArray[CChar, Nat.Digit[Nat._1, Nat.Digit[Nat._2, Nat._8]]])
//      : Unit =
//      !p.cast[Ptr[
//        CArray[CChar, Nat.Digit[Nat._1, Nat.Digit[Nat._2, Nat._8]]]]] = value
//    def _mbstateL: Ptr[CLongLong]           = p.cast[Ptr[CLongLong]]
//    def _mbstateL_=(value: CLongLong): Unit = !p.cast[Ptr[CLongLong]] = value
//  }
//
//  implicit class union_sigval_pos(val p: Ptr[sigval]) extends AnyVal {
//    def sival_int: Ptr[CInt]                = p.cast[Ptr[CInt]]
//    def sival_int_=(value: CInt): Unit      = !p.cast[Ptr[CInt]] = value
//    def sival_ptr: Ptr[Ptr[Byte]]           = p.cast[Ptr[Ptr[Byte]]]
//    def sival_ptr_=(value: Ptr[Byte]): Unit = !p.cast[Ptr[Ptr[Byte]]] = value
//  }
//
//  implicit class union___sigaction_u_pos(val p: Ptr[union___sigaction_u])
//      extends AnyVal {
//    def __sa_handler: Ptr[CFunctionPtr1[CInt, Unit]] =
//      p.cast[Ptr[CFunctionPtr1[CInt, Unit]]]
//    def __sa_handler_=(value: CFunctionPtr1[CInt, Unit]): Unit =
//      !p.cast[Ptr[CFunctionPtr1[CInt, Unit]]] = value
//    def __sa_sigaction
//      : Ptr[CFunctionPtr3[CInt, Ptr[siginfo_t], Ptr[Byte], Unit]] =
//      p.cast[Ptr[CFunctionPtr3[CInt, Ptr[struct___siginfo], Ptr[Byte], Unit]]]
//    def __sa_sigaction_=(
//        value: CFunctionPtr3[CInt, Ptr[siginfo_t], Ptr[Byte], Unit]): Unit =
//      !p.cast[Ptr[
//        CFunctionPtr3[CInt, Ptr[struct___siginfo], Ptr[Byte], Unit]]] = value
//  }
//}
