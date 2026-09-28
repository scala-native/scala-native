#include <assert.h>
#include <stdlib.h>
#ifndef _WIN32
#include <signal.h>
#include <unistd.h>

static struct sigaction previous;

/* Reproduce LLVM's behavior outside a CrashRecoveryContext: restore the
 * previous handler and raise again, losing the original siginfo/context. */
static void intercept(int sig) {
    sigaction(sig, &previous, NULL);
    raise(sig);
}

void installSignalInterceptor(void) {
    alarm(60);
    struct sigaction action = {0};
    action.sa_handler = intercept;
    sigemptyset(&action.sa_mask);
    if (sigaction(SIGSEGV, &action, &previous) != 0)
        abort();
}

void checkSignalInterceptor(void) {
    struct sigaction action;
    if (sigaction(SIGSEGV, NULL, &action) != 0)
        abort();
    if (action.sa_handler != intercept)
        abort();
    alarm(0);
}
#else
void installSignalInterceptor(void) {}
void checkSignalInterceptor(void) {}
#endif
