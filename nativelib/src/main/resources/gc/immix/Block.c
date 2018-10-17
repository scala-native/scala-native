#include <stdio.h>
#include <memory.h>
#include "Block.h"
#include "Object.h"
#include "metadata/ObjectMeta.h"
#include "Log.h"
#include "Allocator.h"
#include "Marker.h"

#define NO_RECYCLABLE_LINE -1

INLINE void Block_recycleUnmarkedBlock(Allocator *allocator,
                                       BlockMeta *blockMeta,
                                       word_t *blockStart) {
    memset(blockMeta, 0, sizeof(BlockMeta));
    // does not unmark in LineMetas because those are ignored by the allocator
    BlockList_AddLast(&allocator->freeBlocks, blockMeta);
    BlockMeta_SetFlag(blockMeta, block_free);
    ObjectMeta_ClearBlockAt(Bytemap_Get(allocator->bytemap, blockStart));
}

/**
 * recycles a block and adds it to the allocator
 */
void Block_Recycle(Allocator *allocator, BlockMeta *blockMeta,
                   word_t *blockStart, LineMeta *lineMetas) {

    // If the block is not marked, it means that it's completely free
    if (!BlockMeta_IsMarked(blockMeta)) {
        Block_recycleUnmarkedBlock(allocator, blockMeta, blockStart);
        allocator->freeBlockCount++;
        allocator->freeMemoryAfterCollection += BLOCK_TOTAL_SIZE;
    } else {
        // If the block is marked, we need to recycle line by line
        assert(BlockMeta_IsMarked(blockMeta));
        BlockMeta_Unmark(blockMeta);
        Bytemap *bytemap = allocator->bytemap;

        // start at line zero, keep separate pointers into all affected data
        // structures
        int16_t lineIndex = 0;
        LineMeta *lineMeta = lineMetas;
        word_t *lineStart = blockStart;
        ObjectMeta *bytemapCursor = Bytemap_Get(bytemap, lineStart);

        int lastRecyclable = NO_RECYCLABLE_LINE;
        while (lineIndex < LINE_COUNT) {
            // If the line is marked, we need to unmark all objects in the line
            if (Line_IsMarked(lineMeta)) {
                // Unmark line
                Line_Unmark(lineMeta);
                ObjectMeta_SweepLineAt(bytemapCursor);

                // next line
                lineIndex++;
                lineMeta++;
                lineStart += WORDS_IN_LINE;
                bytemapCursor = Bytemap_NextLine(bytemapCursor);
            } else {
                // If the line is not marked, we need to merge all continuous
                // unmarked lines.

                // If it's the first free line, update the block header to point
                // to it.
                if (lastRecyclable == NO_RECYCLABLE_LINE) {
                    blockMeta->first = lineIndex;
                } else {
                    // Update the last recyclable line to point to the current
                    // one
                    Block_GetFreeLineMeta(blockStart, lastRecyclable)->next =
                        lineIndex;
                }
                ObjectMeta_ClearLineAt(bytemapCursor);
                lastRecyclable = lineIndex;

                // next line
                lineIndex++;
                lineMeta++;
                lineStart += WORDS_IN_LINE;
                bytemapCursor = Bytemap_NextLine(bytemapCursor);

                allocator->freeMemoryAfterCollection += LINE_SIZE;

                uint8_t size = 1;
                while (lineIndex < LINE_COUNT && !Line_IsMarked(lineMeta)) {
                    ObjectMeta_ClearLineAt(bytemapCursor);
                    size++;

                    // next line
                    lineIndex++;
                    lineMeta++;
                    lineStart += WORDS_IN_LINE;
                    bytemapCursor = Bytemap_NextLine(bytemapCursor);

                    allocator->freeMemoryAfterCollection += LINE_SIZE;
                }
                Block_GetFreeLineMeta(blockStart, lastRecyclable)->size = size;
            }
        }
        // If there is no recyclable line, the block is unavailable
        if (lastRecyclable == NO_RECYCLABLE_LINE) {
            BlockMeta_SetFlag(blockMeta, block_unavailable);
        } else {
            Block_GetFreeLineMeta(blockStart, lastRecyclable)->next = LAST_HOLE;
            BlockMeta_SetFlag(blockMeta, block_recyclable);
            BlockList_AddLast(&allocator->recycledBlocks, blockMeta);

            assert(blockMeta->first != NO_RECYCLABLE_LINE);
            allocator->recycledBlockCount++;
        }
    }
}