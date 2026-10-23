#ifndef SCALANATIVE_SIGNAL_DIAGNOSTICS_H
#define SCALANATIVE_SIGNAL_DIAGNOSTICS_H
#ifndef _WIN32
#include <signal.h>
#include <stdbool.h>
/* Fatal path only: no allocation, stdio, locks or unwinding. */
__attribute__((noreturn)) void
scalanative_signal_fatal(int sig, siginfo_t *info, void *context,
                         const void *mutator, const void *trap, bool stopping);
#endif
#endif
