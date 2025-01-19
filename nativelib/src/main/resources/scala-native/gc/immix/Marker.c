#if defined(SCALANATIVE_GC_IMMIX)
#include <stdint.h>
#include <stdio.h>
#include <setjmp.h>
#include "Marker.h"
#include "Object.h"
#include "immix_commix/Log.h"
#include "State.h"
#include "datastructures/Stack.h"
#include "immix_commix/headers/ObjectHeader.h"
#include "Block.h"
#include "shared/GCTypes.h"
#include <stdatomic.h>
#include "shared/ThreadUtil.h"
#include "stackOverflowGuards.h"

extern word_t *__modules;
extern int __modules_size;

#define LAST_FIELD_OFFSET -1

static inline void Marker_markLockWords(Heap *heap, Stack *stack,
                                        Object *object);
static void Marker_markRange(Heap *heap, Stack *stack, word_t **from,
                             word_t **to, const size_t stride);

void Marker_markObject(Heap *heap, Stack *stack, Bytemap *bytemap,
                       Object *object, ObjectMeta *objectMeta) {
    assert(ObjectMeta_IsAllocated(objectMeta));
    assert(object->rtti != NULL);

    Marker_markLockWords(heap, stack, object);
    if (Object_IsWeakReference(object)) {
        // Added to the WeakReference stack for additional later visit
        Stack_Push(&weakRefStack, object);
    }

    assert(Object_Size(object) != 0);
    Object_Mark(heap, object, objectMeta);
    Stack_Push(stack, object);
}

static inline void Marker_markField(Heap *heap, Stack *stack, Field_t field) {
    if (Heap_IsWordInHeap(heap, field)) {
        ObjectMeta *fieldMeta = Bytemap_Get(heap->bytemap, field);
        if (ObjectMeta_IsAllocated(fieldMeta)) {
            Object *object = (Object *)field;
            Marker_markObject(heap, stack, heap->bytemap, object, fieldMeta);
        }
    }
}

/* If compiling with enabled lock words check if object monitor is inflated and
 * can be marked. Otherwise, in singlethreaded mode this funciton is no-op
 */
static inline void Marker_markLockWords(Heap *heap, Stack *stack,
                                        Object *object) {
#ifdef USES_LOCKWORD
    if (object != NULL) {
        Field_t rttiLock = object->rtti->rt.lockWord;
        if (Field_isInflatedLock(rttiLock)) {
            Marker_markField(heap, stack, Field_allignedLockRef(rttiLock));
        }

        Field_t objectLock = object->lockWord;
        if (Field_isInflatedLock(objectLock)) {
            Field_t field = Field_allignedLockRef(objectLock);
            Marker_markField(heap, stack, field);
        }
    }
#endif
}

void Marker_markConservative(Heap *heap, Stack *stack, word_t *address) {
    assert(Heap_IsWordInHeap(heap, address));
    if (Bytemap_isPtrAligned(address)) {
        Object *object = Object_GetUnmarkedObject(heap, address);
        Bytemap *bytemap = heap->bytemap;
        if (object != NULL) {
            ObjectMeta *objectMeta = Bytemap_Get(bytemap, (word_t *)object);
            if (ObjectMeta_IsAllocated(objectMeta)) {
                Marker_markObject(heap, stack, bytemap, object, objectMeta);
            }
        }
    }
}

void Marker_Mark(Heap *heap, Stack *stack) {
    Bytemap *bytemap = heap->bytemap;
    while (!Stack_IsEmpty(stack)) {
        Object *object = Stack_Pop(stack);
        const int objectId = object->rtti->rt.id;
        if (Object_IsArray(object)) {
            ArrayHeader *arrayHeader = (ArrayHeader *)object;
            if (objectId == __object_array_id) {
                const size_t length = arrayHeader->length;
                word_t **fields = (word_t **)(arrayHeader + 1);
                for (int i = 0; i < length; i++) {
                    Marker_markField(heap, stack, fields[i]);
                }
            } else if (objectId == __blob_array_id) {
                int8_t *start = (int8_t *)(arrayHeader + 1);
                int8_t *end = start + BlobArray_ScannableLimit(arrayHeader);
                Marker_markRange(heap, stack, (word_t **)start, (word_t **)end,
                                 sizeof(word_t));
            }
            // non-object arrays do not contain pointers
        } else {
            int32_t *refFieldOffsets = object->rtti->refFieldOffsets;
            for (int i = 0; refFieldOffsets[i] != LAST_FIELD_OFFSET; i++) {
                size_t fieldOffset = (size_t)refFieldOffsets[i];
                Field_t *fieldRef = (Field_t *)((int8_t *)object + fieldOffset);
                if (Object_IsReferantOfWeakReference(object, fieldOffset))
                    continue;
                Marker_markField(heap, stack, *fieldRef);
            }
            if (objectId == __boxed_ptr_id) {
                // Boxed ptr always has a single field
                word_t *rawPtr = object->fields[0];
                if (Heap_IsWordInHeap(heap, rawPtr)) {
                    Marker_markConservative(heap, stack, rawPtr);
                }
            }
        }
    }
}

