#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX)

#include "immix_commix/CollectorState.h"
#include <stdlib.h>
#include <string.h>
#include <stdatomic.h>
#include <stdint.h>
#include "shared/ThreadUtil.h"
#include "shared/Log.h"
#include <assert.h>

#ifndef _WIN32
#include <errno.h>
#include <signal.h>
#include <unistd.h>
#ifdef __linux__
#include <sys/syscall.h>
#endif
#endif

#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
#include "shared/YieldPointTrap.h"
#endif

static void MutatorThread_dispose(MutatorThread *self) {
#ifdef _WIN32
    if (self->threadHandle != NULL)
        CloseHandle(self->threadHandle);
#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
    if (self->wakeupEvent != NULL)
        CloseHandle(self->wakeupEvent);
#endif
#endif
#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
    if (self->yieldpointTrap != NULL)
        YieldPointTrap_free(self->yieldpointTrap);
#endif
    free(self);
}

bool MutatorThread_register(Field_t *stackbottom) {
    if (currentMutatorThread != NULL)
        return false;
    MutatorThread *self = (MutatorThread *)calloc(1, sizeof(MutatorThread));
    if (self == NULL)
        return false;
    currentMutatorThread = self;
    self->threadInfo = &currentThreadInfo;

    self->stackBottom = stackbottom;
    // Store thread handle for liveness checking and signal delivery
#ifdef _WIN32
    // Duplicate the current thread handle so it remains valid even if
    // the original thread handle becomes invalid
    HANDLE currentThread = GetCurrentThread();
    HANDLE currentProcess = GetCurrentProcess();
    if (!DuplicateHandle(currentProcess, currentThread, currentProcess,
                         &self->threadHandle, 0, FALSE,
                         DUPLICATE_SAME_ACCESS)) {
        GC_LOG_ERROR("Failed to duplicate mutator thread handle: errno=%lu",
                     GetLastError());
        goto failure;
    }
#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
    self->wakeupEvent = CreateEvent(NULL, true, false, NULL);
    if (self->wakeupEvent == NULL) {
        GC_LOG_ERROR("Failed to setup mutator thread wakeup event: errno=%lu",
                     GetLastError());
        goto failure;
    }
#endif // SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
#else
    // Always store thread identifier for liveness checking
    self->thread = pthread_self();
#ifdef __linux__
    self->kernelThreadId = (pid_t)syscall(SYS_gettid);
#endif
#endif

#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
    self->yieldpointTrap = YieldPointTrap_init();
    if (self->yieldpointTrap == NULL)
        goto failure;
    YieldPointTrap_disarm(self->yieldpointTrap);
    scalanative_GC_yieldpoint_trap = self->yieldpointTrap;
#endif

    /* Publication must not make GC wait for an unadmitted thread. */
    atomic_init(&self->state, GC_MutatorThreadState_Unmanaged);
    MutatorThread_switchState(self, GC_MutatorThreadState_Unmanaged);
    Allocator_Init(&self->allocator, &blockAllocator, heap.bytemap,
                   heap.blockMetaStart, heap.heapStart);

    LargeAllocator_Init(&self->largeAllocator, &blockAllocator, heap.bytemap,
                        heap.blockMetaStart, heap.heapStart);
    if (!MutatorThreads_add(self))
        goto failure;
    return true;

failure:
    currentMutatorThread = NULL;
#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
    scalanative_GC_yieldpoint_trap = NULL;
#endif
    MutatorThread_dispose(self);
    return false;
}

bool MutatorThread_init(Field_t *stackbottom) {
    if (!MutatorThread_register(stackbottom))
        return false;
    MutatorThread_admit(currentMutatorThread);
    return true;
}

#ifndef SCALANATIVE_MULTITHREADING_ENABLED
bool MutatorThread_rebind(word_t **stackBottom, ThreadInfo *info) {
    MutatorThread *self = currentMutatorThread;
    if (self == NULL || self->state != GC_MutatorThreadState_Unmanaged)
        return false;
#ifdef _WIN32
    HANDLE handle;
    HANDLE process = GetCurrentProcess();
    if (!DuplicateHandle(process, GetCurrentThread(), process, &handle, 0,
                         FALSE, DUPLICATE_SAME_ACCESS))
        return false;
    if (self->threadHandle != NULL)
        CloseHandle(self->threadHandle);
    self->threadHandle = handle;
#else
    self->thread = pthread_self();
#ifdef __linux__
    self->kernelThreadId = (pid_t)syscall(SYS_gettid);
#endif
#endif
    self->stackBottom = stackBottom;
    self->threadInfo = info;
    /* Capture this owner's registers and stack, replacing the old snapshot. */
    MutatorThread_switchState(self, GC_MutatorThreadState_Unmanaged);
    return true;
}
#endif

