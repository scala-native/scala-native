#ifndef MUTATOR_THREAD_IMMIX_H
#define MUTATOR_THREAD_IMMIX_H
#include "Allocator.h"
#include "LargeAllocator.h"
#include "shared/ScalaNativeGC.h"
#include "immix_commix/RegistersCapture.h"
#include <stdatomic.h>
#include <stdbool.h>
#include "nativeThreadTLS.h"
#include <stdint.h>

typedef struct {
    _Atomic(GC_MutatorThreadState) state;
    word_t **stackBottom;
    atomic_intptr_t stackTop;
    atomic_bool isWaiting;
    RegistersBuffer registersBuffer;

    // Thread handles for liveness checking and signal delivery
#ifdef _WIN32
    HANDLE threadHandle; // Duplicated handle for liveness checking
#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
    HANDLE wakeupEvent; // Used for thread wake-up (trap mode only)
#endif
#else
    thread_t thread; // pthread_t - used for liveness check and signals
#endif

    // Allocators (immutable after init)
    Allocator allocator;
    LargeAllocator largeAllocator;

    ThreadInfo *threadInfo;
#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
    void **yieldpointTrap;
    /* Faulting PC when using deferred safepoint trampoline (POSIX). */
    uintptr_t safepointResumePc;
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
bool MutatorThread_init(word_t **stackBottom);
/* Register without admitting Scala execution. Used by external attachment. */
bool MutatorThread_register(word_t **stackBottom);
/* Admit an unmanaged mutator under the collector lifecycle lock. */
void MutatorThread_admit(MutatorThread *self);
#ifndef SCALANATIVE_MULTITHREADING_ENABLED
/* Caller holds exclusive runtime ownership, with no suspended Scala frames.
 * Keep the singleton allocator state and bind its stack to the new OS owner. */
bool MutatorThread_rebind(word_t **stackBottom, ThreadInfo *info);
void MutatorThread_suspendContext(MutatorThread *self);
#endif
void MutatorThread_delete(MutatorThread *self);
void MutatorThread_switchState(MutatorThread *self,
                               GC_MutatorThreadState newState);

// =============================================================================
// Thread State Checks
// =============================================================================

// Stack bounds accessors
word_t **MutatorThread_getStackBottom(MutatorThread *thread);
// allowEstimated: if true, may return threadInfo->stackTop when not at
// safepoint May return NULL if not at safepoint and allowEstimated is false
word_t **MutatorThread_getStackTop(MutatorThread *thread, bool allowEstimated);

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
bool MutatorThreads_add(MutatorThread *node);
void MutatorThreads_remove(MutatorThread *node);
void MutatorThreads_lock(void);
void MutatorThreads_unlock(void);
// The synchronizer holds the thread list's lock while it stops and scans the
// threads, Commix's as a read lock; Immix has one lock for both
static inline void MutatorThreads_lockRead(void) { MutatorThreads_lock(); }
static inline void MutatorThreads_unlockRead(void) { MutatorThreads_unlock(); }

#define MutatorThreads_foreach(list, node)                                     \
    for (MutatorThreads node = list; node != NULL; node = node->next)

#endif // MUTATOR_THREAD_IMMIX_H
