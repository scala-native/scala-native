#if defined(SCALANATIVE_GC_COMMIX)

#include "Synchronizer.h"
#include "shared/ScalaNativeGC.h"
#include <stdio.h>
#include <stdatomic.h>
#include <stdlib.h>
#include <errno.h>

#include "State.h"
#include "shared/ThreadUtil.h"
#include "MutatorThread.h"

atomic_bool Synchronizer_stopThreads = false;
static mutex_t synchronizerLock;
#ifdef _WIN32
static HANDLE threadSuspensionEvent;
#else
static struct {
    pthread_mutex_t lock;
    pthread_cond_t resume;
} threadSuspension;
#endif

static void Synchronizer_SuspendThread(MutatorThread *thread) {
    assert(thread == currentMutatorThread);
    atomic_store_explicit(&thread->isWaiting, true, memory_order_release);
#ifdef _WIN32
    WaitForSingleObject(threadSuspensionEvent, INFINITE);
#else
    pthread_mutex_lock(&threadSuspension.lock);
    while (atomic_load(&Synchronizer_stopThreads)) {
        pthread_cond_wait(&threadSuspension.resume, &threadSuspension.lock);
    }
    pthread_mutex_unlock(&threadSuspension.lock);
#endif
    atomic_store_explicit(&thread->isWaiting, false, memory_order_release);
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

static void Synchronizer_WakeupThreads() {
    assert(Synchronizer_stopThreads == FALSE);
#ifdef _WIN32
    SetEvent(threadSuspensionEvent);
#else
    pthread_mutex_lock(&threadSuspension.lock);
    pthread_cond_broadcast(&threadSuspension.resume);
    pthread_mutex_unlock(&threadSuspension.lock);
#endif
}

void Synchronizer_init() {
    mutex_init(&synchronizerLock);
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
}

void Synchronizer_wait() {
    MutatorThread *self = currentMutatorThread;
    MutatorThread_switchState(self, MutatorThreadState_Unmanaged);
    atomic_thread_fence(memory_order_seq_cst);

    Synchronizer_SuspendThread(self);

    MutatorThread_switchState(self, MutatorThreadState_Managed);
    atomic_thread_fence(memory_order_seq_cst);
}

bool Synchronizer_acquire() {
    if (!mutex_tryLock(&synchronizerLock)) {
        if (atomic_load(&Synchronizer_stopThreads))
            Synchronizer_wait();
        return false;
    }

    // Don't allow for registration of any new threads;
    MutatorThreads_lockRead();
    Synchronizer_SuspendThreads();
    MutatorThread *self = currentMutatorThread;
    MutatorThread_switchState(self, MutatorThreadState_Unmanaged);

    int iteration = 0;
    int activeThreads;
    do {
        atomic_thread_fence(memory_order_seq_cst);
        iteration++;
        activeThreads = 0;
        MutatorThreads_foreach(mutatorThreads, node) {
            MutatorThread *it = node->value;
            if ((void *)atomic_load(&it->stackTop) == NULL) {
                activeThreads++;
            }
        }
        if (activeThreads > 0)
            thread_yield();
    } while (activeThreads > 0);
    return true;
}

void Synchronizer_release() {
    atomic_store_explicit(&Synchronizer_stopThreads, false,
                          memory_order_release);

    int stoppedThreads;
    do {
        stoppedThreads = 0;
        MutatorThreads_foreach(mutatorThreads, node) {
            MutatorThread *thread = node->value;
            if (atomic_load_explicit(&thread->isWaiting,
                                     memory_order_acquire)) {
                stoppedThreads++;
            }
        }
        if (stoppedThreads > 0) {
            Synchronizer_WakeupThreads();
            thread_yield();
        }
    } while (stoppedThreads > 0);
    MutatorThread_switchState(currentMutatorThread, MutatorThreadState_Managed);
    MutatorThreads_unlockRead();
    mutex_unlock(&synchronizerLock);
}

#endif