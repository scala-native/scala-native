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
bool StackOverflowHandler_overflowBlockScan(BlockMeta *block, Heap *heap,
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
    BlockMeta *currentBlock = Block_GetBlockMeta(
        heap->blockMetaStart, heap->heapStart, currentOverflowAddress);
    word_t *blockMetaEnd = heap->blockMetaEnd;

    while ((word_t *)currentBlock < blockMetaEnd) {
        if (StackOverflowHandler_overflowBlockScan(currentBlock, heap, stack)) {
            return true;
        }
        currentBlock =
            (BlockMeta *)((word_t *)currentBlock + WORDS_IN_BLOCK_METADATA);
        currentOverflowAddress = BlockMeta_GetBlockStart(
            heap->blockMetaStart, heap->heapStart, currentBlock);
    }
    return false;
}

bool StackOverflowHandler_overflowMark(Heap *heap, Stack *stack, Object *object,
                                       ObjectMeta *objectMeta) {

    if (ObjectMeta_IsMarked(objectMeta)) {
        if (Object_IsArray(object)) {
            if (object->rtti->rt.id == __object_array_id) {
                ArrayHeader *arrayHeader = (ArrayHeader *)object;
                size_t length = arrayHeader->length;
                word_t **fields = (word_t **)(arrayHeader + 1);
                for (int i = 0; i < length; i++) {
                    word_t *field = fields[i];
                    Object *fieldObject = (Object *)field;
                    Bytemap *bytemapF =
                        Heap_BytemapForWord(heap, (word_t *)fieldObject);
                    if (bytemapF != NULL) {
                        // is within heap
                        ObjectMeta *metaF =
                            Bytemap_Get(bytemapF, (word_t *)fieldObject);
                        if (ObjectMeta_IsAllocated(metaF)) {
                            Stack_Push(stack, object);
                            return true;
                        }
                    }
                }
            }
            // non-object arrays do not contain pointers
        } else {
            int64_t *ptr_map = object->rtti->refMapStruct;
            int i = 0;
            while (ptr_map[i] != LAST_FIELD_OFFSET) {
                word_t *field = object->fields[ptr_map[i]];
                Object *fieldObject = (Object *)field;
                Bytemap *bytemapF =
                    Heap_BytemapForWord(heap, (word_t *)fieldObject);
                if (bytemapF != NULL) {
                    // is within heap
                    ObjectMeta *metaF =
                        Bytemap_Get(bytemapF, (word_t *)fieldObject);
                    if (ObjectMeta_IsAllocated(metaF)) {
                        Stack_Push(stack, object);
                        return true;
                    }
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
        ObjectMeta *cursorMeta =
            Bytemap_Get(heap->largeBytemap, currentOverflowAddress);
        if (StackOverflowHandler_overflowMark(heap, stack, object,
                                              cursorMeta)) {
            return;
        }
        currentOverflowAddress = (word_t *)Object_NextLargeObject(object);
    }
}

bool overflowScanLine(Heap *heap, Stack *stack, BlockMeta *block,
                      word_t *blockStart, int lineIndex) {
    Bytemap *bytemap = heap->smallBytemap;

    word_t *lineStart = Block_GetLineAddress(blockStart, lineIndex);
    if (Line_IsMarked(Heap_LineMetaForWord(heap, lineStart))) {
        word_t *lineEnd = lineStart + WORDS_IN_LINE;
        word_t *cursor = lineStart;
        ObjectMeta *cursorMeta = Bytemap_Get(bytemap, cursor);
        while (cursor < lineEnd) {
            Object *object = (Object *)cursor;
            if (ObjectMeta_IsMarked(cursorMeta) &&
                StackOverflowHandler_overflowMark(heap, stack, object,
                                                  cursorMeta)) {
                return true;
            }

            cursor += ALLOCATION_ALIGNMENT_WORDS;
            cursorMeta = Bytemap_NextWord(cursorMeta);
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
bool StackOverflowHandler_overflowBlockScan(BlockMeta *block, Heap *heap,
                                            Stack *stack) {
    if (!BlockMeta_IsMarked(block)) {
        return false;
    }

    word_t *blockStart =
        BlockMeta_GetBlockStart(heap->blockMetaStart, heap->heapStart, block);
    int lineIndex =
        Block_GetLineIndexFromWord(blockStart, currentOverflowAddress);
    while (lineIndex < LINE_COUNT) {
        if (overflowScanLine(heap, stack, block, blockStart, lineIndex)) {
            return true;
        }

        lineIndex++;
    }
    return false;
}
