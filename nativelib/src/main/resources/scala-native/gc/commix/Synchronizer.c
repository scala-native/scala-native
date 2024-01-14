#if defined(SCALANATIVE_GC_COMMIX)

#include "immix_commix/Synchronizer.h"
#include "shared/ScalaNativeGC.h"
#include <stdio.h>
#include <stdatomic.h>
#include <stdlib.h>

#include "State.h"
#include "shared/ThreadUtil.h"
#include "MutatorThread.h"

atomic_bool Synchronizer_stopThreads = false;
static mutex_t synchronizerLock;

// Internal API used to implement threads execution yielding
static void Synchronizer_SuspendThreads(void);
static void Synchronizer_ResumeThreads(void);
static void Synchronizer_WaitForResumption(MutatorThread *selfThread);

// We can use 1 out 2 available threads yielding mechanisms:
// 1: Trap-based yieldpoints using signal handlers, see:
// https://dl.acm.org/doi/10.1145/2887746.2754187, low overheads, but
// problematic when debugging
// 2: Conditional yieldpoints based on checking
// internal flag, better for debuggin, but slower
#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
#include "shared/YieldPointTrap.h"
#include "immix_commix/StackTrace.h"
#include <errno.h>
#ifdef _WIN32
#include <errhandlingapi.h>
#else
#include <signal.h>
#include <pthread.h>
#include <sys/mman.h>
#include <sys/time.h>
#include <unistd.h>
#endif

void **scalanative_GC_yieldpoint_trap;

#ifdef _WIN32
static LONG WINAPI SafepointTrapHandler(EXCEPTION_POINTERS *ex) {
    switch (ex->ExceptionRecord->ExceptionCode) {
    case EXCEPTION_ACCESS_VIOLATION:
        ULONG_PTR addr = ex->ExceptionRecord->ExceptionInformation[1];
        if ((void *)addr == scalanative_GC_yieldpoint_trap) {
            Synchronizer_yield();
            return EXCEPTION_CONTINUE_EXECUTION;
        }
        fprintf(stderr, "Caught exception code %p in GC exception handler\n",
                (void *)(uintptr_t)ex->ExceptionRecord->ExceptionCode);
        fflush(stdout);
        StackTrace_PrintStackTrace();
    // pass-through
    default:
        return EXCEPTION_CONTINUE_SEARCH;
    }
}
#else
#ifdef __APPLE__
#define SAFEPOINT_TRAP_SIGNAL SIGBUS
#else
#define SAFEPOINT_TRAP_SIGNAL SIGSEGV
#endif
#define THREAD_WAKEUP_SIGNAL SIGCONT
static struct sigaction defaultAction;
static sigset_t threadWakupSignals;
static void SafepointTrapHandler(int signal, siginfo_t *siginfo, void *uap) {
    if (siginfo->si_addr == scalanative_GC_yieldpoint_trap) {
        Synchronizer_yield();
    } else {
        fprintf(stderr,
                "Unexpected signal %d when accessing memory at address %p\n",
                signal, siginfo->si_addr);
        StackTrace_PrintStackTrace();
        defaultAction.sa_handler(signal);
    }
}
#endif

static void SetupYieldPointTrapHandler() {
#ifdef _WIN32
    // Call it as first exception handler
    AddVectoredExceptionHandler(1, &SafepointTrapHandler);
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
    if (sigaction(SAFEPOINT_TRAP_SIGNAL, &sa, &defaultAction) == -1) {
        perror("Error: cannot setup safepoint synchronization handler");
        exit(errno);
    }
#endif
}

static void Synchronizer_WaitForResumption(MutatorThread *selfThread) {
    assert(selfThread == currentMutatorThread);
#ifdef _WIN32
    if (!ResetEvent(selfThread->wakeupEvent)) {
        fprintf(stderr, "Failed to reset event %lu\n", GetLastError());
    }
    if (WAIT_OBJECT_0 !=
        WaitForSingleObject(selfThread->wakeupEvent, INFINITE)) {
        fprintf(stderr, "Error: suspend thread");
        exit(GetLastError());
    }
#else
    int signum;
    if (0 != sigwait(&threadWakupSignals, &signum)) {
        perror("Error: sig wait");
        exit(errno);
    }
    assert(signum == THREAD_WAKEUP_SIGNAL);
#endif
}

