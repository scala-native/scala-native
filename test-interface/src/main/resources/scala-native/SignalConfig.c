#include <signal.h>
#include <stdio.h>
#include <stdlib.h>

#define YELLOW "\033[0;33m"
#define RESET "\033[0;0m"

// Defines to accomodate diffrent unix-based operating systems.
// Select signals are only defined on select distributions.
#ifndef SIGINFO
#define SIGINFO 0
#endif
#ifndef SIGCLD
#define SIGCLD 0
#endif
#ifndef SIGURG
#define SIGURG 0
#endif
#ifndef SIGWINCH
#define SIGWINCH 0
#endif

/* Default handler for signals like SIGSEGV etc.
 * Since it has to be able to handle segmentation faults, it has to
 * exit the program on call, otherwise it will keep being called indefinetely.
 * This and IO operations in handlers leading to unspecified behavior means we
 * can only communicate through exitcode values. In bash programs, exitcodes >
 * 128 signify fatal signals n, where n = exitcode - 128. This is the
 * convention thst is being used here.
 */
void default_handler(int sig) { exit(128 + sig); }

void set_handler(int sig) {
    if (signal(sig, default_handler) != 0)
        printf("[%swarn%s] Failed to register TestMain "
               "default signal handler for "
               "signal: %d\n",
               YELLOW, RESET, sig);
}

void scalanative_set_default_handlers() {
    // Works on unix-based systems only.
    // On windows, only select "signals" are implemented.
    for (int sig = 1; sig < 32; sig++) {
        if (sig != SIGKILL && sig != SIGURG && sig != SIGSTOP &&
            sig != SIGTSTP && sig != SIGCONT && sig != SIGCHLD &&
            sig != SIGWINCH && sig != SIGINFO && sig != SIGCLD)
            set_handler(sig);
    }
}