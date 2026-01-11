#if defined(SCALANATIVE_GC_COMMIX)
#include "shared/ScalaNativeGC.h"
#include "MutatorThread.h"
#include "State.h"
#include <stdlib.h>
#include <stdatomic.h>
#include "shared/ThreadUtil.h"
#include "shared/Log.h"
#include <assert.h>

#ifndef _WIN32
#include <signal.h>
#endif

static rwlock_t threadListsModificationLock;

void MutatorThread_init(Field_t *stackbottom) {
    MutatorThread *self = (MutatorThread *)malloc(sizeof(MutatorThread));
    memset(self, 0, sizeof(MutatorThread));
    currentMutatorThread = self;
#ifdef SCALANATIVE_THREAD_ALT_STACK
    self->threadInfo = &currentThreadInfo;
#endif

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
        GC_LOG_WARN("Failed to duplicate thread handle, liveness check will "
                    "not work: errno=%lu",
                    GetLastError());
        // Continue anyway - liveness check will return true (assume alive)
        self->threadHandle = NULL;
    }
#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
    self->wakeupEvent = CreateEvent(NULL, true, false, NULL);
    if (self->wakeupEvent == NULL) {
        GC_LOG_ERROR("Failed to setup mutator thread wakeup event: errno=%lu",
                     GetLastError());
        exit(1);
    }
#endif // SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
#else
    // Always store pthread_t for liveness checking
    self->thread = pthread_self();
#endif

    MutatorThread_switchState(self, GC_MutatorThreadState_Managed);
    Allocator_Init(&self->allocator, &blockAllocator, heap.bytemap,
                   heap.blockMetaStart, heap.heapStart);

    LargeAllocator_Init(&self->largeAllocator, &blockAllocator, heap.bytemap,
                        heap.blockMetaStart, heap.heapStart);
    MutatorThreads_add(self);
    atomic_fetch_add(&mutatorThreadsCount, 1);
    // Following init operations might trigger GC, needs to be executed after
    // acknownleding the new thread in MutatorThreads_add
    Allocator_InitCursors(&self->allocator, true);
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    // Stop if there is ongoing GC_collection
    scalanative_GC_yield();
#endif
}

void MutatorThread_delete(MutatorThread *self) {
    MutatorThread_switchState(self, GC_MutatorThreadState_Unmanaged);
    MutatorThreads_remove(self);
    atomic_fetch_add(&mutatorThreadsCount, -1);

#ifdef _WIN32
    if (self->threadHandle != NULL) {
        CloseHandle(self->threadHandle);
    }
#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
    CloseHandle(self->wakeupEvent);
#endif
#endif // WIN32

    free(self);
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

bool MutatorThread_isAtSafepoint(MutatorThread *thread) {
    // A thread is at safepoint when stackTop is non-NULL
    // This means it has switched to Unmanaged state and saved its
    // stack/registers
    return atomic_load_explicit(&thread->stackTop, memory_order_acquire) != 0;
}

bool MutatorThread_isAlive(MutatorThread *thread) {
#ifdef _WIN32
    if (thread->threadHandle == NULL) {
        // No handle available, assume thread is alive (maybe not yet
        // initialized)
        return true;
    }
    DWORD exitCode;
    if (GetExitCodeThread(thread->threadHandle, &exitCode)) {
        return exitCode == STILL_ACTIVE;
    }
    // If we can't get exit code, assume thread is alive
    return true;
#else
    // pthread_kill with signal 0 checks if thread exists without sending signal
    int result = pthread_kill(thread->thread, 0);
    return result == 0;
#endif
}

void MutatorThreads_lockRead() {
    rwlock_lockRead(&threadListsModificationLock);
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

void MutatorThreads_init() {
    rwlock_init(&threadListsModificationLock);
    atomic_init(&mutatorThreads, NULL);
}

void MutatorThreads_add(MutatorThread *node) {
    if (!node)
        return;
    MutatorThreadNode *newNode =
        (MutatorThreadNode *)malloc(sizeof(MutatorThreadNode));
    newNode->value = node;
    MutatorThreads_lockWrite();
    newNode->next = mutatorThreads;
    mutatorThreads = newNode;
    MutatorThreads_unlockWrite();
}

void MutatorThreads_remove(MutatorThread *node) {
    if (!node)
        return;

    MutatorThreads_lockWrite();
    MutatorThreads current = mutatorThreads;
    if (current->value == node) { // expected is at head
        mutatorThreads = current->next;
        free(current);
    } else {
        while (current->next && current->next->value != node) {
            current = current->next;
        }
        MutatorThreads next = current->next;
        if (next) {
            current->next = next->next;
            free(next);
            atomic_thread_fence(memory_order_release);
        }
    }
    MutatorThreads_unlockWrite();
}

#endif
