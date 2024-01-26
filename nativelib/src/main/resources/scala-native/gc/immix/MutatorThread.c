#if defined(SCALANATIVE_GC_IMMIX)

#include "MutatorThread.h"
#include "State.h"
#include <stdlib.h>
#include <stdatomic.h>
#include <setjmp.h>
#include "shared/ThreadUtil.h"
#include <assert.h>

static mutex_t threadListsModificationLock;

void MutatorThread_init(Field_t *stackbottom) {
    MutatorThread *self = (MutatorThread *)malloc(sizeof(MutatorThread));
    memset(self, 0, sizeof(MutatorThread));
    currentMutatorThread = self;

    self->stackBottom = stackbottom;
#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
#ifdef _WIN32
    self->wakeupEvent = CreateEvent(NULL, true, false, NULL);
    if (self->wakeupEvent == NULL) {
        fprintf(stderr, "Failed to setup mutator thread: errno=%lu\n",
                GetLastError());
        exit(1);
    }
#else
    self->thread = pthread_self();
#endif
#endif // SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
    MutatorThread_switchState(self, GC_MutatorThreadState_Managed);
    Allocator_Init(&self->allocator, &blockAllocator, heap.bytemap,
                   heap.blockMetaStart, heap.heapStart);

    LargeAllocator_Init(&self->largeAllocator, &blockAllocator, heap.bytemap,
                        heap.blockMetaStart, heap.heapStart);
    MutatorThreads_add(self);
    // Following init operations might trigger GC, needs to be executed after
    // acknownleding the new thread in MutatorThreads_add
    Allocator_InitCursors(&self->allocator);
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    // Stop if there is ongoing GC_collection
    scalanative_GC_yield();
#endif
}

void MutatorThread_delete(MutatorThread *self) {
    MutatorThread_switchState(self, GC_MutatorThreadState_Unmanaged);
    MutatorThreads_remove(self);
#if defined(SCALANATIVE_GC_USE_YIELDPOINT_TRAPS) && defined(_WIN32)
    CloseHandle(self->wakeupEvent);
#endif
    free(self);
}

typedef word_t **stackptr_t;

INLINE static stackptr_t MutatorThread_approximateStackTop() {
    volatile word_t sp = 1;
    sp = (word_t)&sp;
    /* Also force stack to grow if necessary. Otherwise the later accesses might
     * cause the kernel to think we're doing something wrong. */
    return (stackptr_t)sp;
}

INLINE void MutatorThread_switchState(MutatorThread *self,
                                      GC_MutatorThreadState newState) {
    assert(self != NULL);
    intptr_t newStackTop = 0;
    if (newState == GC_MutatorThreadState_Unmanaged) {
        // Dump registers to allow for their marking later
        RegistersCapture(self->registersBuffer);
        newStackTop = (intptr_t)MutatorThread_approximateStackTop();
    }
    atomic_store_explicit(&self->stackTop, newStackTop, memory_order_release);
    self->state = newState;
}

void MutatorThreads_init() { mutex_init(&threadListsModificationLock); }

void MutatorThreads_add(MutatorThread *node) {
    if (!node)
        return;
    MutatorThreadNode *newNode =
        (MutatorThreadNode *)malloc(sizeof(MutatorThreadNode));
    newNode->value = node;
    MutatorThreads_lock();
    newNode->next = atomic_load_explicit(&mutatorThreads, memory_order_acquire);
    atomic_store_explicit(&mutatorThreads, newNode, memory_order_release);
    MutatorThreads_unlock();
}

void MutatorThreads_remove(MutatorThread *node) {
    if (!node)
        return;

    MutatorThreads_lock();
    MutatorThreads current =
        atomic_load_explicit(&mutatorThreads, memory_order_acquire);
    if (current->value == node) { // expected is at head
        atomic_store_explicit(&mutatorThreads, current->next,
                              memory_order_release);
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
    MutatorThreads_unlock();
}

void MutatorThreads_lock() { mutex_lock(&threadListsModificationLock); }

void MutatorThreads_unlock() { mutex_unlock(&threadListsModificationLock); }

#endif
