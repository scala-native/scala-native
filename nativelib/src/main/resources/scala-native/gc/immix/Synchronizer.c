#include "Synchronizer.h"
#include "ScalaNativeGC.h"
#include <stdio.h>
#include <stdatomic.h>
#include <stdlib.h>
#include <errno.h>

#include "State.h"
#include "ThreadUtil.h"
#include "Safepoint.h"
#include "MutatorThread.h"
#include "StackTrace.h"

#ifdef _WIN32
#include <errhandlingapi.h>
#else
#include <signal.h>
#include <pthread.h>
#include <sys/mman.h>
#include <sys/time.h>
#include <unistd.h>
#endif

static volatile bool isCollecting = false;
static mutex_t synchronizerLock;

#define SafepointInstance (scalanative_gc_safepoint)

#ifdef _WIN32
static LONG WINAPI SafepointTrapHandler(EXCEPTION_POINTERS *ex) {
    switch (ex->ExceptionRecord->ExceptionCode) {
    case EXCEPTION_ACCESS_VIOLATION:
        ULONG_PTR addr = ex->ExceptionRecord->ExceptionInformation[1];
        if (SafepointInstance == (void *)addr) {
            Synchronizer_wait();
            return EXCEPTION_CONTINUE_EXECUTION;
        }
        fprintf(stderr, "Cought exception code %p in GC exception handler\n",
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
#define THREAD_WAKUP_SIGNAL SIGCONT
static struct sigaction defaultAction;
static sigset_t threadWakupSignals;
static void SafepointTrapHandler(int signal, siginfo_t *siginfo, void *uap) {
    if (siginfo->si_addr == SafepointInstance) {
        Synchronizer_wait();
    } else {
        fprintf(stderr,
                "Unexpected signal %d when accessing memory at address %p\n",
                signal, siginfo->si_addr);
        StackTrace_PrintStackTrace();
        defaultAction.sa_handler(signal);
    }
}
#endif

static void SetupPageFaultHandler() {
#ifdef _WIN32
    // Call it as first exception handler
    AddVectoredExceptionHandler(1, &SafepointTrapHandler);
#else
    sigemptyset(&threadWakupSignals);
    sigaddset(&threadWakupSignals, THREAD_WAKUP_SIGNAL);
    sigprocmask(SIG_BLOCK, &threadWakupSignals, NULL);
    assert(sigismember(&threadWakupSignals, THREAD_WAKUP_SIGNAL));

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

static void Synchronizer_SuspendThread(MutatorThread *thread) {
    assert(thread == currentMutatorThread);
#ifdef _WIN32
    if (!ResetEvent(thread->wakeupEvent)) {
        fprintf(stderr, "Failed to reset event %lu\n", GetLastError());
    }
    if (WAIT_OBJECT_0 != WaitForSingleObject(thread->wakeupEvent, INFINITE)) {
        fprintf(stderr, "Error: suspend thread");
        exit(GetLastError());
    }
#else
    int signum;
    if (0 != sigwait(&threadWakupSignals, &signum)) {
        perror("Error: sig wait");
        exit(errno);
    }
    assert(signum == THREAD_WAKUP_SIGNAL);
#endif
}

static void Synchronizer_WakupThread(MutatorThread *thread) {
#ifdef _WIN32
    assert(thread != currentMutatorThread);
    if (!SetEvent(thread->wakeupEvent)) {
        fprintf(stderr, "Failed to set event %lu\n", GetLastError());
    }
#else
    int status = pthread_kill(thread->thread, THREAD_WAKUP_SIGNAL);
    if (status != 0) {
        fprintf(stderr, "Failed to resume thread after GC, retval: %d\n",
                status);
    }
#endif
}

void Synchronizer_init() {
    Safepoint_init(&SafepointInstance);
    mutex_init(&synchronizerLock);
    SetupPageFaultHandler();
}

void Synchronizer_wait() {
    MutatorThread *self = currentMutatorThread;
    MutatorThread_switchState(self, MutatorThreadState_Unmanaged);
    atomic_signal_fence(memory_order_seq_cst);
    atomic_thread_fence(memory_order_seq_cst);

    atomic_store_explicit(&self->isWaiting, true, memory_order_release);
    while (isCollecting) {
        Synchronizer_SuspendThread(self);
    }
    atomic_store_explicit(&self->isWaiting, false, memory_order_release);

    MutatorThread_switchState(self, MutatorThreadState_Managed);
    atomic_thread_fence(memory_order_seq_cst);
}

bool Synchronizer_acquire() {
    if (!mutex_tryLock(&synchronizerLock)) {
        if (isCollecting)
            Synchronizer_wait();
        return false;
    }

    isCollecting = true;
    MutatorThread *self = currentMutatorThread;
    MutatorThread_switchState(self, MutatorThreadState_Unmanaged);

    // Don't allow for registration of any new threads;
    MutatorThreads_lock();
    Safepoint_arm(SafepointInstance);
    atomic_thread_fence(memory_order_seq_cst);

    int iteration = 0;
    int activeThreads;
    do {
        atomic_thread_fence(memory_order_seq_cst);
        iteration++;
        activeThreads = 0;
        MutatorThreads_foreach(mutatorThreads, node) {
            MutatorThread *it = node->value;
            if (it->stackTop == NULL) {
                activeThreads++;
            }
        }
        if (activeThreads > 0)
            thread_yield();
    } while (activeThreads > 0);
    return true;
}

void Synchronizer_release() {
    Safepoint_disarm(SafepointInstance);
    isCollecting = false;
    atomic_thread_fence(memory_order_seq_cst);

    int stoppedThreads;
    do {
        stoppedThreads = 0;
        MutatorThreads_foreach(mutatorThreads, node) {
            MutatorThread *thread = node->value;
            if (atomic_load_explicit(&thread->isWaiting,
                                     memory_order_acquire)) {
                stoppedThreads++;
                Synchronizer_WakupThread(thread);
            }
        }
        if (stoppedThreads > 0)
            thread_yield();
    } while (stoppedThreads > 0);
    MutatorThread_switchState(currentMutatorThread, MutatorThreadState_Managed);
    MutatorThreads_unlock();
    mutex_unlock(&synchronizerLock);
}
