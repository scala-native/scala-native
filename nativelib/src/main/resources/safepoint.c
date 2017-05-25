#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/mman.h>

extern unsigned char scalanative_safepoint_trigger[4096]
    __attribute__((aligned(4096)));

void scalanative_safepoint() {}

void scalanative_safepoint_on() {
    mprotect((void *)&scalanative_safepoint_trigger, 4096, PROT_NONE);
}

void scalanative_safepoint_off() {
    mprotect((void *)&scalanative_safepoint_trigger, 4096, PROT_READ);
}

void scalanative_safepoint_handler(int sig, siginfo_t *info, void *ucontext) {
    if (info->si_addr == (void *)&scalanative_safepoint_trigger) {
        scalanative_safepoint();
    } else {
        printf("Segfault %d at %p", sig, info->si_addr);
        exit(1);
    }
}

void scalanative_safepoint_init() {
    struct sigaction action;
    action.sa_sigaction = scalanative_safepoint_handler;
    action.sa_flags = SA_RESTART | SA_SIGINFO;
    sigemptyset(&action.sa_mask);
    sigaction(SIGSEGV, &action, NULL);
    sigaction(SIGBUS, &action, NULL);
}
