package scala.scalanative
package native

import scala.scalanative.posix.sys.types.{pid_t, pthread_attr_t, uid_t}
import scala.scalanative.posix.time.timespec

// http://pubs.opengroup.org/onlinepubs/9699919799/

@extern
object signal {

  def kill(pid: pid_t, sig: CInt): CInt = extern

  def killpg(pid: pid_t, sig: CInt): CInt = extern

  def psiginfo(pinfo: Ptr[siginfo_t], s: CString): Unit = extern

  def psignal(sig: CInt, s: CString): Unit = extern

  // pthread signals here

  def raise(sig: CInt): CInt = extern

  def sigaction(signum: CInt, act: Ptr[sigaction], oldact: Ptr[sigaction]): CInt = extern

  def sigaddset(set: Ptr[sigset_t], signum: CInt): CInt = extern

  def sigaltstack(ss: Ptr[stack_t], oss: Ptr[stack_t]): CInt = extern

  def sigdelset(set: Ptr[sigset_t], sig: CInt): CInt = extern

  def sigemptyset(set: Ptr[sigset_t]): CInt = extern

  def sigfillset(set: Ptr[sigset_t]): CInt = extern

  def sighold(sig: CInt): CInt = extern

  def sigignore(sig: CInt): CInt = extern

  def siginterrupt(sig: CInt, flag: CInt): CInt = extern

  def sigismember(set: Ptr[sigset_t], signum: CInt): CInt = extern

  def signal(sig: CInt,
             handler: CFunctionPtr1[CInt, Unit]): CFunctionPtr1[CInt, Unit] =
    extern

  def sigpause(sig: CInt): CInt = extern

  def sigpending(set: Ptr[sigset_t]): CInt = extern

  def sigprocmask(how: CInt, set: Ptr[sigset_t], oldset: Ptr[sigset_t]): CInt = extern

  def sigqueue(pid: pid_t, sig: CInt, value: sigval): CInt = extern

  def sigrelse(set: Ptr[sigset_t]): CInt = extern

  def sigset(sig: CInt, handler: CFunctionPtr1[CInt, Unit]): CFunctionPtr1[CInt, Unit] = extern

  def sigsuspend(set: Ptr[sigset_t]): CInt = extern

  def sigtimedwait(set: Ptr[sigset_t], info: Ptr[siginfo_t], timeout: Ptr[timespec]): CInt = extern

  def sigwait(set: Ptr[sigset_t], sig: Ptr[CInt]): CInt = extern

  def sigwaitinfo(set: Ptr[sigset_t], info: Ptr[siginfo_t]): CInt = extern

  // Types

  type mcontext_t = CStruct0
  type sig_atomic_t = CInt
  type sigset_t = CInt
  type sigval = Ptr[Byte]
  type sigevent =
    CStruct5[CInt, CInt, sigval, CFunctionPtr1[sigval, Unit], Ptr[pthread_attr_t]]
  type sigaction = CStruct4[Ptr[Byte], sigset_t, CInt, Ptr[Byte]]
  type stack_t = CStruct3[Ptr[Byte], CSize, CInt]
  type ucontext_t = CStruct4[Ptr[ucontext_t], sigset_t, stack_t, mcontext_t]
  type siginfo_t = CStruct9[CInt, CInt, CInt, pid_t, uid_t, Ptr[Byte], CInt, CLong, sigval]

  // Macros

  @name("scalanative_libc_sig_dfl")
  def SIG_DFL: CFunctionPtr1[CInt, Unit] = extern

  @name("scalanative_libc_sig_err")
  def SIG_ERR: CFunctionPtr1[CInt, Unit] = extern

  @name("scalanative_libc_sig_hold")
  def SIG_HOLD: CFunctionPtr1[CInt, Unit] = extern

  @name("scalanative_libc_sig_ign")
  def SIG_IGN: CFunctionPtr1[CInt, Unit] = extern

  @name("scalanative_libc_sigev_none")
  def SIGEV_NONE: CInt = extern

