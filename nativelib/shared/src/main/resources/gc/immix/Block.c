#include <stdio.h>
#include <memory.h>
#include "Block.h"
#include "Object.h"
#include "Log.h"
#include "Allocator.h"
#include "Marker.h"

extern int __object_array_id;

#define NO_RECYCLABLE_LINE -1

INLINE void Block_recycleUnmarkedBlock(Allocator *allocator,
                                       BlockHeader *blockHeader) {
    memset(blockHeader, 0, LINE_SIZE);
    BlockList_AddLast(&allocator->freeBlocks, blockHeader);
    Block_SetFlag(blockHeader, block_free);
}

INLINE void Block_recycleMarkedLine(BlockHeader *blockHeader,
                                    LineHeader *lineHeader, int lineIndex) {
    Line_Unmark(lineHeader);
    // If the line contains an object
    if (Line_ContainsObject(lineHeader)) {
        // Unmark all objects in line
        Object *object = Line_GetFirstObject(lineHeader);
        word_t *lineEnd =
            Block_GetLineAddress(blockHeader, lineIndex) + WORDS_IN_LINE;
        while (object != NULL && (word_t *)object < lineEnd) {
            ObjectHeader *objectHeader = &object->header;
            if (Object_IsMarked(objectHeader)) {
                Object_SetAllocated(objectHeader);
            } else {
                Object_SetFree(objectHeader);
            }
            object = Object_NextObject(object);
        }
    }
}

/**
 * recycles a block and adds it to the allocator
 */
void Block_Recycle(Allocator *allocator, BlockHeader *blockHeader) {

    // If the block is not marked, it means that it's completely free
    if (!Block_IsMarked(blockHeader)) {
        Block_recycleUnmarkedBlock(allocator, blockHeader);
        allocator->freeBlockCount++;
        allocator->freeMemoryAfterCollection += BLOCK_TOTAL_SIZE;
    } else {
        // If the block is marked, we need to recycle line by line
        assert(Block_IsMarked(blockHeader));
        Block_Unmark(blockHeader);
        int16_t lineIndex = 0;
        int lastRecyclable = NO_RECYCLABLE_LINE;
        while (lineIndex < LINE_COUNT) {
            LineHeader *lineHeader =
                Block_GetLineHeader(blockHeader, lineIndex);
            // If the line is marked, we need to unmark all objects in the line
            if (Line_IsMarked(lineHeader)) {
                // Unmark line
                Block_recycleMarkedLine(blockHeader, lineHeader, lineIndex);
                lineIndex++;
            } else {
                // If the line is not marked, we need to merge all continuous
                // unmarked lines.

                // If it's the first free line, update the block header to point
                // to it.
                if (lastRecyclable == NO_RECYCLABLE_LINE) {
                    blockHeader->header.first = lineIndex;
                } else {
                    // Update the last recyclable line to point to the current
                    // one
                    Block_GetFreeLineHeader(blockHeader, lastRecyclable)->next =
                        lineIndex;
                }
                lastRecyclable = lineIndex;
                lineIndex++;
                Line_SetEmpty(lineHeader);
                allocator->freeMemoryAfterCollection += LINE_SIZE;
                uint8_t size = 1;
                while (lineIndex < LINE_COUNT &&
                       !Line_IsMarked(lineHeader = Block_GetLineHeader(
                                          blockHeader, lineIndex))) {
                    size++;
                    lineIndex++;
                    Line_SetEmpty(lineHeader);
                    allocator->freeMemoryAfterCollection += LINE_SIZE;
                }
                Block_GetFreeLineHeader(blockHeader, lastRecyclable)->size =
                    size;
            }
        }
        // If there is no recyclable line, the block is unavailable
        if (lastRecyclable == NO_RECYCLABLE_LINE) {
            Block_SetFlag(blockHeader, block_unavailable);
        } else {
            Block_GetFreeLineHeader(blockHeader, lastRecyclable)->next =
                LAST_HOLE;
            Block_SetFlag(blockHeader, block_recyclable);
            BlockList_AddLast(&allocator->recycledBlocks, blockHeader);

            assert(blockHeader->header.first != NO_RECYCLABLE_LINE);
            allocator->recycledBlockCount++;
        }
    }
}

void Block_Print(BlockHeader *block) {
    printf("%p ", block);
    if (Block_IsFree(block)) {
        printf("FREE\n");
    } else if (Block_IsUnavailable(block)) {
        printf("UNAVAILABLE\n");
    } else {
        int lineIndex = block->header.first;
        while (lineIndex != LAST_HOLE) {
            FreeLineHeader *freeLineHeader =
                Block_GetFreeLineHeader(block, lineIndex);
            printf("[index: %d, size: %d] -> ", lineIndex,
                   freeLineHeader->size);
            lineIndex = freeLineHeader->next;
        }
        printf("\n");
    }
    fflush(stdout);
}