#include <stdio.h>
#include "StackoverflowHandler.h"
#include "State.h"
#include "Block.h"
#include "Object.h"
#include "Marker.h"

extern int __object_array_id;

bool smallHeapOverflowHeapScan(Heap *heap, Stack *stack);
void largeHeapOverflowHeapScan(Heap *heap, Stack *stack);
bool overflowBlockScan(BlockHeader *block, Heap *heap, Stack *stack,
                       word_t **currentOverflowAddress);

void StackOverflowHandler_checkForOverflow() {
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
            Marker_mark(heap, stack);
        }
    }
}

bool smallHeapOverflowHeapScan(Heap *heap, Stack *stack) {
    assert(heap_isWordInSmallHeap(heap, currentOverflowAddress));
    BlockHeader *currentBlock = Block_getBlockHeader(currentOverflowAddress);
    word_t *heapEnd = heap->heapEnd;

    while ((word_t *)currentBlock != heapEnd) {
        if (overflowBlockScan(currentBlock, heap, stack,
                              &currentOverflowAddress)) {
            return true;
        }
        currentBlock = (BlockHeader *)((word_t *)currentBlock + WORDS_IN_BLOCK);
        currentOverflowAddress = (word_t *)currentBlock;
    }
    return false;
}

bool overflowMark(Heap *heap, Stack *stack, Object *object) {
    ObjectHeader *objectHeader = &object->header;
    if (Object_isMarked(objectHeader)) {
        if (object->rtti->rt.id == __object_array_id) {
            size_t size =
                Object_size(&object->header) - OBJECT_HEADER_SIZE - WORD_SIZE;
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

/**
 * Scans through the large heap to find marked blocks with unmarked children.
 * Updates `currentOverflowAddress` while doing so.
 */
void largeHeapOverflowHeapScan(Heap *heap, Stack *stack) {
    assert(heap_isWordInLargeHeap(heap, currentOverflowAddress));
    void *heapEnd = heap->largeHeapEnd;

    while (currentOverflowAddress != heapEnd) {
        Object *object = (Object *)currentOverflowAddress;
        if (overflowMark(heap, stack, object)) {
            return;
        }
        currentOverflowAddress = (word_t *)Object_nextLargeObject(object);
    }
}

bool overflowScanLine(Heap *heap, Stack *stack, BlockHeader *block,
                      int lineIndex) {
    LineHeader *lineHeader = Block_getLineHeader(block, lineIndex);

    if (Line_isMarked(lineHeader) && Line_containsObject(lineHeader)) {
        Object *object = Line_getFirstObject(lineHeader);
        word_t *lineEnd =
            Block_getLineAddress(block, lineIndex) + WORDS_IN_LINE;
        while (object != NULL && (word_t *)object < lineEnd) {
            if (overflowMark(heap, stack, object)) {
                return true;
            }
            object = Object_nextObject(object);
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
bool overflowBlockScan(BlockHeader *block, Heap *heap, Stack *stack,
                       word_t **currentOverflowAddress) {
    word_t *blockEnd = Block_getBlockEnd(block);
    if (!Block_isMarked(block)) {
        *currentOverflowAddress = blockEnd;
        return false;
    }

    int lineIndex;

    if (*currentOverflowAddress == (word_t *)block) {
        lineIndex = 0;
    } else {
        lineIndex = Block_getLineIndexFromWord(block, *currentOverflowAddress);
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