  @name("scalanative_libc_sigev_signal")
  def SIGEV_SIGNAL: CInt = extern

  @name("scalanative_libc_sigev_none")
  def SIGEV_THREAD: CInt = extern

  @name("scalanative_libc_sigabrt")
  def SIGABRT: CInt = extern

  @name("scalanative_libc_sigalrm")
  def SIGALRM: CInt = extern

  @name("scalanative_libc_sigbus")
  def SIGBUS: CInt = extern

  @name("scalanative_libc_sigchld")
  def SIGCHLD: CInt = extern

  @name("scalanative_libc_sigcont")
  def SIGCONT: CInt = extern

  @name("scalanative_libc_sigfpe")
  def SIGFPE: CInt = extern

  @name("scalanative_libc_sighup")
  def SIGHUP: CInt = extern

  @name("scalanative_libc_sigill")
  def SIGILL: CInt = extern

  @name("scalanative_libc_sigint")
  def SIGINT: CInt = extern

  @name("scalanative_libc_sigkill")
  def SIGKILL: CInt = extern

  @name("scalanative_libc_sigpipe")
  def SIGPIPE: CInt = extern

  @name("scalanative_libc_sigquit")
  def SIGQUIT: CInt = extern

  @name("scalanative_libc_sigsegv")
  def SIGSEGV: CInt = extern

  @name("scalanative_libc_sigstop")
  def SIGSTOP: CInt = extern

  @name("scalanative_libc_sigterm")
  def SIGTERM: CInt = extern

  @name("scalanative_libc_sigtstp")
  def SIGTSTP: CInt = extern

  @name("scalanative_libc_sigttin")
  def SIGTTIN: CInt = extern

  @name("scalanative_libc_sigttou")
  def SIGTTOU: CInt = extern

  @name("scalanative_libc_sigusr1")
  def SIGUSR1: CInt = extern

  @name("scalanative_libc_sigusr2")
  def SIGUSR2: CInt = extern

  @name("scalanative_libc_sigpoll")
  def SIGPOLL: CInt = extern

  @name("scalanative_libc_sigprof")
  def SIGPROF: CInt = extern

  @name("scalanative_libc_sigsys")
  def SIGSYS: CInt = extern

  @name("scalanative_libc_sigtrap")
  def SIGTRAP: CInt = extern

  @name("scalanative_libc_sigurg")
  def SIGURG: CInt = extern

  @name("scalanative_libc_sigvtalrm")
  def SIGVTALRM: CInt = extern

  @name("scalanative_libc_sigxcpu")
  def SIGXCPU: CInt = extern

  @name("scalanative_libc_sigxfsz")
  def SIGXFSZ: CInt = extern

  @name("scalanative_libc_sig_block")
  def SIG_BLOCK: CInt = extern

  @name("scalanative_libc_sig_unblock")
  def SIG_UNBLOCK: CInt = extern

  @name("scalanative_libc_sig_setmask")
  def SIG_SETMASK: CInt = extern

  @name("scalanative_libc_sa_nocldstop")
  def SA_NOCLDSTOP: CInt = extern

  @name("scalanative_libc_sa_onstack")
  def SA_ONSTACK: CInt = extern

  @name("scalanative_libc_sa_resethand")
  def SA_RESETHAND: CInt = extern

  @name("scalanative_libc_sa_restart")
  def SA_RESTART: CInt = extern

  @name("scalanative_libc_sa_siginfo")
  def SA_SIGINFO: CInt = extern

  @name("scalanative_libc_sa_nocldwait")
  def SA_NOCLDWAIT: CInt = extern

  @name("scalanative_libc_sa_nodefer")
  def SA_NODEFER: CInt = extern

  @name("scalanative_libc_ss_onstack")
  def SS_ONSTACK: CInt = extern

  @name("scalanative_libc_ss_disable")
  def SS_DISABLE: CInt = extern

  @name("scalanative_libc_minsigstksz")
  def MINSIGSTKSZ: CInt = extern

