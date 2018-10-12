#include <stdio.h>
#include <memory.h>
#include "Block.h"
#include "Object.h"
#include "Log.h"
#include "Allocator.h"
#include "Marker.h"

#define NO_RECYCLABLE_LINE -1

INLINE void Block_recycleUnmarkedBlock(Allocator *allocator,
                                       BlockHeader *blockHeader, word_t* blockStart) {
    memset(blockHeader, 0, sizeof(BlockHeader));
    // does not unmark in LineHeaders because those are ignored by the allocator
    BlockList_AddLast(&allocator->freeBlocks, blockHeader);
    BlockHeader_SetFlag(blockHeader, block_free);
    Bytemap_SetAreaFree(allocator->bytemap, blockStart, WORDS_IN_BLOCK);
}

/**
 * recycles a block and adds it to the allocator
 */
void Block_Recycle(Allocator *allocator, BlockHeader *blockHeader, word_t* blockStart, LineHeader* lineHeaders) {

    // If the block is not marked, it means that it's completely free
    if (!BlockHeader_IsMarked(blockHeader)) {
        Block_recycleUnmarkedBlock(allocator, blockHeader, blockStart);
        allocator->freeBlockCount++;
        allocator->freeMemoryAfterCollection += BLOCK_TOTAL_SIZE;
    } else {
        // If the block is marked, we need to recycle line by line
        assert(BlockHeader_IsMarked(blockHeader));
        BlockHeader_Unmark(blockHeader);
        int16_t lineIndex = 0;
        Bytemap *bytemap = allocator->bytemap;
        int lastRecyclable = NO_RECYCLABLE_LINE;
        while (lineIndex < LINE_COUNT) {
            LineHeader *lineHeader = &lineHeaders[lineIndex];
            // If the line is marked, we need to unmark all objects in the line
            if (Line_IsMarked(lineHeader)) {
                // Unmark line
                Line_Unmark(lineHeader);
                word_t *lineStart = Block_GetLineAddress(blockStart, lineIndex);
                Bytemap_SweepLine(bytemap, lineStart);

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
                    Block_GetFreeLineHeader(blockStart, lastRecyclable)->next =
                        lineIndex;
                }
                lastRecyclable = lineIndex;
                lineIndex++;
                allocator->freeMemoryAfterCollection += LINE_SIZE;
                uint8_t size = 1;
                while (lineIndex < LINE_COUNT &&
                       !Line_IsMarked(lineHeader = &lineHeaders[lineIndex])) {
                    size++;
                    lineIndex++;
                    allocator->freeMemoryAfterCollection += LINE_SIZE;
                }
                Bytemap_SetAreaFree(allocator->bytemap, Block_GetLineAddress(blockStart, lastRecyclable), WORDS_IN_LINE * size);
                Block_GetFreeLineHeader(blockStart, lastRecyclable)->size = size;
            }
        }
        // If there is no recyclable line, the block is unavailable
        if (lastRecyclable == NO_RECYCLABLE_LINE) {
            BlockHeader_SetFlag(blockHeader, block_unavailable);
        } else {
            Block_GetFreeLineHeader(blockStart, lastRecyclable)->next =
                LAST_HOLE;
            BlockHeader_SetFlag(blockHeader, block_recyclable);
            BlockList_AddLast(&allocator->recycledBlocks, blockHeader);

            assert(blockHeader->header.first != NO_RECYCLABLE_LINE);
            allocator->recycledBlockCount++;
        }
    }
}

void Block_Print(BlockHeader *block) {
    printf("%p ", block);
    if (BlockHeader_IsFree(block)) {
        printf("FREE\n");
    } else if (BlockHeader_IsUnavailable(block)) {
        printf("UNAVAILABLE\n");
    } else {
        printf("RECYCLED\n");
    }
    printf("mark: %d, flags: %d, first: %d, nextBlock: %d \n",
           block->header.mark, block->header.flags, block->header.first,
           block->header.nextBlock);
    printf("\n");
    fflush(stdout);
}