void MutatorThread_delete(MutatorThread *self) {
    MutatorThread_switchState(self, GC_MutatorThreadState_Unmanaged);
    MutatorThreads_remove(self);

    assert(currentMutatorThread == self);
    currentMutatorThread = NULL;
#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
    scalanative_GC_yieldpoint_trap = NULL;
#endif
    MutatorThread_dispose(self);
}

typedef word_t **stackptr_t;

NOINLINE static stackptr_t MutatorThread_approximateStackTop() {
    volatile word_t sp = 0;
    sp = (word_t)&sp;
    /* Also force stack to grow if necessary. Otherwise the later accesses might
     * cause the kernel to think we're doing something wrong. */
    assert(sp > 0);
    return (stackptr_t)sp;
}

INLINE void MutatorThread_switchState(MutatorThread *self,
                                      GC_MutatorThreadState newState) {
    assert(self != NULL);
    switch (newState) {
    case GC_MutatorThreadState_Unmanaged:
        RegistersCapture(self->registersBuffer);
        atomic_store_explicit(&self->stackTop,
                              (intptr_t)MutatorThread_approximateStackTop(),
                              memory_order_release);
        break;

    case GC_MutatorThreadState_Managed:
        atomic_store_explicit(&self->stackTop, 0, memory_order_release);
        break;
    }
    self->state = newState;
}

word_t **MutatorThread_getStackBottom(MutatorThread *thread) {
    word_t **bottom = thread->stackBottom;
    if (thread->threadInfo != NULL && thread->threadInfo->stackBottom != NULL &&
        (uintptr_t)thread->threadInfo->stackBottom > (uintptr_t)bottom) {
        bottom = (word_t **)thread->threadInfo->stackBottom;
    }
    return bottom;
}

word_t **MutatorThread_getStackTop(MutatorThread *thread, bool allowEstimated) {
    intptr_t sp = atomic_load_explicit(&thread->stackTop, memory_order_acquire);
    if (sp != 0)
        return (word_t **)sp;
    if (allowEstimated && thread->threadInfo != NULL &&
        thread->threadInfo->stackTop != NULL)
        return (word_t **)thread->threadInfo->stackTop;
    return NULL;
}

bool MutatorThread_isAtSafepoint(MutatorThread *thread) {
    // A thread is at safepoint when stackTop is non-NULL
    // This means it has switched to Unmanaged state and saved its
    // stack/registers
    return atomic_load_explicit(&thread->stackTop, memory_order_acquire) != 0;
}

// Conservative liveness: a thread counts as alive unless it is known to be
// gone. Used for diagnostics only.
bool MutatorThread_isAlive(MutatorThread *thread) {
    return !MutatorThread_hasTerminated(thread);
}

bool MutatorThread_hasTerminated(MutatorThread *thread) {
    if (thread == NULL)
        return false;
#ifdef _WIN32
    if (thread->threadHandle == NULL)
        return false;
    DWORD exitCode;
    if (!GetExitCodeThread(thread->threadHandle, &exitCode))
        return false;
    return exitCode != STILL_ACTIVE;
#else
#ifdef __linux__
    /* Java threads are detached, so their pthread_t dangles once they exit.
     * tgkill takes the kernel id instead and never reads that descriptor.
     * Only ESRCH proves the thread is gone; on any other answer liveness
     * stays unknown and the caller keeps waiting. */
    return syscall(SYS_tgkill, getpid(), thread->kernelThreadId, 0) == -1 &&
           errno == ESRCH;
#else
    return pthread_kill(thread->thread, 0) == ESRCH;
#endif
#endif
}

void MutatorThread_reap(MutatorThread *thread) {
    if (thread == NULL)
        return;
    /* No switchState here: it would capture the caller's stack, not the
     * dead thread's. The caller must not hold the list read lock. */
    MutatorThreads_remove(thread);
    MutatorThread_dispose(thread);
}

void MutatorThreads_afterForkChild(void) {
    /* Drop the mutators of OS threads that fork() did not copy, and reinit
     * the list lock in case one of them held it. Their records are leaked
     * on purpose: freeing them would also have to return their heap blocks,
     * and the next collection reclaims those anyway. */
    MutatorThreads kept = NULL;
    for (MutatorThreads node = mutatorThreads; node != NULL;
         node = node->next) {
        if (node->value == currentMutatorThread)
            kept = node;
    }
    // Commix initialization clears the list, so save the survivor first.
    MutatorThreads_init();
    if (kept != NULL) {
        kept->next = NULL;
#ifdef __linux__
        /* The child is a new task, so the recorded id belongs to the parent.
         * Without this the liveness probe would report the caller dead. */
        kept->value->kernelThreadId = (pid_t)syscall(SYS_gettid);
#endif
    }
    mutatorThreads = kept;
#ifdef SCALANATIVE_GC_COMMIX
    atomic_store_explicit(&mutatorThreadsCount, kept != NULL ? 1 : 0,
                          memory_order_relaxed);
#endif
}

#endif
