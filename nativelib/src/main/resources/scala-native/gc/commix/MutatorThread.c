#if defined(SCALANATIVE_GC_COMMIX)
#include "shared/ScalaNativeGC.h"
#include "MutatorThread.h"
#include "State.h"
#include "Sweeper.h"
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
#include "immix_commix/Synchronizer.h"
#endif
#include <stdlib.h>
#include <string.h>
#include <stdatomic.h>
#include <stdint.h>
#include "shared/ThreadUtil.h"
#include "shared/Log.h"
#include <assert.h>

#ifndef _WIN32
#include <signal.h>
#ifdef __linux__
#include <sys/syscall.h>
#endif
#endif

#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
#include "shared/YieldPointTrap.h"
#endif

static rwlock_t threadListsModificationLock;

void MutatorThreads_lockRead() {
    rwlock_lockRead(&threadListsModificationLock);
}
bool MutatorThreads_tryLockRead() {
    return rwlock_tryLockRead(&threadListsModificationLock);
}
void MutatorThreads_unlockRead() {
    rwlock_unlockRead(&threadListsModificationLock);
}

static void MutatorThreads_lockWrite() {
    rwlock_lockWrite(&threadListsModificationLock);
}
static void MutatorThreads_unlockWrite() {
    rwlock_unlockWrite(&threadListsModificationLock);
}

#ifndef SCALANATIVE_MULTITHREADING_ENABLED
void MutatorThread_suspendContext(MutatorThread *self) {
    MutatorThreads_lockWrite();
    assert(self->state == GC_MutatorThreadState_Unmanaged);
    self->threadInfo = NULL;
    memset(&self->registersBuffer, 0, sizeof(self->registersBuffer));
    self->stackBottom = (word_t **)&self->registersBuffer;
    atomic_store(&self->stackTop, (intptr_t)self->stackBottom);
    MutatorThreads_unlockWrite();
}
#endif

void MutatorThread_setThreadInfo(MutatorThread *self, ThreadInfo *info) {
    MutatorThreads_lockWrite();
    self->threadInfo = info;
    MutatorThreads_unlockWrite();
}

void MutatorThread_admit(MutatorThread *self) {
    assert(self != NULL);
    if (atomic_load_explicit(&self->state, memory_order_acquire) ==
        GC_MutatorThreadState_Managed)
        return;
    /* GC releases this lock while waiting for sweepers.
     * Stay unmanaged until GC resumes the threads. */
    MutatorThreads_lockWrite();
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    while (
        atomic_load_explicit(&Synchronizer_stopThreads, memory_order_acquire)) {
        MutatorThreads_unlockWrite();
        thread_yield();
        MutatorThreads_lockWrite();
    }
#endif
    MutatorThread_switchState(self, GC_MutatorThreadState_Managed);
    MutatorThreads_unlockWrite();
}

void MutatorThreads_init() {
    rwlock_init(&threadListsModificationLock);
    atomic_init(&mutatorThreads, NULL);
}

bool MutatorThreads_add(MutatorThread *node) {
    if (!node)
        return false;
    MutatorThreadNode *newNode =
        (MutatorThreadNode *)malloc(sizeof(MutatorThreadNode));
    if (newNode == NULL)
        return false;
    newNode->value = node;
    MutatorThreads_lockWrite();
    newNode->next = mutatorThreads;
    mutatorThreads = newNode;
    atomic_fetch_add_explicit(&mutatorThreadsCount, 1, memory_order_relaxed);
    MutatorThreads_unlockWrite();
    return true;
}

void MutatorThreads_remove(MutatorThread *node) {
    if (!node)
        return;

    MutatorThreads_lockWrite();
    // Concurrent sweepers distribute chunks to a live mutator allocator.
    // Keep the last allocator until that sweep has finished. Release the
    // lifecycle lock while waiting so sweepers can make progress.
    while (atomic_load_explicit(&mutatorThreadsCount, memory_order_relaxed) ==
               1 &&
           !Sweeper_IsSweepDone(&heap)) {
        MutatorThreads_unlockWrite();
        thread_yield();
        MutatorThreads_lockWrite();
    }
    MutatorThreads current = mutatorThreads;
    MutatorThreads previous = NULL;
    while (current != NULL && current->value != node) {
        previous = current;
        current = current->next;
    }
    if (current != NULL) {
        if (previous != NULL)
            previous->next = current->next;
        else
            mutatorThreads = current->next;
        atomic_fetch_sub_explicit(&mutatorThreadsCount, 1,
                                  memory_order_relaxed);
        free(current);
    }
    MutatorThreads_unlockWrite();
}

void MutatorThreads_afterForkChild(void) {
    /* Drop the mutators of OS threads that fork() did not copy, and reinit
     * the list lock in case one of them held it. Their records are leaked
     * on purpose: freeing them would also have to return their heap blocks,
     * and the next collection reclaims those anyway. */
    rwlock_init(&threadListsModificationLock);
    MutatorThreads kept = NULL;
    for (MutatorThreads node = mutatorThreads; node != NULL;
         node = node->next) {
        if (node->value == currentMutatorThread)
            kept = node;
    }
    if (kept != NULL) {
        kept->next = NULL;
#ifdef __linux__
        /* The child is a new task, so the recorded id belongs to the parent.
         * Without this the liveness probe would report the caller dead. */
        kept->value->kernelThreadId = (pid_t)syscall(SYS_gettid);
#endif
    }
    mutatorThreads = kept;
    atomic_store_explicit(&mutatorThreadsCount, kept != NULL ? 1 : 0,
                          memory_order_relaxed);
}

#endif
