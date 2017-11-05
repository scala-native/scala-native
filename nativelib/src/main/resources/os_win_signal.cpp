#ifdef _WIN32

extern "C" {

#include "os_win_signal.h"
#include <signal.h>

extern unsigned char scalanative_safepoint_trigger[4096]
    __attribute__((aligned(4096)));

static const struct sigaction *acts[32];

void sigHandler(int signum) {
    siginfo_t info;
    info.si_addr = (void *)&scalanative_safepoint_trigger;
    acts[signum]->sa_sigaction(signum, &info, 0);
}

int sigemptyset(sigset_t *set) { return 0; }

int sigaction(int sig, const struct sigaction *act, struct sigaction *oact) {
#ifdef SCALA_NATIVE_EXPERIMENTAL_MEMORY_SAFEPOINT
    acts[sig] = act;
    switch (sig) {
    case SIGBUS:
        break;
    case SIGSEGV:
    case SIGABRT:
    case SIGILL:
    case SIGFPE:
        signal(sig, sigHandler);
        break;
    default:
        return -1;
    }
#endif
    return 0;
}
}

#endif