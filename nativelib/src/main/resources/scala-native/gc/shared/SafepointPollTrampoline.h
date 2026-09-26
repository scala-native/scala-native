#ifndef SAFEPOINT_POLL_TRAMPOLINE_H
#define SAFEPOINT_POLL_TRAMPOLINE_H

/*
 * Deferred safepoint trampoline (POSIX, Immix/Commix, trap yield points).
 *
 * The SIGSEGV/SIGBUS handler must not run Synchronizer_yield() in async-signal
 * context. prepare_redirect() stores the faulting PC on the MutatorThread and
 * points ucontext at scalanative_gc_safepoint_poll_trampoline (asm); that stub
 * saves/restores registers, calls Synchronizer_yield(), then resumes at the
 * saved PC. See docs/contrib/gc-safepoint-trampoline.md.
 */

#include <stdbool.h>
#include <stdint.h>

#if defined(SCALANATIVE_MULTITHREADING_ENABLED) &&                             \
    defined(SCALANATIVE_GC_USE_YIELDPOINT_TRAPS) && !defined(_WIN32)

/* Entry from modified ucontext PC after a trap fault; implemented in .S */
void scalanative_gc_safepoint_poll_trampoline(void);

/* uap: third argument to SA_SIGINFO handler (ucontext_t *). mutatorThread:
 * MutatorThread * (void * to keep this header independent of GC typedefs). */
bool scalanative_gc_safepoint_prepare_redirect(void *uap, void *mutatorThread);

void scalanative_gc_safepoint_poll_run_yield(void);
uintptr_t scalanative_gc_safepoint_load_resume_pc(void);

#else

static inline bool
scalanative_gc_safepoint_prepare_redirect(void *uap, void *mutatorThread) {
    (void)uap;
    (void)mutatorThread;
    return false;
}

#endif

#endif
