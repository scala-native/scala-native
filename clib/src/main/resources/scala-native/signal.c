#include <signal.h>

typedef void (*sig_handler_t)(int);

sig_handler_t scalanative_libc_sig_dfl() { return SIG_DFL; }

sig_handler_t scalanative_libc_sig_ign() { return SIG_IGN; }

sig_handler_t scalanative_libc_sig_err() { return SIG_ERR; }

int scalanative_libc_sigabrt() { return SIGABRT; }

int scalanative_libc_sigfpe() { return SIGFPE; }

int scalanative_libc_sigill() { return SIGILL; }

int scalanative_libc_sigint() { return SIGINT; }

int scalanative_libc_sigsegv() { return SIGSEGV; }

int scalanative_libc_sigterm() { return SIGTERM; }

int scalanative_libc_sigusr1() { return SIGUSR1; }