  @name("scalanative_libc_sigstksz")
  def SIGSTKSZ: CInt = extern

  @name("scalanative_libc_ill_illopc")
  def ILL_ILLOPC: CInt = extern

  @name("scalanative_libc_ill_illopn")
  def ILL_ILLOPN: CInt = extern

  @name("scalanative_libc_ill_illadr")
  def ILL_ILLADR: CInt = extern

  @name("scalanative_libc_ill_illtrp")
  def ILL_ILLTRP: CInt = extern

  @name("scalanative_libc_ill_prvopc")
  def ILL_PRVOPC: CInt = extern

  @name("scalanative_libc_ill_prvreg")
  def ILL_PRVREG: CInt = extern

  @name("scalanative_libc_ill_coproc")
  def ILL_COPROC: CInt = extern

  @name("scalanative_libc_ill_badstk")
  def ILL_BADSTK: CInt = extern

  @name("scalanative_libc_fpe_intdiv")
  def FPE_INTDIV: CInt = extern

  @name("scalanative_libc_fpe_intovf")
  def FPE_INTOVF: CInt = extern

  @name("scalanative_libc_fpe_fltdiv")
  def FPE_FLTDIV: CInt = extern

  @name("scalanative_libc_fpe_fltovf")
  def FPE_FLTOVF: CInt = extern

  @name("scalanative_libc_fpe_fltund")
  def FPE_FLTUND: CInt = extern

  @name("scalanative_libc_fpe_fltres")
  def FPE_FLTRES: CInt = extern

  @name("scalanative_libc_fpe_fltinv")
  def FPE_FLTINV: CInt = extern

  @name("scalanative_libc_fpe_fltsub")
  def FPE_FLTSUB: CInt = extern

  @name("scalanative_libc_segv_maperr")
  def SEGV_MAPERR: CInt = extern

  @name("scalanative_libc_segv_accerr")
  def SEGV_ACCERR: CInt = extern

  @name("scalanative_libc_bus_adraln")
  def BUS_ADRALN: CInt = extern

  @name("scalanative_libc_bus_adrerr")
  def BUS_ADRERR: CInt = extern

  @name("scalanative_libc_bus_objerr")
  def BUS_OBJERR: CInt = extern

  @name("scalanative_libc_trap_brkpt")
  def TRAP_BRKPT: CInt = extern

  @name("scalanative_libc_trap_trace")
  def TRAP_TRACE: CInt = extern

  @name("scalanative_libc_cld_exited")
  def CLD_EXITED: CInt = extern

  @name("scalanative_libc_cld_killed")
  def CLD_KILLED: CInt = extern

  @name("scalanative_libc_cld_dumped")
  def CLD_DUMPED: CInt = extern

  @name("scalanative_libc_cld_trapped")
  def CLD_TRAPPED: CInt = extern

  @name("scalanative_libc_cld_stopped")
  def CLD_STOPPED: CInt = extern

  @name("scalanative_libc_cld_continued")
  def CLD_CONTINUED: CInt = extern

  @name("scalanative_libc_poll_in")
  def POLL_IN: CInt = extern

  @name("scalanative_libc_poll_out")
  def POLL_OUT: CInt = extern

  @name("scalanative_libc_poll_msg")
  def POLL_MSG: CInt = extern

  @name("scalanative_libc_poll_err")
  def POLL_ERR: CInt = extern

  @name("scalanative_libc_poll_pri")
  def POLL_PRI: CInt = extern

  @name("scalanative_libc_poll_hup")
  def POLL_HUP: CInt = extern

  @name("scalanative_libc_si_user")
  def SI_USER: CInt = extern

  @name("scalanative_libc_si_queue")
  def SI_QUEUE: CInt = extern

  @name("scalanative_libc_si_timer")
  def SI_TIMER: CInt = extern

  @name("scalanative_libc_si_asyncio")
  def SI_ASYNCIO: CInt = extern

  @name("scalanative_libc_si_mesgq")
  def SI_MESGQ: CInt = extern
}