#include <signal.h>

// symbolic constants
void *scalanative_sig_dfl(int s) { return SIG_DFL; }
void *scalanative_sig_err(int s) { return SIG_ERR; }
// void *scalanative_sig_hold(int s) { return SIG_HOLD; }
void *scalanative_sig_ign(int s) { return SIG_IGN; }

int scalanative_sigev_none() { return SIGEV_NONE; }
int scalanative_sigev_signal() { return SIGEV_SIGNAL; }
int scalanative_sigev_thread() { return SIGEV_THREAD; }

int scalanative_sigabrt() { return SIGABRT; }
int scalanative_sigalrm() { return SIGALRM; }
int scalanative_sigbus() { return SIGBUS; }
int scalanative_sigchld() { return SIGCHLD; }
int scalanative_sigcont() { return SIGCONT; }
int scalanative_sigfpe() { return SIGFPE; }
int scalanative_sighup() { return SIGHUP; }
int scalanative_sigill() { return SIGILL; }
int scalanative_sigint() { return SIGINT; }
int scalanative_sigkill() { return SIGKILL; }
int scalanative_sigpipe() { return SIGPIPE; }
int scalanative_sigquit() { return SIGQUIT; }
int scalanative_sigsegv() { return SIGSEGV; }
int scalanative_sigstop() { return SIGSTOP; }
int scalanative_sigterm() { return SIGTERM; }
int scalanative_sigtstp() { return SIGTSTP; }
int scalanative_sigttin() { return SIGTTIN; }
int scalanative_sigttou() { return SIGTTOU; }
int scalanative_sigusr1() { return SIGUSR1; }
int scalanative_sigusr2() { return SIGUSR2; }
// int scalanative_sigpoll() { return SIGPOLL; }
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

// define the following symbolic constants
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

// scala native functions
