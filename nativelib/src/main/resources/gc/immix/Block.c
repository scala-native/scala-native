#include <stdio.h>
#include <memory.h>
#include "Block.h"
#include "Object.h"
#include "Log.h"
#include "Allocator.h"
#include "Marker.h"

extern int __object_array_id;

INLINE void recycleUnmarkedBlock(Allocator *allocator,
                                 BlockHeader *blockHeader) {
    memset(blockHeader, 0, LINE_SIZE);
    BlockList_addLast(&allocator->freeBlocks, blockHeader);
    Block_setFlag(blockHeader, block_free);
}

INLINE void recycleMarkedLine(BlockHeader *blockHeader, LineHeader *lineHeader,
                              int lineIndex) {
    Line_unmark(lineHeader);
    // If the line contains an object
    if (Line_containsObject(lineHeader)) {
        // Unmark all objects in line
        Object *object = Line_getFirstObject(lineHeader);
        word_t *lineEnd =
            Block_getLineAddress(blockHeader, lineIndex) + WORDS_IN_LINE;
        while (object != NULL && (word_t *)object < lineEnd) {
            ObjectHeader *objectHeader = &object->header;
            if (Object_isMarked(objectHeader)) {
                Object_setAllocated(objectHeader);
            } else {
                Object_setFree(objectHeader);
            }
            object = Object_nextObject(object);
        }
    }
}

/**
 * recycles a block and adds it to the allocator
 */
void Block_recycle(Allocator *allocator, BlockHeader *blockHeader) {

    // If the block is not marked, it means that it's completely free
    if (!Block_isMarked(blockHeader)) {
        recycleUnmarkedBlock(allocator, blockHeader);
        allocator->freeBlockCount++;
        allocator->freeMemoryAfterCollection += BLOCK_TOTAL_SIZE;
    } else {
        // If the block is marked, we need to recycle line by line
        assert(Block_isMarked(blockHeader));
        Block_unmark(blockHeader);
        int16_t lineIndex = 0;
        int lastRecyclable = -1;
        while (lineIndex < LINE_COUNT) {
            LineHeader *lineHeader =
                Block_getLineHeader(blockHeader, lineIndex);
            // If the line is marked, we need to unmark all objects in the line
            if (Line_isMarked(lineHeader)) {
                // Unmark line
                recycleMarkedLine(blockHeader, lineHeader, lineIndex);
                lineIndex++;
            } else {
                // If the line is not marked, we need to merge all continuous
                // unmarked lines.

                // If it's the first free line, update the block header to point
                // to it.
                if (lastRecyclable == -1) {
                    blockHeader->header.first = lineIndex;
                } else {
                    // Update the last recyclable line to point to the current
                    // one
                    Block_getFreeLineHeader(blockHeader, lastRecyclable)->next =
                        lineIndex;
                }
                lastRecyclable = lineIndex;
                lineIndex++;
                Line_setEmpty(lineHeader);
                allocator->freeMemoryAfterCollection += LINE_SIZE;
                uint8_t size = 1;
                while (lineIndex < LINE_COUNT &&
                       !Line_isMarked(lineHeader = Block_getLineHeader(
                                          blockHeader, lineIndex))) {
                    size++;
                    lineIndex++;
                    Line_setEmpty(lineHeader);
                    allocator->freeMemoryAfterCollection += LINE_SIZE;
                }
                Block_getFreeLineHeader(blockHeader, lastRecyclable)->size =
                    size;
            }
        }
        // If there is no recyclable line, the block is unavailable
        if (lastRecyclable == -1) {
            Block_setFlag(blockHeader, block_unavailable);
        } else {
            Block_getFreeLineHeader(blockHeader, lastRecyclable)->next =
                LAST_HOLE;
            Block_setFlag(blockHeader, block_recyclable);
            BlockList_addLast(&allocator->recycledBlocks, blockHeader);

            assert(blockHeader->header.first != -1);
            allocator->recycledBlockCount++;
        }
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
            if (Marker_overflowMark(heap, stack, object)) {
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
bool block_overflowHeapScan(BlockHeader *block, Heap *heap, Stack *stack,
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

void Block_print(BlockHeader *block) {
    printf("%p ", block);
    if (Block_isFree(block)) {
        printf("FREE\n");
    } else if (Block_isUnavailable(block)) {
        printf("UNAVAILABLE\n");
    } else {
        int lineIndex = block->header.first;
        while (lineIndex != LAST_HOLE) {
            FreeLineHeader *freeLineHeader =
                Block_getFreeLineHeader(block, lineIndex);
            printf("[index: %d, size: %d] -> ", lineIndex,
                   freeLineHeader->size);
            lineIndex = freeLineHeader->next;
        }
        printf("\n");
    }
    fflush(stdout);
}