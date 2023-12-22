#include "shared/ScalaNativeGC.h"
#include "shared/GCTypes.h"
#include "Allocator.h"
#include "LargeAllocator.h"
#include "State.h"
#include <stdatomic.h>
#include "shared/ThreadUtil.h"
#include <setjmp.h>

#ifndef MUTATOR_THREAD_H
#define MUTATOR_THREAD_H

typedef struct {
    _Atomic(MutatorThreadState) state;
    atomic_intptr_t stackTop;
    atomic_bool isWaiting;
    jmp_buf executionContext;
    // immutable fields
    word_t **stackBottom;
    Allocator allocator;
    LargeAllocator largeAllocator;
} MutatorThread;

typedef struct MutatorThreadNode {
    MutatorThread *value;
    struct MutatorThreadNode *next;
} MutatorThreadNode;

typedef MutatorThreadNode *MutatorThreads;

void MutatorThread_init(word_t **stackBottom);
void MutatorThread_delete(MutatorThread *self);
void MutatorThread_switchState(MutatorThread *self,
                               MutatorThreadState newState);

void MutatorThreads_init();
void MutatorThreads_add(MutatorThread *node);
void MutatorThreads_remove(MutatorThread *node);
void MutatorThreads_lock();
void MutatorThreads_unlock();

#define MutatorThreads_foreach(list, node)                                     \
    for (MutatorThreads node = list; node != NULL; node = node->next)

#endif // MUTATOR_THREAD_H
