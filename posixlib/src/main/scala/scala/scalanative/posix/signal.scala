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
    CInt, // sigev_notify Notification type
    CInt, // sigev_signo Signal number
    Ptr[sigval], // sigev_value Signal value (Ptr instead of value)
    CFunctionPtr1[Ptr[sigval], Unit], // sigev_notify_function Notification function (Ptr instead of value for sigval)
    Ptr[pthread_attr_t] // sigev_notify_attributes Notification attributes
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

  // The storage occupied by sa_handler and sa_sigaction may overlap,
  // and a conforming application shall not use both simultaneously.
  type sigaction = CStruct4[
    CFunctionPtr1[CInt, Unit], // sa_handler Ptr to a signal-catching function or one of the SIG_IGN or SIG_DFL
    sigset_t, // sa_mask Set of signals to be blocked during execution of the signal handling func
    CInt, // sa_flags Special flags
    CFunctionPtr3[CInt, Ptr[siginfo_t], Ptr[Byte], Unit] // sa_sigaction Pointer to a signal-catching function
  ]

  // define the following macros which shall expand to integer constant expressions
  // that need not be usable in #if preprocessing directives
  @name("scalanative_sig_block")
  def SIG_BLOCK: CInt = extern
  @name("scalanative_sig_unblock")
  def SIG_UNBLOCK: CInt = extern
  @name("scalanative_sig_setmask")
  def SIG_SETMASK: CInt = extern

  // define the following symbolic constants
  @name("scalanative_sa_nocldstop")
  def SA_NOCLDSTOP: CInt = extern
  @name("scalanative_sa_onstack")
  def SA_ONSTACK: CInt = extern
  @name("scalanative_sa_resethand")
  def SA_RESETHAND: CInt = extern
  @name("scalanative_sa_restart")
  def SA_RESTART: CInt = extern
  @name("scalanative_sa_siginfo")
  def SA_SIGINFO: CInt = extern
  @name("scalanative_sa_nocldwait")
  def SA_NOCLDWAIT: CInt = extern
  @name("scalanative_sa_nodefer")
  def SA_NODEFER: CInt = extern
  @name("scalanative_ss_onstack")
  def SS_ONSTACK: CInt = extern
  @name("scalanative_ss_disable")
  def SS_DISABLE: CInt = extern
  @name("scalanative_minsigstksz")
  def MINSIGSTKSZ: CInt = extern
  @name("scalanative_sigstksz")
  def SIGSTKSZ: CInt = extern

  // A machine-specific representation of the saved context
  // mac OS type mcontext_t = Ptr[__darwin_mcontext64]
  // __darwin_mcontext64 -> _STRUCT_MCONTEXT64 -> typedef _STRUCT_MCONTEXT64	*mcontext_t;
  type mcontext_t = Ptr[Byte]

  type ucontext_t = CStruct4[
    Ptr[Byte], // ucontext_t *uc_link Ptr to the context that is resumed when this context returns (Ptr instead)
    sigset_t, // c_sigmask The set of signals that are blocked when this context is active
    Ptr[stack_t], // uc_stack The stack used by this context (Ptr instead of value)
    mcontext_t // uc_mcontext A machine-specific representation of the saved context
  ]

  type stack_t = CStruct3[
    Ptr[Byte], // void *ss_sp Stack base or pointer
    size_t, // ss_size Stack size
    CInt // ss_flags Flags
  ]

  type siginfo_t = CStruct9[
    CInt, // si_signo Signal number
    CInt, // si_code Signal code
    CInt, // si_errno If non-zero, an errno value associated with this signal, as described in <errno.h>
    pid_t, // si_pid Sending process ID
    uid_t, // si_uid Real user ID of sending process
    Ptr[Byte], // void *si_addr Address of faulting instruction (Ptr instead - is void * a fcn?)
    CInt, // si_status Exit value or signal
    CLong, // si_band Band event for SIGPOLL
    Ptr[sigval] // si_value Signal value (Ptr instead of value)
  ]

  // define the symbolic constants in the Code column of the following table for use as values of si_code
  // that are signal-specific or non-signal-specific reasons why the signal was generated
  @name("scalanative_ill_illopc")
  def ILL_ILLOPC: CInt = extern
  @name("scalanative_ill_illopn")
  def ILL_ILLOPN: CInt = extern
  @name("scalanative_ill_illadr")
  def ILL_ILLADR: CInt = extern
  @name("scalanative_ill_illtrp")
  def ILL_ILLTRP: CInt = extern
  @name("scalanative_ill_prvopc")
  def ILL_PRVOPC: CInt = extern
  @name("scalanative_ill_prvreg")
  def ILL_PRVREG: CInt = extern
  @name("scalanative_ill_coproc")
  def ILL_COPROC: CInt = extern
  @name("scalanative_ill_badstk")
  def ILL_BADSTK: CInt = extern
  @name("scalanative_fpe_intdiv")
  def FPE_INTDIV: CInt = extern
  @name("scalanative_fpe_intovf")
  def FPE_INTOVF: CInt = extern
  @name("scalanative_fpe_fltdiv")
  def FPE_FLTDIV: CInt = extern
  @name("scalanative_fpe_fltovf")
  def FPE_FLTOVF: CInt = extern
  @name("scalanative_fpe_fltund")
  def FPE_FLTUND: CInt = extern
  @name("scalanative_fpe_fltres")
  def FPE_FLTRES: CInt = extern
  @name("scalanative_fpe_fltinv")
  def FPE_FLTINV: CInt = extern
  @name("scalanative_fpe_fltsub")
  def FPE_FLTSUB: CInt = extern
  @name("scalanative_segv_maperr")
  def SEGV_MAPERR: CInt = extern
  @name("scalanative_segv_accerr")
  def SEGV_ACCERR: CInt = extern
  @name("scalanative_bus_adraln")
  def BUS_ADRALN: CInt = extern
  @name("scalanative_bus_adrerr")
  def BUS_ADRERR: CInt = extern
  @name("scalanative_bus_objerr")
  def BUS_OBJERR: CInt = extern
  @name("scalanative_trap_brkpt")
  def TRAP_BRKPT: CInt = extern
  @name("scalanative_trap_trace")
  def TRAP_TRACE: CInt = extern
  @name("scalanative_cld_exited")
  def CLD_EXITED: CInt = extern
  @name("scalanative_cld_killed")
  def CLD_KILLED: CInt = extern
  @name("scalanative_cld_dumped")
  def CLD_DUMPED: CInt = extern
  @name("scalanative_cld_trapped")
  def CLD_TRAPPED: CInt = extern
  @name("scalanative_cld_stopped")
  def CLD_STOPPED: CInt = extern
  @name("scalanative_cld_continued")
  def CLD_CONTINUED: CInt = extern
  @name("scalanative_poll_in")
  def POLL_IN: CInt = extern
  @name("scalanative_poll_out")
  def POLL_OUT: CInt = extern
  @name("scalanative_poll_msg")
  def POLL_MSG: CInt = extern
  @name("scalanative_poll_err")
  def POLL_ERR: CInt = extern
  @name("scalanative_poll_pri")
  def POLL_PRI: CInt = extern
  @name("scalanative_poll_hup")
  def POLL_HUP: CInt = extern
  @name("scalanative_si_user")
  def SI_USER: CInt = extern
  @name("scalanative_si_queue")
  def SI_QUEUE: CInt = extern
  @name("scalanative_si_timer")
  def SI_TIMER: CInt = extern
  @name("scalanative_si_asyncio")
  def SI_ASYNCIO: CInt = extern
  @name("scalanative_si_mesgq")
  def SI_MESGQ: CInt = extern

  // The following shall be declared as functions and may also be defined as macros.
  // Function prototypes shall be provided
  // Note: sigset_t is already a pointer above
  def kill(p0: pid_t, p1: CInt): CInt                 = extern
  def killpg(p0: pid_t, p1: CInt): CInt               = extern
  def psiginfo(p0: Ptr[siginfo_t], p1: CString): Unit = extern
  def psignal(p0: CInt, p1: CString): Unit            = extern
  def pthread_kill(p0: pthread_t, p1: CInt): CInt     = extern
  def pthread_sigmask(p0: CInt, p1: Ptr[sigset_t], p2: Ptr[sigset_t]): CInt =
    extern
  def raise(p0: CInt): CInt                                             = extern
  def sigaction(p0: CInt, p1: Ptr[sigaction], p2: Ptr[sigaction]): CInt = extern
  def sigaddset(p0: Ptr[sigset_t], p1: CInt): CInt                      = extern
  def sigaltstack(p0: Ptr[stack_t], p1: Ptr[stack_t]): CInt             = extern
  def sigdelset(p0: Ptr[sigset_t], p1: CInt): CInt                      = extern
  def sigemptyset(p0: Ptr[sigset_t]): CInt                              = extern
  def sigfillset(p0: Ptr[sigset_t]): CInt                               = extern
  def sighold(p0: CInt): CInt                                           = extern
  def sigignore(p0: CInt): CInt                                         = extern
  def siginterrupt(p0: CInt, p1: CInt): CInt                            = extern
  def sigismember(p0: Ptr[sigset_t], p1: CInt): CInt                    = extern
  def signal(p0: CInt,
             p1: CFunctionPtr1[CInt, Unit]): CFunctionPtr1[CInt, Unit]  = extern
  def sigpause(p0: CInt): CInt                                          = extern
  def sigpending(p0: Ptr[sigset_t]): CInt                               = extern
  def sigprocmask(p0: CInt, p1: Ptr[sigset_t], p2: Ptr[sigset_t]): CInt = extern
  def sigqueue(p0: pid_t, p1: CInt, p2: Ptr[sigval]): CInt              = extern
  def sigrelse(p0: CInt): CInt                                          = extern
  def sigset(p0: CInt,
             p1: CFunctionPtr1[CInt, Unit]): CFunctionPtr1[CInt, Unit] = extern
  def sigsuspend(p0: Ptr[sigset_t]): CInt                              = extern
  def sigtimedwait(p0: Ptr[sigset_t],
                   p1: Ptr[siginfo_t],
                   p2: Ptr[timespec]): CInt                    = extern
  def sigwait(p0: Ptr[sigset_t], p1: Ptr[CInt]): CInt          = extern
  def sigwaitinfo(p0: Ptr[sigset_t], p1: Ptr[siginfo_t]): CInt = extern
}

