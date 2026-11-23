#if defined(SCALANATIVE_GC_IMMIX)
#include "immix_commix/ObjectTracer.h"
#include <stdint.h>
#include <stdio.h>
#include <setjmp.h>
#include "Marker.h"
#include "immix_commix/Object.h"
#include "State.h"
#include "immix_commix/datastructures/Stack.h"
#include "immix_commix/headers/ObjectHeader.h"
#include "Block.h"
#include "shared/GCTypes.h"
#include <stdatomic.h>
#include "shared/ThreadUtil.h"
#include "stackOverflowGuards.h"
#include "Generations.h"

#ifdef SCALANATIVE_GC_STICKY
bool Marker_evacuating = false;
#endif

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
#ifdef SCALANATIVE_GC_STICKY
    if (Marker_evacuating) {
        // Lines are marked once the evacuation decided where it lives.
        if (ObjectMeta_IsPinned(objectMeta))
            ObjectMeta_SetMarkedPinned(objectMeta);
        else
            ObjectMeta_SetMarked(objectMeta);
        Generations_NoteSurvivor(object);
        Stack_Push(stack, object);
        return;
    }
#endif
    Object_Mark(heap, object, objectMeta);
    Stack_Push(stack, object);
}

#ifdef SCALANATIVE_GC_STICKY
// Marks a young object that must keep its address in this collection: a
// conservatively scanned word or a slot the evacuation does not update may
// refer to it. It may be marked already, through a precise reference.
static void Marker_markPinned(Heap *heap, Stack *stack, Object *object) {
    ObjectMeta *objectMeta = Bytemap_Get(heap->bytemap, (word_t *)object);
    if (ObjectMeta_IsAllocated(objectMeta))
        Marker_markObject(heap, stack, heap->bytemap, object, objectMeta);
    if (ObjectMeta_IsMarked(objectMeta))
        ObjectMeta_SetMarkedPinned(objectMeta);
}
#endif

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
            Field_t monitor = Field_allignedLockRef(rttiLock);
#ifdef SCALANATIVE_GC_STICKY
            // Nothing lists the class records, so this slot is not updated.
            if (Marker_evacuating) {
                if (Heap_IsWordInHeap(heap, monitor))
                    Marker_markPinned(heap, stack, (Object *)monitor);
            } else
#endif
                Marker_markField(heap, stack, monitor);
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
#ifdef SCALANATIVE_GC_STICKY
    if (Marker_evacuating) {
        Object *young = Object_GetYoungObject(heap, address);
        if (young != NULL)
            Marker_markPinned(heap, stack, young);
        return;
    }
#endif
    Object *object = Object_GetUnmarkedObject(heap, address);
    Bytemap *bytemap = heap->bytemap;
    if (object != NULL) {
        ObjectMeta *objectMeta = Bytemap_Get(bytemap, (word_t *)object);
        if (ObjectMeta_IsAllocated(objectMeta)) {
            Marker_markObject(heap, stack, bytemap, object, objectMeta);
        }
    }
}

// What the callbacks of shared/GCHooks.h scan into.
typedef struct ScanContext {
    Heap *heap;
    Stack *stack;
} ScanContext;

static void Marker_scanRange(void *ctx, void *low, void *high) {
    ScanContext *context = ctx;
    Marker_markRange(context->heap, context->stack, (word_t **)low,
                     (word_t **)high, sizeof(word_t));
}

