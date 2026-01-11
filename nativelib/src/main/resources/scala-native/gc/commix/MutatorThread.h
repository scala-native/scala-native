#ifndef MUTATOR_THREAD_H
#define MUTATOR_THREAD_H
#include "shared/ScalaNativeGC.h"
#include "shared/GCTypes.h"
#include "Allocator.h"
#include "LargeAllocator.h"
#include <stdatomic.h>
#include <stdbool.h>
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

    // Thread handles for liveness checking and signal delivery
#ifdef _WIN32
    HANDLE threadHandle; // Duplicated handle for liveness checking
#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
    HANDLE wakeupEvent; // Used for thread wake-up (trap mode only)
#endif
#else
    thread_t thread; // pthread_t - used for liveness check and signals
#endif

#ifdef SCALANATIVE_THREAD_ALT_STACK
    ThreadInfo *threadInfo;
#endif
} MutatorThread;

typedef struct MutatorThreadNode {
    MutatorThread *value;
    struct MutatorThreadNode *next;
} MutatorThreadNode;

typedef MutatorThreadNode *MutatorThreads;

// =============================================================================
// MutatorThread Lifecycle API
// =============================================================================
void MutatorThread_init(word_t **stackBottom);
void MutatorThread_delete(MutatorThread *self);
void MutatorThread_switchState(MutatorThread *self,
                               GC_MutatorThreadState newState);

// =============================================================================
// Thread State Checks
// =============================================================================

// Check if thread has reached a safepoint (stopped and saved its stack)
// Returns true if thread is at safepoint (stackTop is set), false if still
// running Note: A thread at safepoint has switched to Unmanaged state and saved
// registers
bool MutatorThread_isAtSafepoint(MutatorThread *thread);

// Check if a mutator thread is still alive (useful for detecting zombie
// threads) Returns true if thread exists, false if thread has terminated
bool MutatorThread_isAlive(MutatorThread *thread);

// =============================================================================
// MutatorThreads List Management
// =============================================================================
void MutatorThreads_init(void);
void MutatorThreads_add(MutatorThread *node);
void MutatorThreads_remove(MutatorThread *node);
void MutatorThreads_lockRead(void);
void MutatorThreads_unlockRead(void);

#define MutatorThreads_foreach(list, node)                                     \
    for (MutatorThreads node = list; node != NULL; node = node->next)

#endif // MUTATOR_THREAD_H
