#include <stdio.h>
#include <setjmp.h>
#include "Marker.h"
#include "Object.h"
#include "Log.h"
#include "State.h"
#include "datastructures/Stack.h"
#include "headers/ObjectHeader.h"
#include "Block.h"

#define UNW_LOCAL_ONLY
#include <libunwind.h>

extern int __object_array_id;
extern word_t *__modules;
extern int __modules_size;

void mark(Heap *heap, Stack *stack);
void largeHeapOverflowHeapScan(Heap *heap, Stack *stack);
bool smallHeapOverflowHeapScan(Heap *heap, Stack *stack);

void markObject(Heap *heap, Stack *stack, Object *object) {
    assert(!Object_isMarked(&object->header));
    assert(Object_size(&object->header) != 0);
    Object_mark(object);
    if (!overflow) {
        overflow = Stack_push(stack, object);
    }
}

void markConservative(Heap *heap, Stack *stack, word_t *address) {
    assert(heap_isWordInHeap(heap, address));
    Object *object = NULL;
    if (heap_isWordInSmallHeap(heap, address)) {
        object = Object_getObject(address);
        assert(
            object == NULL ||
            Line_containsObject(&Block_getBlockHeader((word_t *)object)
                                     ->lineHeaders[Block_getLineIndexFromWord(
                                         Block_getBlockHeader((word_t *)object),
                                         (word_t *)object)]));
#ifdef DEBUG_PRINT
        if (object == NULL) {
            printf("Not found: %p\n", address);
        }
#endif
    } else {
        object = Object_getLargeObject(heap->largeAllocator, address);
    }

    if (object != NULL && !Object_isMarked(&object->header)) {
        markObject(heap, stack, object);
    }
}

void checkForOverflow() {
    if (overflow) {
        // Set overflow address to the first word of the heap
        currentOverflowAddress = heap->heapStart;
        overflow = false;
        Stack_doubleSize(stack);

#ifdef PRINT_STACK_OVERFLOW
        printf("Stack grew to %zu bytes\n",
               stack->nb_words * sizeof(Stack_Type));
        fflush(stdout);
#endif

        word_t *largeHeapEnd = heap->largeHeapEnd;
        // Continue while we don' hit the end of the large heap.
        while (currentOverflowAddress != largeHeapEnd) {

            // If the current overflow address is in the small heap, scan the
            // small heap.
            if (heap_isWordInSmallHeap(heap, currentOverflowAddress)) {
                // If no object was found in the small heap, move on to large
                // heap
                if (!smallHeapOverflowHeapScan(heap, stack)) {
                    currentOverflowAddress = heap->largeHeapStart;
                }
            } else {
                largeHeapOverflowHeapScan(heap, stack);
            }

            // At every iteration when a object is found, trace it
            mark(heap, stack);
        }
    }
}

void mark(Heap *heap, Stack *stack) {
    while (!Stack_isEmpty(stack)) {
        Object *object = Stack_pop(stack);

        if (object->rtti->rt.id == __object_array_id) {
            // remove header and rtti from size
            size_t size =
                Object_size(&object->header) - OBJECT_HEADER_SIZE - WORD_SIZE;
            size_t nbWords = size / WORD_SIZE;
            for (int i = 0; i < nbWords; i++) {

                word_t *field = object->fields[i];
                Object *fieldObject = Object_fromMutatorAddress(field);
                if (heap_isObjectInHeap(heap, fieldObject) &&
                    !Object_isMarked(&fieldObject->header)) {
                    markObject(heap, stack, fieldObject);
                }
            }
        } else {
            int64_t *ptr_map = object->rtti->refMapStruct;
            int i = 0;
            while (ptr_map[i] != -1) {
                word_t *field = object->fields[ptr_map[i]];
                Object *fieldObject = Object_fromMutatorAddress(field);
                if (heap_isObjectInHeap(heap, fieldObject) &&
                    !Object_isMarked(&fieldObject->header)) {
                    markObject(heap, stack, fieldObject);
                }
                ++i;
            }
        }
    }
    checkForOverflow();
}

