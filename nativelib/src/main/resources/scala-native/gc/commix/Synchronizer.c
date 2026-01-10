#if defined(SCALANATIVE_MULTITHREADING_ENABLED) &&                             \
    defined(SCALANATIVE_GC_COMMIX)

#include "string_constants.h"
#include "immix_commix/Synchronizer.h"
#include "shared/ScalaNativeGC.h"
#include "shared/Log.h"
#include <stdio.h>
#include <stdatomic.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>

#include "State.h"
#include "Settings.h"
#include "shared/ThreadUtil.h"
#include "shared/Time.h"
#include "MutatorThread.h"
#include "shared/Log.h"
#include <signal.h>
#include <errno.h>

atomic_bool Synchronizer_stopThreads = false;
static mutex_t synchronizerLock;

// =============================================================================
// Diagnostics for Stuck Threads
// =============================================================================
static void Synchronizer_diagnoseStuckThreads(MutatorThread *self,
                                              int activeCount) {
    GC_LOG_WARN("%d thread(s) not reaching safepoint:", activeCount);

    MutatorThreads_foreach(mutatorThreads, node) {
        MutatorThread *thread = node->value;
        if (thread != self && !MutatorThread_isAtSafepoint(thread)) {
            bool alive = MutatorThread_isAlive(thread);
            GC_MutatorThreadState state =
                atomic_load_explicit(&thread->state, memory_order_acquire);

#ifdef _WIN32
            GC_LOG_WARN("  Thread id=%p, stackBottom=%p, state=%s, alive=%s",
                        (void *)thread->threadHandle,
#else
            GC_LOG_WARN("  Thread id=%lu, stackBottom=%p, state=%s, alive=%s",
                        (unsigned long)thread->thread,
#endif
                        (void *)thread->stackBottom,
                        state == GC_MutatorThreadState_Managed ? "Managed"
                                                               : "Unmanaged",
                        alive ? "yes" : "NO (zombie)");
        }
    }

    GC_LOG_WARN(
        "Possible causes:\n"
        "  - Thread blocked in native code without @blocking annotation\n"
        "  - Thread crashed without cleanup\n"
        "  - Infinite loop in native code\n"
        "  - Deadlock with resource held by waiting thread");
}

#ifndef _WIN32
/* Receiving and handling SIGINT/SIGTERM during GC would lead to deadlocks
   It can happen when thread executing GC would be suspended by signal handler.
   Function executing handler might allocate new objects using GC, but when
   doing so it would be stopped in Synchronizer_yield */
static sigset_t signalsBlockedDuringGC;
#endif

// Internal API used to implement threads execution yielding
static void Synchronizer_SuspendThreads(void);
static void Synchronizer_ResumeThreads(void);
static void Synchronizer_WaitForResumption(MutatorThread *selfThread);

// =============================================================================
// Trap-based Yieldpoints Implementation
// =============================================================================
// Uses signal handlers for low-overhead yieldpoints
// See: https://dl.acm.org/doi/10.1145/2887746.2754187
#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
#include "shared/YieldPointTrap.h"
#include "StackTrace.h"
#include <errno.h>
#ifdef _WIN32
#include <errhandlingapi.h>
#else
#include <pthread.h>
#include <sys/mman.h>
#include <sys/time.h>
#include <unistd.h>
#endif

void **scalanative_GC_yieldpoint_trap;

#ifdef _WIN32
static LONG WINAPI SafepointTrapHandler(EXCEPTION_POINTERS *ex) {
    if (ex->ExceptionRecord->ExceptionFlags == 0) {
        switch (ex->ExceptionRecord->ExceptionCode) {
        case EXCEPTION_ACCESS_VIOLATION:
            ULONG_PTR addr = ex->ExceptionRecord->ExceptionInformation[1];
            if ((void *)addr == scalanative_GC_yieldpoint_trap) {
                Synchronizer_yield();
                return EXCEPTION_CONTINUE_EXECUTION;
            }
            GC_LOG_WARN("Caught exception code %p in GC exception handler",
                        (void *)(uintptr_t)ex->ExceptionRecord->ExceptionCode);
            StackTrace_PrintStackTrace();
        // pass-through
        default:
            return EXCEPTION_CONTINUE_SEARCH;
        }
    }
    return EXCEPTION_CONTINUE_SEARCH;
}
#else
#ifdef __APPLE__
#define SAFEPOINT_TRAP_SIGNAL SIGBUS
#else
#define SAFEPOINT_TRAP_SIGNAL SIGSEGV
#endif
#define THREAD_WAKEUP_SIGNAL SIGCONT
static struct sigaction previousSignalHandler = {};
static sigset_t threadWakupSignals = {};

static void SafepointTrapHandler(int signal, siginfo_t *siginfo, void *uap) {
    int old_errno = errno;
    if (signal == SAFEPOINT_TRAP_SIGNAL &&
        siginfo->si_addr == scalanative_GC_yieldpoint_trap) {
        Synchronizer_yield();
        errno = old_errno;
        return;
    }

    // Try call other handlers
    if (previousSignalHandler.sa_handler != NULL) {
        void *handler = previousSignalHandler.sa_handler;
        if (handler != SIG_DFL && handler != SIG_IGN && handler != SIG_ERR &&
            handler != SafepointTrapHandler) {
            if (previousSignalHandler.sa_flags & SA_SIGINFO) {
                void (*sigInfoHandler)(int, siginfo_t *, void *) = (void (*)(
                    int, siginfo_t *, void *))previousSignalHandler.sa_handler;
                return sigInfoHandler(signal, siginfo, uap);
            } else {
                return previousSignalHandler.sa_handler(signal);
            }
        }
        return;
    }

    GC_LOG_ERROR("%s Unhandled signal %d triggered when accessing "
                 "memory address %p, code=%d",
                 snErrorPrefix, signal, siginfo->si_addr, siginfo->si_code);
    StackTrace_PrintStackTrace();
    abort();
}
#endif

static void SetupYieldPointTrapHandler(void) {
#ifdef _WIN32
    // Call it as first exception handler
    SetUnhandledExceptionFilter(&SafepointTrapHandler);
#else
    sigemptyset(&threadWakupSignals);
    sigaddset(&threadWakupSignals, THREAD_WAKEUP_SIGNAL);
    sigprocmask(SIG_BLOCK, &threadWakupSignals, NULL);
    assert(sigismember(&threadWakupSignals, THREAD_WAKEUP_SIGNAL));

    struct sigaction sa;
    memset(&sa, 0, sizeof(struct sigaction));
    sigemptyset(&sa.sa_mask);
    sa.sa_sigaction = &SafepointTrapHandler;
    sa.sa_flags = SA_SIGINFO | SA_RESTART;
    if (sigaction(SAFEPOINT_TRAP_SIGNAL, &sa, &previousSignalHandler) == -1) {
        GC_LOG_ERROR("Cannot setup safepoint synchronization handler: %s",
                     strerror(errno));
        exit(errno);
    }
#endif
}

static void Synchronizer_WaitForResumption(MutatorThread *selfThread) {
    assert(selfThread == currentMutatorThread);
#ifdef _WIN32
    if (!ResetEvent(selfThread->wakeupEvent)) {
        GC_LOG_WARN("Failed to reset event %lu", GetLastError());
    }
    if (WAIT_OBJECT_0 !=
        WaitForSingleObject(selfThread->wakeupEvent, INFINITE)) {
        GC_LOG_ERROR("suspend thread failed: errno=%lu", GetLastError());
        exit(GetLastError());
    }
#else
    int signum;
    if (0 != sigwait(&threadWakupSignals, &signum)) {
        GC_LOG_ERROR("sigwait failed: %s", strerror(errno));
        exit(errno);
    }
    assert(signum == THREAD_WAKEUP_SIGNAL);
#endif
}

static void Synchronizer_ResumeThread(MutatorThread *thread) {
#ifdef _WIN32
    assert(thread != currentMutatorThread);
    if (!SetEvent(thread->wakeupEvent)) {
        GC_LOG_ERROR("Failed to set event %lu", GetLastError());
    }
#else
    int status = pthread_kill(thread->thread, THREAD_WAKEUP_SIGNAL);
    if (status != 0) {
        GC_LOG_ERROR("Failed to resume thread after GC, retval: %d", status);
    }
#endif
}

static void Synchronizer_SuspendThreads(void) {
    atomic_store_explicit(&Synchronizer_stopThreads, true,
                          memory_order_release);
    YieldPointTrap_arm(scalanative_GC_yieldpoint_trap);
}

static void Synchronizer_ResumeThreads(void) {
    YieldPointTrap_disarm(scalanative_GC_yieldpoint_trap);
    atomic_store_explicit(&Synchronizer_stopThreads, false,
                          memory_order_release);
    MutatorThreads_foreach(mutatorThreads, node) {
        MutatorThread *thread = node->value;
        if (atomic_load_explicit(&thread->isWaiting, memory_order_acquire)) {
            Synchronizer_ResumeThread(thread);
        }
    }
}

#else // !SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
// =============================================================================
// Conditional Yieldpoints Implementation (Debug Mode)
// =============================================================================
#ifdef _WIN32
static HANDLE threadSuspensionEvent;
#else
static struct {
    pthread_mutex_t lock;
    pthread_cond_t resume;
} threadSuspension;
#endif

static void Synchronizer_WaitForResumption(MutatorThread *selfThread) {
    assert(selfThread == currentMutatorThread);
#ifdef _WIN32
    WaitForSingleObject(threadSuspensionEvent, INFINITE);
#else
    pthread_mutex_lock(&threadSuspension.lock);
    while (
        atomic_load_explicit(&Synchronizer_stopThreads, memory_order_consume)) {
        pthread_cond_wait(&threadSuspension.resume, &threadSuspension.lock);
    }
    pthread_mutex_unlock(&threadSuspension.lock);
#endif
}

static void Synchronizer_SuspendThreads(void) {
#ifdef _WIN32
    ResetEvent(threadSuspensionEvent);
    atomic_store_explicit(&Synchronizer_stopThreads, true,
                          memory_order_release);
#else
    pthread_mutex_lock(&threadSuspension.lock);
    atomic_store_explicit(&Synchronizer_stopThreads, true,
                          memory_order_release);
    pthread_mutex_unlock(&threadSuspension.lock);
#endif
}

static void Synchronizer_ResumeThreads(void) {
#ifdef _WIN32
    atomic_store_explicit(&Synchronizer_stopThreads, false,
                          memory_order_release);
    SetEvent(threadSuspensionEvent);
#else
    pthread_mutex_lock(&threadSuspension.lock);
    atomic_store_explicit(&Synchronizer_stopThreads, false,
                          memory_order_release);
    pthread_cond_broadcast(&threadSuspension.resume);
    pthread_mutex_unlock(&threadSuspension.lock);
#endif
}
#endif // !SCALANATIVE_GC_USE_YIELDPOINT_TRAPS

// =============================================================================
// Synchronizer Initialization
// =============================================================================
void Synchronizer_init(void) {
    mutex_init(&synchronizerLock);
#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
    scalanative_GC_yieldpoint_trap = YieldPointTrap_init();
    YieldPointTrap_disarm(scalanative_GC_yieldpoint_trap);
    SetupYieldPointTrapHandler();
#else
#ifdef _WIN32
    threadSuspensionEvent = CreateEvent(NULL, true, false, NULL);
    if (threadSuspensionEvent == NULL) {
        GC_LOG_ERROR("Failed to setup synchronizer event: errno=%lu",
                     GetLastError());
        exit(1);
    }
#else
    sigemptyset(&signalsBlockedDuringGC);
    sigaddset(&signalsBlockedDuringGC, SIGINT);
    sigaddset(&signalsBlockedDuringGC, SIGTERM);
    if (pthread_mutex_init(&threadSuspension.lock, NULL) != 0 ||
        pthread_cond_init(&threadSuspension.resume, NULL) != 0) {
        GC_LOG_ERROR("Failed to setup synchronizer lock: %s", strerror(errno));
        exit(1);
    }
#endif
#endif
}

// =============================================================================
// Common Implementation
// =============================================================================
void Synchronizer_yield(void) {
    MutatorThread *self = currentMutatorThread;
    MutatorThread_switchState(self, GC_MutatorThreadState_Unmanaged);
    atomic_thread_fence(memory_order_seq_cst);

    atomic_store_explicit(&self->isWaiting, true, memory_order_release);
    while (
        atomic_load_explicit(&Synchronizer_stopThreads, memory_order_consume)) {
        Synchronizer_WaitForResumption(self);
    }
    atomic_store_explicit(&self->isWaiting, false, memory_order_release);

    MutatorThread_switchState(self, GC_MutatorThreadState_Managed);
    atomic_thread_fence(memory_order_seq_cst);
}

bool Synchronizer_acquire(void) {
    if (!mutex_tryLock(&synchronizerLock)) {
        scalanative_GC_yield();
        return false;
    }
#ifndef _WIN32
    sigprocmask(SIG_BLOCK, &signalsBlockedDuringGC, NULL);
#endif

    // Don't allow for registration of any new threads
    MutatorThreads_lockRead();
    Synchronizer_SuspendThreads();
    MutatorThread *self = currentMutatorThread;
    MutatorThread_switchState(self, GC_MutatorThreadState_Unmanaged);

    uint64_t startTime = Time_current_millis();
    uint64_t lastWarningTime = startTime;
    int activeThreads;

    do {
        atomic_thread_fence(memory_order_seq_cst);
        activeThreads = 0;
        MutatorThreads_foreach(mutatorThreads, node) {
            MutatorThread *it = node->value;
            // Don't count self - we're the GC thread
            if (it != self && !MutatorThread_isAtSafepoint(it)) {
                activeThreads++;
            }
        }

        if (activeThreads > 0) {
            uint64_t now = Time_current_millis();
            uint64_t elapsed = now - startTime;

            // Periodic warnings about stuck threads
            if (now - lastWarningTime >= Settings_SyncWarningIntervalMs()) {
                lastWarningTime = now;
                GC_LOG_WARN("Waiting for %d thread(s) to reach safepoint "
                            "(%.1fs elapsed)",
                            activeThreads, elapsed / 1000.0);
                Synchronizer_diagnoseStuckThreads(self, activeThreads);
            }

            // Check for timeout (0 = disabled)
            if (Settings_SyncTimeoutMs() > 0 &&
                elapsed >= Settings_SyncTimeoutMs()) {
                GC_LOG_ERROR(
                    "FATAL: Timeout after %.1fs waiting for %d thread(s)\n"
                    "Threads did not reach safepoint which blocks the GC.\n"
                    "This is likely caused by:\n"
                    "  - Native/extern call missing @blocking annotation\n"
                    "  - Thread stuck in infinite loop in native code\n"
                    "Set SCALANATIVE_GC_SYNC_TIMEOUT_MS=0 to disable timeout\n"
                    "Current timeout: %llu ms",
                    elapsed / 1000.0, activeThreads,
                    (unsigned long long)Settings_SyncTimeoutMs());
                // Abort - this is the safest option as continuing could corrupt
                // memory
                abort();
            }

            thread_yield();
        }
    } while (activeThreads > 0);
    return true;
}

void Synchronizer_release(void) {
    Synchronizer_ResumeThreads();
    MutatorThreads_unlockRead();
    mutex_unlock(&synchronizerLock);
    MutatorThread_switchState(currentMutatorThread,
                              GC_MutatorThreadState_Managed);
#ifndef _WIN32
    sigprocmask(SIG_UNBLOCK, &signalsBlockedDuringGC, NULL);
#endif
}

#endif
