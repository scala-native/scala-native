#ifndef MUTATOR_THREAD_IMMIX_H
#define MUTATOR_THREAD_IMMIX_H
#include "Allocator.h"
#include "LargeAllocator.h"
#include "shared/ScalaNativeGC.h"
#include "immix_commix/RegistersCapture.h"
#include <stdatomic.h>
#include <stdbool.h>
#include "nativeThreadTLS.h"

typedef struct {
    _Atomic(GC_MutatorThreadState) state;
    word_t **stackBottom;
    atomic_intptr_t stackTop;
    atomic_bool isWaiting;
    RegistersBuffer registersBuffer;
    // immutable fields
#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
#ifdef _WIN32
    HANDLE wakeupEvent;
#else
    thread_t thread;
#endif
#endif // SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
    Allocator allocator;
    LargeAllocator largeAllocator;
#ifdef SCALANATIVE_THREAD_ALT_STACK
    ThreadInfo *threadInfo;
#endif
} MutatorThread;

typedef struct MutatorThreadNode {
    MutatorThread *value;
    struct MutatorThreadNode *next;
} MutatorThreadNode;

typedef MutatorThreadNode *MutatorThreads;

void MutatorThread_init(word_t **stackBottom);
void MutatorThread_delete(MutatorThread *self);
void MutatorThread_switchState(MutatorThread *self,
                               GC_MutatorThreadState newState);
void MutatorThreads_init();
void MutatorThreads_add(MutatorThread *node);
void MutatorThreads_remove(MutatorThread *node);
void MutatorThreads_lock();
void MutatorThreads_unlock();

#define MutatorThreads_foreach(list, node)                                     \
    for (MutatorThreads node = list; node != NULL; node = node->next)

#endif // MUTATOR_THREAD_IMMIX_H
