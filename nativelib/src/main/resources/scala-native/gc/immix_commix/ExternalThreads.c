#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX)
#include "externalThreadGC.h"
#include <assert.h>
#ifdef SCALANATIVE_GC_IMMIX
#include "immix/State.h"
#else
#include "commix/State.h"
#endif

bool scalanative_externalGC_isRegistered(void) {
    return currentMutatorThread != NULL;
}

bool scalanative_externalGC_register(void) {
    return MutatorThread_register(currentThreadInfo.stackBottom);
}

bool scalanative_externalGC_rebind(void) {
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    return currentMutatorThread != NULL;
#else
    return MutatorThread_rebind(currentThreadInfo.stackBottom,
                                &currentThreadInfo);
#endif
}

void scalanative_externalGC_unregister(void) {
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    MutatorThread_delete(currentMutatorThread);
#else
    // Retain allocator cursors, but stop scanning the relinquished OS stack
    // and its record, which may be destroyed before the next owner arrives.
    MutatorThread_suspendContext(currentMutatorThread);
#endif
    /* A single-threaded context owns allocator cursors across OS owners. */
}

void scalanative_externalGC_admit(void) {
    MutatorThread_admit(currentMutatorThread);
}

GC_MutatorThreadState scalanative_externalGC_state(void) {
    return atomic_load_explicit(&currentMutatorThread->state,
                                memory_order_acquire);
}

bool scalanative_externalGC_addRoots(void *low, void *high) {
    return GC_Roots_Add(customRoots, (AddressRange){low, high});
}

void *scalanative_externalGC_saveContext(ThreadInfo *info) {
    assert(scalanative_externalGC_state() ==
           GC_MutatorThreadState_Unmanaged);
#ifdef SCALANATIVE_GC_IMMIX
    MutatorThreads_lock();
    currentMutatorThread->threadInfo = info;
    MutatorThreads_unlock();
#else
    MutatorThread_setThreadInfo(currentMutatorThread, info);
#endif
    return currentMutatorThread;
}

bool scalanative_externalGC_restoreContext(void *context) {
    currentMutatorThread = context;
#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
    scalanative_GC_yieldpoint_trap = currentMutatorThread->yieldpointTrap;
#endif
    return context != NULL;
}
#endif
