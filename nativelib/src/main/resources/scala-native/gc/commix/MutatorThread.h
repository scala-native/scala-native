#ifndef MUTATOR_THREAD_H
#define MUTATOR_THREAD_H
#include "shared/ScalaNativeGC.h"
#include "shared/GCTypes.h"
#include "Allocator.h"
#include "LargeAllocator.h"
#include <stdatomic.h>
#include <shared/ThreadUtil.h>
#include "immix_commix/RegistersCapture.h"
#include "nativeThreadTLS.h"

typedef struct {
    _Atomic(GC_MutatorThreadState) state;
    atomic_intptr_t stackTop;
    atomic_bool isWaiting;
    RegistersBuffer registersBuffer;
    // immutable fields
    word_t **stackBottom;
    Allocator allocator;
    LargeAllocator largeAllocator;
#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
#ifdef _WIN32
    HANDLE wakeupEvent;
#else
    thread_t thread;
#endif
#endif // SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
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
void MutatorThreads_lockRead();
void MutatorThreads_unlockRead();

#define MutatorThreads_foreach(list, node)                                     \
    for (MutatorThreads node = list; node != NULL; node = node->next)

#endif // MUTATOR_THREAD_H
