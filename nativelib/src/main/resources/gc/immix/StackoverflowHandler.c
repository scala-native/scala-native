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
                                            Stack *stack);

void StackOverflowHandler_CheckForOverflow() {
    if (overflow) {
        // Set overflow address to the first word of the heap
        currentOverflowAddress = heap.heapStart;
        overflow = false;
        Stack_DoubleSize(&stack);

#ifdef PRINT_STACK_OVERFLOW
        printf("Stack grew to %zu bytes\n",
               stack.nb_words * sizeof(Stack_Type));
        fflush(stdout);
#endif

        word_t *largeHeapEnd = heap.largeHeapEnd;
        // Continue while we don' hit the end of the large heap.
        while (currentOverflowAddress != largeHeapEnd) {

            // If the current overflow address is in the small heap, scan the
            // small heap.
            if (Heap_IsWordInSmallHeap(&heap, currentOverflowAddress)) {
                // If no object was found in the small heap, move on to large
                // heap
                if (!StackOverflowHandler_smallHeapOverflowHeapScan(&heap,
                                                                    &stack)) {
                    currentOverflowAddress = heap.largeHeapStart;
                }
            } else {
                StackOverflowHandler_largeHeapOverflowHeapScan(&heap, &stack);
            }

            // At every iteration when a object is found, trace it
            Marker_Mark(&heap, &stack);
        }
    }
}

bool StackOverflowHandler_smallHeapOverflowHeapScan(Heap *heap, Stack *stack) {
    assert(Heap_IsWordInSmallHeap(heap, currentOverflowAddress));
    BlockHeader *currentBlock = Block_GetBlockHeader(heap->blockHeaderStart, heap->heapStart, currentOverflowAddress);
    word_t *blockHeaderEnd = heap->blockHeaderEnd;

    while ((word_t *)currentBlock < blockHeaderEnd) {
        if (StackOverflowHandler_overflowBlockScan(currentBlock, heap, stack)) {
            return true;
        }
        currentBlock = (BlockHeader *)((word_t *)currentBlock + WORDS_IN_BLOCK_METADATA);
        currentOverflowAddress = BlockHeader_GetBlockStart(heap->blockHeaderStart, heap->heapStart, currentBlock);
    }
    return false;
}

bool StackOverflowHandler_overflowMark(Heap *heap, Stack *stack,
                                       Object *object) {
    ObjectHeader *objectHeader = &object->header;

    Bytemap *bytemap = Heap_BytemapForWord(heap, (word_t*) object);

    if (Bytemap_IsMarked(bytemap, (word_t*) object)) {
        if (object->rtti->rt.id == __object_array_id) {
            size_t size =
                Object_Size(&object->header) - OBJECT_HEADER_SIZE - WORD_SIZE;
            assert(Object_Size(&object->header) == OBJECT_HEADER_SIZE + Object_SizeInternal(object));
            size_t nbWords = size / WORD_SIZE;
            for (int i = 0; i < nbWords; i++) {
                word_t *field = object->fields[i];
                Object *fieldObject = Object_FromMutatorAddress(field);
                Bytemap *bytemapF = Heap_BytemapForWord(heap, (word_t*) fieldObject);
                if (heap_isObjectInHeap(heap, fieldObject) &&
                    !Bytemap_IsMarked(bytemapF, (word_t*) fieldObject)) {
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
                Bytemap *bytemapF = Heap_BytemapForWord(heap, (word_t*) fieldObject);
                if (heap_isObjectInHeap(heap, fieldObject) &&
                    !Bytemap_IsMarked(bytemapF, (word_t*) fieldObject)) {
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

bool overflowScanLine(Heap *heap, Stack *stack, BlockHeader *block, word_t* blockStart,
                      int lineIndex) {
    Bytemap *bytemap = heap->smallBytemap;

    Object *object = Line_GetFirstObject(bytemap, blockStart, lineIndex);
    if (object != NULL && Line_IsMarked(Heap_LineHeaderForWord(heap,(word_t *) object))) {
        word_t *lineEnd =
            Block_GetLineAddress(blockStart, lineIndex) + WORDS_IN_LINE;
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
                                            Stack *stack) {
    if (!BlockHeader_IsMarked(block)) {
        return false;
    }

    word_t *blockStart = BlockHeader_GetBlockStart(heap->blockHeaderStart, heap->heapStart, block);
    int lineIndex = Block_GetLineIndexFromWord(blockStart, currentOverflowAddress);
    while (lineIndex < LINE_COUNT) {
        if (overflowScanLine(heap, stack, block, blockStart, lineIndex)) {
            return true;
        }

        lineIndex++;
    }
    return false;
}