object signalOps {
  import signal._

  implicit class sigevent_ops(val p: Ptr[sigevent]) extends AnyVal {
    def sigev_notify: CInt                                      = !p._1
    def sigev_notify_=(value: CInt): Unit                       = !p._1 = value
    def sigev_signo: CInt                                       = !p._2
    def sigev_signo_=(value: CInt): Unit                        = !p._2 = value
    def sigev_value: Ptr[sigval]                                = !p._3
    def sigev_value_=(value: Ptr[sigval]): Unit                 = !p._3 = value
    def sigev_notify_function: CFunctionPtr1[Ptr[sigval], Unit] = !p._4
    def sigev_notify_function_=(value: CFunctionPtr1[Ptr[sigval], Unit]): Unit =
      !p._4 = value
    def sigev_notify_attributes: Ptr[pthread_attr_t] = !p._5
    def sigev_notify_attributes_=(value: Ptr[pthread_attr_t]): Unit =
      !p._5 = value
  }

  def struct_sigevent()(implicit z: Zone): Ptr[sigevent] = alloc[sigevent]

  implicit class sigval_ops(val p: Ptr[sigval]) extends AnyVal {
    def sival_int: Ptr[CInt]                = p.cast[Ptr[CInt]]
    def sival_int_=(value: CInt): Unit      = !p.cast[Ptr[CInt]] = value
    def sival_ptr: Ptr[Ptr[Byte]]           = p.cast[Ptr[Ptr[Byte]]]
    def sival_ptr_=(value: Ptr[Byte]): Unit = !p.cast[Ptr[Ptr[Byte]]] = value
  }

  def union_sigval()(implicit z: Zone): Ptr[sigval] = alloc[sigval]
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
//  def struct_siginfo()(implicit z: Zone): Ptr[siginfo_t] = alloc[siginfo_t]
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
}
