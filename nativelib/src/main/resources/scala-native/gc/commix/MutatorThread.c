#if defined(SCALANATIVE_GC_COMMIX)

#include "MutatorThread.h"
#include "State.h"
#include <stdlib.h>
#include <stdatomic.h>
#include <setjmp.h>
#include <shared/ThreadUtil.h>
#include <assert.h>

static rwlock_t threadListsModificationLock;

void MutatorThread_init(Field_t *stackbottom) {
    MutatorThread *self = (MutatorThread *)malloc(sizeof(MutatorThread));
    memset(self, 0, sizeof(MutatorThread));
    currentMutatorThread = self;

    self->stackBottom = stackbottom;
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
    MutatorThread_switchState(self, MutatorThreadState_Managed);
    Allocator_Init(&self->allocator, &blockAllocator, heap.bytemap,
                   heap.blockMetaStart, heap.heapStart);

    LargeAllocator_Init(&self->largeAllocator, &blockAllocator, heap.bytemap,
                        heap.blockMetaStart, heap.heapStart);
    MutatorThreads_add(self);
    mutatorThreadsCount += 1;
    // Following init operations might trigger GC, needs to be executed after
    // acknownleding the new thread in MutatorThreads_add
    Allocator_InitCursors(&self->allocator);
}

void MutatorThread_delete(MutatorThread *self) {
    MutatorThread_switchState(self, MutatorThreadState_Unmanaged);
    MutatorThreads_remove(self);
    mutatorThreadsCount -= 1;
#ifdef _WIN32
    CloseHandle(self->wakeupEvent);
#endif
    free(self);
}

typedef word_t **volatile stackptr_t;

NOINLINE static stackptr_t MutatorThread_approximateStackTop() {
    volatile word_t sp;
    sp = (word_t)&sp;
    /* Also force stack to grow if necessary. Otherwise the later accesses might
     * cause the kernel to think we're doing something wrong. */
    return (stackptr_t)sp;
}

void MutatorThread_switchState(MutatorThread *self,
                               MutatorThreadState newState) {
    assert(self != NULL);
    if (newState == MutatorThreadState_Unmanaged) {
        // Dump registers to allow for their marking later
        setjmp(self->executionContext);
        self->stackTop = MutatorThread_approximateStackTop();
    } else {
        self->stackTop = NULL;
    }
    self->state = newState;
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
    newNode->next = atomic_load_explicit(&mutatorThreads, memory_order_acquire);
    atomic_store_explicit(&mutatorThreads, newNode, memory_order_release);
    MutatorThreads_unlockWrite();
}

void MutatorThreads_remove(MutatorThread *node) {
    if (!node)
        return;

    MutatorThreads_lockWrite();
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
    MutatorThreads_unlockWrite();
}

#endif