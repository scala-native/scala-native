#ifndef _WIN32
#if defined(__APPLE__) && !defined(_XOPEN_SOURCE)
#define _XOPEN_SOURCE 700
#endif
#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif
#include "SignalDiagnostics.h"
#include <stdint.h>
#include <stdatomic.h>
#include <unistd.h>
#include <ucontext.h>
#ifdef __linux__
#include <sys/syscall.h>
#endif

static atomic_flag reporting = ATOMIC_FLAG_INIT;

static char *text(char *p, const char *s) {
    while (*s)
        *p++ = *s++;
    return p;
}

static char *number(char *p, uintptr_t value, unsigned base) {
    char digits[sizeof(uintptr_t) * 8];
    unsigned n = 0;
    do {
        digits[n++] = "0123456789abcdef"[value % base];
        value /= base;
    } while (value);
    while (n)
        *p++ = digits[--n];
    return p;
}

static char *hexField(char *p, const char *name, uintptr_t value) {
    return number(text(text(p, name), "0x"), value, 16);
}

void scalanative_signal_fatal(int sig, siginfo_t *info, void *context,
                              const void *mutator, const void *trap,
                              bool stopping) {
    if (atomic_flag_test_and_set_explicit(&reporting, memory_order_relaxed))
        _exit(128 + sig);
    uintptr_t pc = 0, sp = 0;
    ucontext_t *uc = context;
    if (uc != NULL) {
#if defined(__linux__) && defined(__x86_64__)
        pc = uc->uc_mcontext.gregs[REG_RIP];
        sp = uc->uc_mcontext.gregs[REG_RSP];
#elif defined(__linux__) && defined(__aarch64__)
        pc = uc->uc_mcontext.pc;
        sp = uc->uc_mcontext.sp;
#elif defined(__APPLE__) && defined(__x86_64__)
        pc = uc->uc_mcontext->__ss.__rip;
        sp = uc->uc_mcontext->__ss.__rsp;
#elif defined(__APPLE__) && defined(__aarch64__)
        pc = uc->uc_mcontext->__ss.__pc;
        sp = uc->uc_mcontext->__ss.__sp;
#endif
    }
    /* Fixed fields have a bounded total size, including full-width numbers. */
    char buffer[1024];
    char *p = text(buffer, "ScalaNative: fatal signal=");
    p = number(p, (unsigned)sig, 10);
    p = text(p, " code=");
    int code = info->si_code;
    if (code < 0)
        p = text(p, "-");
    p = number(p, code < 0 ? -(intptr_t)code : code, 10);
    bool fromSender = code == SI_USER || code == SI_QUEUE;
#ifdef SI_TKILL
    fromSender = fromSender || code == SI_TKILL;
#endif
    if (fromSender) {
        p = number(text(p, " sender_pid="), info->si_pid, 10);
        p = number(text(p, " sender_uid="), info->si_uid, 10);
    } else if (code > 0 && (sig == SIGSEGV || sig == SIGBUS)) {
        p = hexField(p, " address=", (uintptr_t)info->si_addr);
    }
    p = hexField(p, " pc=", pc);
    p = hexField(p, " sp=", sp);
    p = number(text(p, " pid="), getpid(), 10);
#ifdef __linux__
    p = number(text(p, " tid="), syscall(SYS_gettid), 10);
#endif
    p = hexField(p, " mutator=", (uintptr_t)mutator);
    p = hexField(p, " trap=", (uintptr_t)trap);
    p = number(text(p, " gc_stopping="), stopping, 10);
    p = text(p, "\n");
    /* One best-effort write, without libc buffering or an unbounded retry. */
    ssize_t written = write(STDERR_FILENO, buffer, (size_t)(p - buffer));
    (void)written;

    /* Preserve the original terminating signal and allow the OS to dump core.
     * The register record above describes the original fault, not raise(). */
    struct sigaction action = {0};
    action.sa_handler = SIG_DFL;
    sigemptyset(&action.sa_mask);
    sigaction(sig, &action, NULL);
    sigset_t mask;
    sigemptyset(&mask);
    sigaddset(&mask, sig);
    sigprocmask(SIG_UNBLOCK, &mask, NULL);
    raise(sig);
    _exit(128 + sig);
}
#endif