NO_SANITIZE static void Marker_markRange(Heap *heap, Stack *stack,
                                         word_t **from, word_t **to,
                                         const size_t stride) {
    assert(from != NULL);
    assert(to != NULL);
    if (from > to) {
        word_t **tmp = from;
        from = to;
        to = tmp;
    }
    // Align start address
    const intptr_t alignmentMask = ~(sizeof(word_t) - 1);
    ubyte_t *alignedFrom = (ubyte_t *)((intptr_t)from & alignmentMask);
    // Align end address to be optionally 1 higher when unaligned
    ubyte_t *alignedTo = (ubyte_t *)((intptr_t)(to + 1) & alignmentMask);
    for (ubyte_t *current = alignedFrom; current <= alignedTo;
         current += stride) {
        word_t *addr = *(word_t **)current;
        if (Heap_IsWordInHeap(heap, addr)) {
            Marker_markConservative(heap, stack, addr);
        }
    }
}

NO_SANITIZE void Marker_markProgramStack(MutatorThread *thread, Heap *heap,
                                         Stack *stack) {
    word_t **stackBottom = thread->stackBottom;
    word_t **stackTop = NULL;
    do {
        // Can spuriously fail, very rare, yet deadly
        stackTop = (word_t **)atomic_load_explicit(&thread->stackTop,
                                                   memory_order_acquire);
    } while (stackTop == NULL);
#ifdef SCALANATIVE_THREAD_ALT_STACK
    // If signal handler is executing in alternative stack we need to mark the
    // whole thread stack
    word_t **_stackTop = stackTop;
    if (!isInRange(stackTop, thread->threadInfo->stackTop,
                   thread->threadInfo->stackBottom)) {
        // Area between thread-stackTop and stackGaurdPage might be guarded
        void *stackScanLimit = threadStackScanableLimit(thread->threadInfo);
        stackTop =
            (stackScanLimit != NULL)
                ? stackScanLimit
                : stackBottom - 64 * 1024; // not yet initialized, approximate
                                           // safe scanning limit
        printf("Replaced stackTop %p with %p, scanLimit=%p\n", _stackTop,
               stackTop, stackScanLimit);
        if (thread->threadInfo->signalHandlerStack != NULL) {
            // Marking alternative stack should not be needed, but tests showed
            // that it might contain some pointer to managed object
            word_t **signalHandlerStack =
                thread->threadInfo->signalHandlerStack;
            printf("mark alt stack: {%p %p}\n", signalHandlerStack,
                   ((char *)signalHandlerStack +
                    thread->threadInfo->signalHandlerStackSize));
            Marker_markRange(
                heap, stack, signalHandlerStack,
                (word_t **)((char *)signalHandlerStack +
                            thread->threadInfo->signalHandlerStackSize),
                sizeof(word_t));
        }
    }
#endif
    printf("mark stack: {%p %p}\n", stackTop, stackBottom);
    Marker_markRange(heap, stack, stackTop, stackBottom, sizeof(word_t));
    printf("marked\n");

    // Mark registers buffer
    size_t registerBufferStride =
#if defined(CAPTURE_SETJMP)
        // Pointers in jmp_bufr might be non word-size aligned
        sizeof(uint32_t);
#else
        sizeof(word_t);
#endif
    Marker_markRange(heap, stack, (word_t **)&thread->registersBuffer,
                     (word_t **)(&thread->registersBuffer + 1),
                     registerBufferStride);
}

void Marker_markModules(Heap *heap, Stack *stack) {
    word_t **modules = &__modules;
    int nb_modules = __modules_size;
    Bytemap *bytemap = heap->bytemap;
    for (int i = 0; i < nb_modules; i++) {
        Object *object = (Object *)modules[i];
        Marker_markField(heap, stack, (Field_t)object);
    }
}

void Marker_markCustomRoots(Heap *heap, Stack *stack, GC_Roots *roots) {
    mutex_lock(&roots->modificationLock);
    for (GC_Root *it = roots->head; it != NULL; it = it->next) {
        Marker_markRange(heap, stack, (word_t **)it->range.address_low,
                         (word_t **)it->range.address_high, sizeof(word_t));
    }
    mutex_unlock(&roots->modificationLock);
}

void Marker_MarkRoots(Heap *heap, Stack *stack) {
    atomic_thread_fence(memory_order_seq_cst);

    MutatorThreadNode *head = mutatorThreads;
    MutatorThreads_foreach(mutatorThreads, node) {
        MutatorThread *thread = node->value;
        Marker_markProgramStack(thread, heap, stack);
    }
    Marker_markModules(heap, stack);
    Marker_markCustomRoots(heap, stack, customRoots);
    Marker_Mark(heap, stack);
}

#endif
