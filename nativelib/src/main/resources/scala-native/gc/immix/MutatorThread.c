#if defined(SCALANATIVE_GC_IMMIX)

#include "MutatorThread.h"
#include "State.h"
#include <stdlib.h>
#include <string.h>
#include <stdatomic.h>
#include <stdint.h>
#include "shared/ThreadUtil.h"
#include "shared/Log.h"
#include <assert.h>

#ifndef _WIN32
#include <signal.h>
#endif

#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
#include "shared/YieldPointTrap.h"
#endif

static mutex_t threadListsModificationLock;

#ifndef SCALANATIVE_MULTITHREADING_ENABLED
void MutatorThread_suspendContext(MutatorThread *self) {
    MutatorThreads_lock();
    assert(self->state == GC_MutatorThreadState_Unmanaged);
    self->threadInfo = NULL;
    memset(&self->registersBuffer, 0, sizeof(self->registersBuffer));
    self->stackBottom = (word_t **)&self->registersBuffer;
    atomic_store(&self->stackTop, (intptr_t)self->stackBottom);
    MutatorThreads_unlock();
}
#endif

void MutatorThread_admit(MutatorThread *self) {
    assert(self != NULL);
    if (atomic_load_explicit(&self->state, memory_order_acquire) ==
        GC_MutatorThreadState_Managed)
        return;
    /* GC holds this lifecycle lock while it stops and scans mutators. Wait
     * while still unmanaged, then publish admission before unlocking. */
    MutatorThreads_lock();
    MutatorThread_switchState(self, GC_MutatorThreadState_Managed);
    MutatorThreads_unlock();
}

void MutatorThreads_init() { mutex_init(&threadListsModificationLock); }

bool MutatorThreads_add(MutatorThread *node) {
    if (!node)
        return false;
    MutatorThreadNode *newNode =
        (MutatorThreadNode *)malloc(sizeof(MutatorThreadNode));
    if (newNode == NULL)
        return false;
    newNode->value = node;
    MutatorThreads_lock();
    newNode->next = mutatorThreads;
    mutatorThreads = newNode;
    MutatorThreads_unlock();
    return true;
}

void MutatorThreads_remove(MutatorThread *node) {
    if (!node)
        return;

    MutatorThreads_lock();
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
        free(current);
    }
    MutatorThreads_unlock();
}

void MutatorThreads_lock() { mutex_lock(&threadListsModificationLock); }

void MutatorThreads_unlock() { mutex_unlock(&threadListsModificationLock); }

#endif
