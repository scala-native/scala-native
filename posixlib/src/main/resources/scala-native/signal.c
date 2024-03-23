#if defined(__SCALANATIVE_POSIX_SIGNAL)
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
#include <signal.h>

#ifdef __OpenBSD__

// OpenBSD doesn't implenent SIGEB_ signals, use 0 instead
#define SIGEV_NONE 0
#define SIGEV_SIGNAL 0
#define SIGEV_THREAD 0

// nor SIGPOLL
#define POLL_IN 0
#define POLL_OUT 0
#define POLL_MSG 0
#define POLL_ERR 0
#define POLL_PRI 0
#define POLL_HUP 0
#define NSIGPOLL 0

// nor SIGPROF
#define PROF_SIG 0
#define NSIGPROF 0

// SI_ASYNCIO and SI_MESGQ are missed as well
#define SI_ASYNCIO 0
#define SI_MESGQ 0
#endif

// symbolic constants -  see signal.scala
// some missing are deprecated or not supported
// others missing can be found in clib

int scalanative_sigev_none() { return SIGEV_NONE; }
int scalanative_sigev_signal() { return SIGEV_SIGNAL; }
int scalanative_sigev_thread() { return SIGEV_THREAD; }

int scalanative_sigalrm() { return SIGALRM; }
int scalanative_sigbus() { return SIGBUS; }
int scalanative_sigchld() { return SIGCHLD; }
int scalanative_sigcont() { return SIGCONT; }
int scalanative_sighup() { return SIGHUP; }
int scalanative_sigkill() { return SIGKILL; }
int scalanative_sigpipe() { return SIGPIPE; }
int scalanative_sigquit() { return SIGQUIT; }
int scalanative_sigstop() { return SIGSTOP; }
int scalanative_sigtstp() { return SIGTSTP; }
int scalanative_sigttin() { return SIGTTIN; }
int scalanative_sigttou() { return SIGTTOU; }
int scalanative_sigusr1() { return SIGUSR1; }
int scalanative_sigusr2() { return SIGUSR2; }

int scalanative_sigprof() { return SIGPROF; }
int scalanative_sigsys() { return SIGSYS; }
int scalanative_sigtrap() { return SIGTRAP; }
int scalanative_sigurg() { return SIGURG; }
int scalanative_sigtalrm() { return SIGVTALRM; }
int scalanative_sigxcpu() { return SIGXCPU; }
int scalanative_sigxfsz() { return SIGXFSZ; }

int scalanative_sig_block() { return SIG_BLOCK; }
int scalanative_sig_unblock() { return SIG_UNBLOCK; }
int scalanative_sig_setmask() { return SIG_SETMASK; }

int scalanative_sa_nocldstop() { return SA_NOCLDSTOP; }
int scalanative_sa_onstack() { return SA_ONSTACK; }
int scalanative_sa_resethand() { return SA_RESETHAND; }
int scalanative_sa_restart() { return SA_RESTART; }
int scalanative_sa_siginfo() { return SA_SIGINFO; }
int scalanative_sa_nocldwait() { return SA_NOCLDWAIT; }
int scalanative_sa_nodefer() { return SA_NODEFER; }
int scalanative_ss_onstack() { return SS_ONSTACK; }
int scalanative_ss_disable() { return SS_DISABLE; }
int scalanative_minsigstksz() { return MINSIGSTKSZ; }
int scalanative_sigstksz() { return SIGSTKSZ; }

int scalanative_ill_illopc() { return ILL_ILLOPC; }
int scalanative_ill_illopn() { return ILL_ILLOPN; }
int scalanative_ill_illadr() { return ILL_ILLADR; }
int scalanative_ill_illtrp() { return ILL_ILLTRP; }
int scalanative_ill_prvopc() { return ILL_PRVOPC; }
int scalanative_ill_prvreg() { return ILL_PRVREG; }
int scalanative_ill_coproc() { return ILL_COPROC; }
int scalanative_ill_badstk() { return ILL_BADSTK; }
int scalanative_fpe_intdiv() { return FPE_INTDIV; }
int scalanative_fpe_intovf() { return FPE_INTOVF; }
int scalanative_fpe_fltdiv() { return FPE_FLTDIV; }
int scalanative_fpe_fltovf() { return FPE_FLTOVF; }
int scalanative_fpe_fltund() { return FPE_FLTUND; }
int scalanative_fpe_fltres() { return FPE_FLTRES; }
int scalanative_fpe_fltinv() { return FPE_FLTINV; }
int scalanative_fpe_fltsub() { return FPE_FLTSUB; }
int scalanative_segv_maperr() { return SEGV_MAPERR; }
int scalanative_segv_accerr() { return SEGV_ACCERR; }
int scalanative_bus_adraln() { return BUS_ADRALN; }
int scalanative_bus_adrerr() { return BUS_ADRERR; }
int scalanative_bus_objerr() { return BUS_OBJERR; }
int scalanative_cld_exited() { return CLD_EXITED; }
int scalanative_cld_killed() { return CLD_KILLED; }
int scalanative_cld_dumped() { return CLD_DUMPED; }
int scalanative_cld_trapped() { return CLD_TRAPPED; }
int scalanative_cld_stopped() { return CLD_STOPPED; }
int scalanative_cld_continued() { return CLD_CONTINUED; }
int scalanative_poll_in() { return POLL_IN; }
int scalanative_poll_out() { return POLL_OUT; }
int scalanative_poll_msg() { return POLL_MSG; }
int scalanative_poll_err() { return POLL_ERR; }
int scalanative_poll_pri() { return POLL_PRI; }
int scalanative_poll_hup() { return POLL_HUP; }
int scalanative_si_user() { return SI_USER; }
int scalanative_si_queue() { return SI_QUEUE; }
int scalanative_si_timer() { return SI_TIMER; }
int scalanative_si_asyncio() { return SI_ASYNCIO; }
int scalanative_si_mesgq() { return SI_MESGQ; }
#endif // is Unix or MacOS
#endif