static void Synchronizer_ResumeThread(MutatorThread *thread) {
#ifdef _WIN32
    assert(thread != currentMutatorThread);
    if (!SetEvent(thread->wakeupEvent)) {
        fprintf(stderr, "Failed to set event %lu\n", GetLastError());
    }
#else
    int status = pthread_kill(thread->thread, THREAD_WAKEUP_SIGNAL);
    if (status != 0) {
        fprintf(stderr, "Failed to resume thread after GC, retval: %d\n",
                status);
    }
#endif
}

static void Synchronizer_SuspendThreads(void) {
    atomic_store_explicit(&Synchronizer_stopThreads, true,
                          memory_order_release);
    YieldPointTrap_arm(scalanative_GC_yieldpoint_trap);
}

static void Synchronizer_ResumeThreads(void) {
    atomic_store_explicit(&Synchronizer_stopThreads, false,
                          memory_order_release);
    YieldPointTrap_disarm(scalanative_GC_yieldpoint_trap);
    MutatorThreads_foreach(mutatorThreads, node) {
        Synchronizer_ResumeThread(node->value);
    }
}

#else // notDefined SCALANATIVE_GC_USE_YIELDPOINT_TRAPS

#ifdef _WIN32
static HANDLE threadSuspensionEvent;
#else
static struct {
    pthread_mutex_t lock;
    pthread_cond_t resume;
} threadSuspension;
#endif

static void Synchronizer_WaitForResumption(MutatorThread *selfThread) {
    assert(thread == currentMutatorThread);
    atomic_store_explicit(&selfThread->isWaiting, true, memory_order_release);
#ifdef _WIN32
    WaitForSingleObject(threadSuspensionEvent, INFINITE);
#else
    pthread_mutex_lock(&threadSuspension.lock);
    while (atomic_load(&Synchronizer_stopThreads)) {
        pthread_cond_wait(&threadSuspension.resume, &threadSuspension.lock);
    }
    pthread_mutex_unlock(&threadSuspension.lock);
#endif
    atomic_store_explicit(&selfThread->isWaiting, false, memory_order_release);
}

static void Synchronizer_SuspendThreads() {
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

static void Synchronizer_ResumeThreads() {

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

void Synchronizer_init() {
    mutex_init(&synchronizerLock);
#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
    scalanative_GC_yieldpoint_trap = YieldPointTrap_init();
    SetupYieldPointTrapHandler();
#else
#ifdef _WIN32
    threadSuspensionEvent = CreateEvent(NULL, true, false, NULL);
    if (threadSuspensionEvent == NULL) {
        fprintf(stderr, "Failed to setup synchronizer event: errno=%lu\n",
                GetLastError());
        exit(1);
    }
#else
    if (pthread_mutex_init(&threadSuspension.lock, NULL) != 0 ||
        pthread_cond_init(&threadSuspension.resume, NULL) != 0) {
        perror("Failed to setup synchronizer lock");
        exit(1);
    }
#endif
#endif
}

// ---------------------
// Common implementation
// ---------------------

void Synchronizer_yield() {
    MutatorThread *self = currentMutatorThread;
    MutatorThread_switchState(self, GC_MutatorThreadState_Unmanaged);
    atomic_thread_fence(memory_order_seq_cst);

    Synchronizer_WaitForResumption(self);

    MutatorThread_switchState(self, GC_MutatorThreadState_Managed);
    atomic_thread_fence(memory_order_seq_cst);
}

bool Synchronizer_acquire() {
    if (!mutex_tryLock(&synchronizerLock)) {
        if (atomic_load(&Synchronizer_stopThreads))
            Synchronizer_yield();
        return false;
    }

    // Don't allow for registration of any new threads;
    MutatorThreads_lockRead();
    Synchronizer_SuspendThreads();
    MutatorThread *self = currentMutatorThread;
    MutatorThread_switchState(self, GC_MutatorThreadState_Unmanaged);

    int iteration = 0;
    int activeThreads;
    do {
        atomic_thread_fence(memory_order_seq_cst);
        iteration++;
        activeThreads = 0;
        MutatorThreads_foreach(mutatorThreads, node) {
            MutatorThread *it = node->value;
            if ((void *)atomic_load_explicit(&it->stackTop,
                                             memory_order_acquire) == NULL) {
                activeThreads++;
            }
        }
        if (activeThreads > 0)
            thread_yield();
    } while (activeThreads > 0);
    return true;
}

void Synchronizer_release() {
    Synchronizer_ResumeThreads();
    MutatorThreads_unlockRead();
    MutatorThread_switchState(currentMutatorThread,
                              GC_MutatorThreadState_Managed);
    mutex_unlock(&synchronizerLock);
}

#endif