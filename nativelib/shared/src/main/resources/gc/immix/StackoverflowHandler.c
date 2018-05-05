#include <stdio.h>
#include "StackoverflowHandler.h"
#include "State.h"
#include "Block.h"
#include "Object.h"
#include "Marker.h"

extern int __object_array_id;

#define LAST_FIELD_OFFSET -1

bool StackOverflowHandler_smallHeapOverflowHeapScan(Heap *heap, Stack *stack);
void StackOverflowHandler_largeHeapOverflowHeapScan(Heap *heap, Stack *stack);
bool StackOverflowHandler_overflowBlockScan(BlockHeader *block, Heap *heap,
                                            Stack *stack,
                                            word_t **currentOverflowAddress);

void StackOverflowHandler_CheckForOverflow() {
    if (overflow) {
        // Set overflow address to the first word of the heap
        currentOverflowAddress = heap->heapStart;
        overflow = false;
        Stack_DoubleSize(stack);

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
            if (Heap_IsWordInSmallHeap(heap, currentOverflowAddress)) {
                // If no object was found in the small heap, move on to large
                // heap
                if (!StackOverflowHandler_smallHeapOverflowHeapScan(heap,
                                                                    stack)) {
                    currentOverflowAddress = heap->largeHeapStart;
                }
            } else {
                StackOverflowHandler_largeHeapOverflowHeapScan(heap, stack);
            }

            // At every iteration when a object is found, trace it
            Marker_Mark(heap, stack);
        }
    }
}

bool StackOverflowHandler_smallHeapOverflowHeapScan(Heap *heap, Stack *stack) {
    assert(Heap_IsWordInSmallHeap(heap, currentOverflowAddress));
    BlockHeader *currentBlock = Block_GetBlockHeader(currentOverflowAddress);
    word_t *heapEnd = heap->heapEnd;

    while ((word_t *)currentBlock != heapEnd) {
        if (StackOverflowHandler_overflowBlockScan(currentBlock, heap, stack,
                                                   &currentOverflowAddress)) {
            return true;
        }
        currentBlock = (BlockHeader *)((word_t *)currentBlock + WORDS_IN_BLOCK);
        currentOverflowAddress = (word_t *)currentBlock;
    }
    return false;
}

bool StackOverflowHandler_overflowMark(Heap *heap, Stack *stack,
                                       Object *object) {
    ObjectHeader *objectHeader = &object->header;
    if (Object_IsMarked(objectHeader)) {
        if (object->rtti->rt.id == __object_array_id) {
            size_t size =
                Object_Size(&object->header) - OBJECT_HEADER_SIZE - WORD_SIZE;
            size_t nbWords = size / WORD_SIZE;
            for (int i = 0; i < nbWords; i++) {
                word_t *field = object->fields[i];
                Object *fieldObject = Object_FromMutatorAddress(field);
                if (heap_isObjectInHeap(heap, fieldObject) &&
                    !Object_IsMarked(&fieldObject->header)) {
                    Stack_Push(stack, object);
                    return true;
                }
            }
        } else {
            int64_t *ptr_map = object->rtti->refMapStruct;
            int i = 0;
            while (ptr_map[i] != LAST_FIELD_OFFSET) {
                word_t *field = object->fields[ptr_map[i]];
                Object *fieldObject = Object_FromMutatorAddress(field);
                if (heap_isObjectInHeap(heap, fieldObject) &&
                    !Object_IsMarked(&fieldObject->header)) {
                    Stack_Push(stack, object);
                    return true;
                }
                ++i;
            }
        }
    }
    return false;
}

/**
 * Scans through the large heap to find marked blocks with unmarked children.
 * Updates `currentOverflowAddress` while doing so.
 */
void StackOverflowHandler_largeHeapOverflowHeapScan(Heap *heap, Stack *stack) {
    assert(Heap_IsWordInLargeHeap(heap, currentOverflowAddress));
    void *heapEnd = heap->largeHeapEnd;

    while (currentOverflowAddress != heapEnd) {
        Object *object = (Object *)currentOverflowAddress;
        if (StackOverflowHandler_overflowMark(heap, stack, object)) {
            return;
        }
        currentOverflowAddress = (word_t *)Object_NextLargeObject(object);
    }
}

bool overflowScanLine(Heap *heap, Stack *stack, BlockHeader *block,
                      int lineIndex) {
    LineHeader *lineHeader = Block_GetLineHeader(block, lineIndex);

    if (Line_IsMarked(lineHeader) && Line_ContainsObject(lineHeader)) {
        Object *object = Line_GetFirstObject(lineHeader);
        word_t *lineEnd =
            Block_GetLineAddress(block, lineIndex) + WORDS_IN_LINE;
        while (object != NULL && (word_t *)object < lineEnd) {
            if (StackOverflowHandler_overflowMark(heap, stack, object)) {
                return true;
            }
            object = Object_NextObject(object);
        }
    }
    return false;
}

/**
 *
 * This method is used in case of overflow during the marking phase.
 * It sweeps through the block starting at `currentOverflowAddress` until it
 * finds a marked block with unmarked children.
 * It updates the value of `currentOverflowAddress` while sweeping through the
 * block
 * Once an object is found it adds it to the stack and returns `true`. If no
 * object is found it returns `false`.
 *
 */
bool StackOverflowHandler_overflowBlockScan(BlockHeader *block, Heap *heap,
                                            Stack *stack,
                                            word_t **currentOverflowAddress) {
    word_t *blockEnd = Block_GetBlockEnd(block);
    if (!Block_IsMarked(block)) {
        *currentOverflowAddress = blockEnd;
        return false;
    }

    int lineIndex;

    if (*currentOverflowAddress == (word_t *)block) {
        lineIndex = 0;
    } else {
        lineIndex = Block_GetLineIndexFromWord(block, *currentOverflowAddress);
    }
    while (lineIndex < LINE_COUNT) {
        if (overflowScanLine(heap, stack, block, lineIndex)) {
            return true;
        }

        lineIndex++;
    }
    *currentOverflowAddress = blockEnd;
    return false;
}