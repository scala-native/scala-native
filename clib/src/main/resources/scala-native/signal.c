#include <signal.h>

typedef void (*sig_handler_t)(int);

sig_handler_t scalanative_sig_dfl() { return SIG_DFL; }

sig_handler_t scalanative_sig_ign() { return SIG_IGN; }

sig_handler_t scalanative_sig_err() { return SIG_ERR; }

int scalanative_sigabrt() { return SIGABRT; }

int scalanative_sigfpe() { return SIGFPE; }

int scalanative_sigill() { return SIGILL; }

int scalanative_sigint() { return SIGINT; }

int scalanative_sigsegv() { return SIGSEGV; }

int scalanative_sigterm() { return SIGTERM; }