void Marker_Mark(Heap *heap, Stack *stack) {
    Bytemap *bytemap = heap->bytemap;
    while (!Stack_IsEmpty(stack)) {
        Object *object = Stack_Pop(stack);
        const Rtti *rtti = object->rtti;
        if (Object_IsArray(object)) {
            ArrayHeader *arrayHeader = (ArrayHeader *)object;
            if (Rtti_ArrayScan(rtti) == RTTI_ARRAY_SCAN_REFERENCES) {
                const size_t length = arrayHeader->length;
                word_t **fields = (word_t **)(arrayHeader + 1);
                for (int i = 0; i < length; i++) {
                    Marker_markField(heap, stack, fields[i]);
                }
            } else if (Rtti_ArrayScan(rtti) == RTTI_ARRAY_SCAN_CONSERVATIVE) {
#ifdef SCALANATIVE_GC_STICKY
                ObjectMeta *blobMeta = Bytemap_Get(bytemap, (word_t *)object);
                if (ObjectMeta_IsMarked(blobMeta)) {
                    Generations_NoteUnbarriered(object);
                    // Listed by address, and holds raw stack words.
                    if (Marker_evacuating)
                        ObjectMeta_SetMarkedPinned(blobMeta);
                }
#endif
                int8_t *start = (int8_t *)(arrayHeader + 1);
                size_t bytesLength = BlobArray_ScannableLimit(arrayHeader);
                size_t wordsLength =
                    (bytesLength + sizeof(word_t) - 1) / sizeof(word_t);
                int8_t *end = start + wordsLength * sizeof(word_t);
                Marker_markRange(heap, stack, (word_t **)start, (word_t **)end,
                                 sizeof(word_t));
            }
            // non-object arrays do not contain pointers
        } else {
            int32_t *refFieldOffsets = object->rtti->refFieldOffsets;
            int i = 0;
            for (; refFieldOffsets[i] != LAST_FIELD_OFFSET; i++) {
                size_t fieldOffset = (size_t)refFieldOffsets[i];
                Field_t *fieldRef = (Field_t *)((int8_t *)object + fieldOffset);
                Marker_markField(heap, stack, *fieldRef);
            }
            // Raw pointer fields that may point into the heap
            for (i++; refFieldOffsets[i] != LAST_FIELD_OFFSET; i++) {
                word_t *rawPtr =
                    *(word_t **)((int8_t *)object + refFieldOffsets[i]);
                if (Heap_IsWordInHeap(heap, rawPtr)) {
                    Marker_markConservative(heap, stack, rawPtr);
                }
            }
            if (UNLIKELY(Rtti_HasFlag(rtti, RTTI_FLAG_HAS_OBJECT_TRACER))) {
#ifdef SCALANATIVE_GC_STICKY
                // What the tracer scans changes without the write barrier
                ObjectMeta *tracedMeta = Bytemap_Get(bytemap, (word_t *)object);
                if (ObjectMeta_IsMarked(tracedMeta)) {
                    Generations_NoteUnbarriered(object);
                    // Listed by address, which the tracer's owner also keeps.
                    if (Marker_evacuating)
                        ObjectMeta_SetMarkedPinned(tracedMeta);
                }
#endif
                // Not set before the first owner registers it
                GC_ObjectTracerFn *trace = atomic_load(&ObjectTracer_trace);
                if (trace != NULL) {
                    ScanContext context = {heap, stack};
                    trace((void **)object->fields, Marker_scanRange, &context);
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
    ubyte_t *alignedTo = (ubyte_t *)((intptr_t)to & alignmentMask);
    if (alignedFrom >= alignedTo) {
        return; // No range to scan
    }
    for (ubyte_t *current = alignedFrom; current < alignedTo;
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
        stackTop = MutatorThread_getStackTop(thread, false);
    } while (stackTop == NULL);
    stackBottom = (word_t **)threadStackScanBottom(thread->threadInfo, stackTop,
                                                   stackBottom);
#ifdef SCALANATIVE_THREAD_ALT_STACK
    // Keep the captured SP until stack bounds are known.
    if (thread->threadInfo != NULL && thread->threadInfo->stackTop != NULL &&
        thread->threadInfo->stackBottom != NULL &&
        (isOnSignalHandlerStack(thread->threadInfo, stackTop) ||
         !isOnThreadStack(thread->threadInfo, stackTop))) {
        void *stackScanLimit = threadStackScanableLimit(thread->threadInfo);
        // Host stacks may lack a Scala guard.
        stackTop =
            (word_t **)(stackScanLimit != NULL ? stackScanLimit
                                               : thread->threadInfo->stackTop);
        if (thread->threadInfo->signalHandlerStack != NULL) {
            // Signal stacks can retain managed references.
            word_t **signalHandlerStack =
                (word_t **)thread->threadInfo->signalHandlerStack;
            Marker_markRange(
                heap, stack, signalHandlerStack,
                (word_t **)((char *)signalHandlerStack +
                            thread->threadInfo->signalHandlerStackSize),
                sizeof(word_t));
        }
    }
#endif
    StackScanRange ranges[2];
    int nRanges = threadStackScanableRanges(stackTop, stackBottom,
                                            thread->threadInfo, ranges);
    for (int i = 0; i < nRanges; i++)
        Marker_markRange(heap, stack, (word_t **)ranges[i].from,
                         (word_t **)ranges[i].to, sizeof(word_t));

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

void Marker_markCustomRoots(Heap *heap, Stack *stack, GC_Roots *roots) {
    mutex_lock(&roots->modificationLock);
    for (GC_Root *it = roots->head; it != NULL; it = it->next) {
        Marker_markRange(heap, stack, (word_t **)it->range.address_low,
                         (word_t **)it->range.address_high, sizeof(word_t));
    }
    mutex_unlock(&roots->modificationLock);
}

void Marker_MarkRemembered(Heap *heap, Stack *stack, Object *object) {
    Marker_markLockWords(heap, stack, object);
    Stack_Push(stack, object);
}

void Marker_MarkSlots(Heap *heap, Stack *stack, word_t **from, word_t **to) {
    for (word_t **slot = from; slot < to; slot++)
        Marker_markField(heap, stack, *slot);
}

void Marker_MarkRoots(Heap *heap, Stack *stack, bool minor) {
    atomic_thread_fence(memory_order_seq_cst);

    MutatorThreadNode *head = mutatorThreads;
    MutatorThreads_foreach(mutatorThreads, node) {
        MutatorThread *thread = node->value;
        Marker_markProgramStack(thread, heap, stack);
    }
    Marker_markCustomRoots(heap, stack, customRoots);
    ScanContext context = {heap, stack};
    GCHooks_scanDynamicRoots(Marker_scanRange, &context);
#ifdef SCALANATIVE_GC_STICKY
    if (minor)
        Generations_MarkMinorRoots(heap, stack);
#endif
    Marker_Mark(heap, stack);
}

#endif
