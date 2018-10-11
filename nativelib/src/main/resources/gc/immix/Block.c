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
                                       BlockHeader *blockHeader, word_t* blockStart) {
    memset(blockHeader, 0, TOTAL_BLOCK_METADATA_SIZE);
    BlockList_AddLast(&allocator->freeBlocks, blockHeader);
    BlockHeader_SetFlag(blockHeader, block_free);
    Bytemap_SetAreaFree(allocator->bytemap, blockStart, WORDS_IN_BLOCK);
}

INLINE void Block_recycleMarkedLine(Bytemap *bytemap, word_t *blockStart,
                                    LineHeader *lineHeader, int lineIndex) {
    Line_Unmark(lineHeader);
    // If the line contains an object
    Object *object = Line_GetFirstObject(bytemap, blockStart, lineIndex);
    // Unmark all objects in line
    word_t *lineEnd =
        Block_GetLineAddress(blockStart, lineIndex) + WORDS_IN_LINE;
    while (object != NULL && (word_t *)object < lineEnd) {
        ObjectHeader *objectHeader = &object->header;
        if (Bytemap_IsMarked(bytemap, (word_t *) object)) {
            Bytemap_SetAllocated(bytemap, (word_t*) object);
        } else {
            Bytemap_SetPlaceholder(bytemap, (word_t*) object);
        }
        object = Object_NextObject(object);
    }
}

/**
 * recycles a block and adds it to the allocator
 */
void Block_Recycle(Allocator *allocator, BlockHeader *blockHeader, word_t* blockStart) {

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
            LineHeader *lineHeader =
                BlockHeader_GetLineHeader(blockHeader, lineIndex);
            // If the line is marked, we need to unmark all objects in the line
            if (Line_IsMarked(lineHeader)) {
                // Unmark line
                Block_recycleMarkedLine(bytemap, blockStart, lineHeader, lineIndex);
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
                       !Line_IsMarked(lineHeader = BlockHeader_GetLineHeader(
                                          blockHeader, lineIndex))) {
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

    for (int i = 0; i < LINE_COUNT; i++) {
        printf("%d ", block->lineHeaders[i]);
    }
    printf("\n");
    fflush(stdout);
}