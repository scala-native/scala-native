#if defined(SCALANATIVE_MULTITHREADING_ENABLED) &&                             \
    defined(SCALANATIVE_GC_USE_YIELDPOINT_TRAPS) && !defined(_WIN32)

/*
 * Implements ucontext PC redirection and tiny C helpers used by
 * SafepointPollTrampoline-{x86_64,aarch64}.S.
 *
 * Flow: SafepointTrapHandler -> prepare_redirect() -> [kernel returns] ->
 * scalanative_gc_safepoint_poll_trampoline -> run_yield() -> branch to
 * safepointResumePc. Unsupported OS/arch: prepare_redirect returns false and
 * the handler calls Synchronizer_yield() directly.
 */

#if defined(__APPLE__)
/* Darwin ucontext.h requires a POSIX feature macro before inclusion. */
#ifndef _XOPEN_SOURCE
#define _XOPEN_SOURCE 700
#endif
#elif defined(__linux__)
/* glibc: x86_64 uc_mcontext.gregs / REG_RIP are GNU extensions. With only
 * _XOPEN_SOURCE, mcontext_t has no gregs */
#ifndef _GNU_SOURCE
#define _GNU_SOURCE 1
#endif
#else
#ifndef _XOPEN_SOURCE
#define _XOPEN_SOURCE 700
#endif
#endif

#include "SafepointPollTrampoline.h"
#include "immix_commix/Synchronizer.h"
#include <stddef.h>
#include <stdint.h>
#include <ucontext.h>

#if defined(SCALANATIVE_GC_IMMIX)
#include "immix/State.h"
#include "immix/MutatorThread.h"
#elif defined(SCALANATIVE_GC_COMMIX)
#include "commix/State.h"
#include "commix/MutatorThread.h"
#else
#error "SafepointPollTrampoline requires immix or commix"
#endif

void scalanative_gc_safepoint_poll_run_yield(void) { Synchronizer_yield(); }

uintptr_t scalanative_gc_safepoint_load_resume_pc(void) {
    MutatorThread *self = currentMutatorThread;
    return (self != NULL) ? self->safepointResumePc : (uintptr_t)0;
}

void scalanative_gc_safepoint_poll_trampoline(void);

#if defined(__x86_64__)

#if defined(__APPLE__)
#define SN_UC_GET_PC(uc) ((uintptr_t)(uc)->uc_mcontext->__ss.__rip)
#define SN_UC_SET_PC(uc, addr)                                                 \
    ((uc)->uc_mcontext->__ss.__rip = (__uint64_t)(uintptr_t)(addr))
#define SN_UC_GET_SP(uc) ((uintptr_t)(uc)->uc_mcontext->__ss.__rsp)
#define SN_UC_SET_SP(uc, sp)                                                   \
    ((uc)->uc_mcontext->__ss.__rsp = (__uint64_t)(uintptr_t)(sp))
#elif defined(__linux__)
#include <signal.h>
#include <sys/reg.h>
#define SN_UC_GET_PC(uc) ((uintptr_t)(uc)->uc_mcontext.gregs[REG_RIP])
#define SN_UC_SET_PC(uc, addr)                                                 \
    ((uc)->uc_mcontext.gregs[REG_RIP] = (greg_t)(uintptr_t)(addr))
#define SN_UC_GET_SP(uc) ((uintptr_t)(uc)->uc_mcontext.gregs[REG_RSP])
#define SN_UC_SET_SP(uc, sp)                                                   \
    ((uc)->uc_mcontext.gregs[REG_RSP] = (greg_t)(uintptr_t)(sp))
#else
#define SN_UC_UNSUPPORTED 1
#endif

#elif defined(__aarch64__)

#if defined(__APPLE__)
#define SN_UC_GET_PC(uc) ((uintptr_t)(uc)->uc_mcontext->__ss.__pc)
#define SN_UC_SET_PC(uc, addr)                                                 \
    ((uc)->uc_mcontext->__ss.__pc = (__uint64_t)(uintptr_t)(addr))
#define SN_UC_GET_SP(uc) ((uintptr_t)(uc)->uc_mcontext->__ss.__sp)
#define SN_UC_SET_SP(uc, sp)                                                   \
    ((uc)->uc_mcontext->__ss.__sp = (__uint64_t)(uintptr_t)(sp))
/* Darwin arm64 lets leaf functions use 128 bytes below sp. Keep in sync with
 * SFPT_RED_ZONE in SafepointPollTrampoline-aarch64.S */
#define SN_RED_ZONE 128
#elif defined(__linux__)
#define SN_UC_GET_PC(uc) ((uintptr_t)(uc)->uc_mcontext.pc)
#define SN_UC_SET_PC(uc, addr)                                                 \
    ((uc)->uc_mcontext.pc = (uint64_t)(uintptr_t)(addr))
#else
#define SN_UC_UNSUPPORTED 1
#endif

#else
#define SN_UC_UNSUPPORTED 1
#endif

bool scalanative_gc_safepoint_is_stale_fault_signal(void *uap) {
#if defined(__APPLE__) && defined(__aarch64__)
    // Raised at a syscall (ESR class SVC): a fault left pending while masked.
    ucontext_t *uc = (ucontext_t *)uap;
    return uc != NULL && (uc->uc_mcontext->__es.__esr >> 26) == 0x15;
#else
    (void)uap;
    return false;
#endif
}

bool scalanative_gc_safepoint_prepare_redirect(void *uap, void *mutatorThread) {
#ifndef SN_UC_UNSUPPORTED
    MutatorThread *self = (MutatorThread *)mutatorThread;
    if (uap == NULL || self == NULL)
        return false;
    ucontext_t *uc = (ucontext_t *)uap;
    uintptr_t pc = SN_UC_GET_PC(uc);
    self->safepointResumePc = pc;
#if defined(__x86_64__)
    uintptr_t sp = SN_UC_GET_SP(uc) - 128 - sizeof(uintptr_t);
    *(uintptr_t *)sp = pc;
    SN_UC_SET_SP(uc, sp);
#elif defined(__aarch64__) && defined(SN_RED_ZONE)
    SN_UC_SET_SP(uc, SN_UC_GET_SP(uc) - SN_RED_ZONE);
#endif
    SN_UC_SET_PC(uc, (void *)scalanative_gc_safepoint_poll_trampoline);
    return true;
#else
    (void)uap;
    (void)mutatorThread;
    return false;
#endif
}

#else /* !multithreading || !traps || windows */

typedef int scalanative_gc_safepoint_poll_trampoline_unused_typedef;

#endif