void markProgramStack(Heap *heap, Stack *stack) {
    unw_cursor_t cursor;
    unw_context_t context;
    unw_getcontext(&context);
    unw_init_local(&cursor, &context);
    unw_word_t top = LONG_MAX, bottom = 0;
    unw_word_t rsp;

    while (unw_step(&cursor) > 0) {
        unw_get_reg(&cursor, UNW_X86_64_RSP, &rsp);

        if (rsp < top) {
            top = rsp;
        }
        if (rsp > bottom) {
            bottom = rsp;
        }
    }
    unw_word_t p = top;

    while (p < bottom) {

        word_t *pp = (*(word_t **)p) - WORDS_IN_OBJECT_HEADER;
        if (heap_isWordInHeap(heap, pp)) {
            markConservative(heap, stack, pp);
        }
        p += 8;
    }
}

void markModules(Heap *heap, Stack *stack) {
    word_t **modules = &__modules;
    int nb_modules = __modules_size;

    for (int i = 0; i < nb_modules; i++) {
        Object *object = Object_fromMutatorAddress(modules[i]);
        if (heap_isObjectInHeap(heap, object) &&
            !Object_isMarked(&object->header)) {
            markObject(heap, stack, object);
        }
    }
}

void Mark_markRoots(Heap *heap, Stack *stack) {

    // Dumps registers into 'regs' which is on stack
    jmp_buf regs;
    setjmp(regs);

    markProgramStack(heap, stack);

    markModules(heap, stack);

    mark(heap, stack);
}

bool smallHeapOverflowHeapScan(Heap *heap, Stack *stack) {
    assert(heap_isWordInSmallHeap(heap, currentOverflowAddress));
    BlockHeader *currentBlock = Block_getBlockHeader(currentOverflowAddress);
    word_t *heapEnd = heap->heapEnd;

    while ((word_t *)currentBlock != heapEnd) {
        if (block_overflowHeapScan(currentBlock, heap, stack,
                                   &currentOverflowAddress)) {
            return true;
        }
        currentBlock = (BlockHeader *)((word_t *)currentBlock + WORDS_IN_BLOCK);
        currentOverflowAddress = (word_t *)currentBlock;
    }
    return false;
}

bool Marker_overflowMark(Heap* heap, Stack* stack, Object* object) {
    ObjectHeader *objectHeader = &object->header;
    if (Object_isMarked(objectHeader)) {
        if (object->rtti->rt.id == __object_array_id) {
            size_t size = Object_size(&object->header) -
                          OBJECT_HEADER_SIZE - WORD_SIZE;
            size_t nbWords = size / WORD_SIZE;
            for (int i = 0; i < nbWords; i++) {
                word_t *field = object->fields[i];
                Object *fieldObject = (Object *)(field - 1);
                if (heap_isObjectInHeap(heap, fieldObject) &&
                    !Object_isMarked(&fieldObject->header)) {
                    Stack_push(stack, object);
                    return true;
                }
            }
        } else {
            int64_t *ptr_map = object->rtti->refMapStruct;
            int i = 0;
            while (ptr_map[i] != -1) {
                word_t *field = object->fields[ptr_map[i]];
                Object *fieldObject = (Object *)(field - 1);
                if (heap_isObjectInHeap(heap, fieldObject) &&
                    !Object_isMarked(&fieldObject->header)) {
                    Stack_push(stack, object);
                    return true;
                }
                ++i;
            }
        }
    }
    return false;
}

// Scans through the large heap to find marked blocks with unmarked children.
// Updates `currentOverflowAddress` while doing so.
void largeHeapOverflowHeapScan(Heap *heap, Stack *stack) {
    assert(heap_isWordInLargeHeap(heap, currentOverflowAddress));
    void *heapEnd = heap->largeHeapEnd;

    while (currentOverflowAddress != heapEnd) {
        Object *object = (Object *)currentOverflowAddress;
        if(Marker_overflowMark(heap, stack, object)) {
            return;
        }
        currentOverflowAddress = (word_t *)Object_nextLargeObject(object);
    }
}
