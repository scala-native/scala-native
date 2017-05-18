#include <stdio.h>
#include <memory.h>
#include "Block.h"
#include "Object.h"
#include "Log.h"
#include "Allocator.h"
#include "stats/AllocatorStats.h"
#include "headers/BlockHeader.h"
#include "headers/LineHeader.h"


INLINE void recycleUnmarkedBlock(Allocator *allocator, BlockHeader* blockHeader) {
    memset(blockHeader, 0, LINE_SIZE);
    BlockList_addLast(&allocator->freeBlocks, blockHeader);
    Block_setFlag(blockHeader, block_free);
#ifdef ALLOCATOR_STATS
    allocator->stats->availableBlockCount++;
#endif

}

INLINE void recycleMarkedLine(BlockHeader* blockHeader, LineHeader* lineHeader, int lineIndex) {
    Line_unmark(lineHeader);
    // If the line contains an object
    if(Line_containsObject(lineHeader)) {
        //Unmark all objects in line
        ObjectHeader *object = Line_getFirstObject(lineHeader);
        word_t *lineEnd = Block_getLineAddress(blockHeader, lineIndex) + WORDS_IN_LINE;
        while (object != NULL && (word_t *) object < lineEnd) {
            if(Object_isMarked(object)) {
                Object_setAllocated(object);
            } else {
                Object_setFree(object);
            }
            object = Object_nextObject(object);
        }
    }
}

void Block_recycle(Allocator *allocator, BlockHeader *blockHeader) {

    if(!Block_isMarked(blockHeader)) {
        recycleUnmarkedBlock(allocator, blockHeader);

    } else {
        assert(Block_isMarked(blockHeader));
        Block_unmark(blockHeader);
        int16_t lineIndex = 0;
        int lastRecyclable = -1;
        while(lineIndex < LINE_COUNT) {
            LineHeader* lineHeader = Block_getLineHeader(blockHeader, lineIndex);
            if(Line_isMarked(lineHeader)) {
                // Unmark line
                recycleMarkedLine(blockHeader, lineHeader, lineIndex);
                lineIndex++;
            } else {
                if(lastRecyclable == -1) {
                    blockHeader->header.first = lineIndex;
                } else {
                    Block_getFreeLineHeader(blockHeader, lastRecyclable)->next = lineIndex;
                }
                lastRecyclable = lineIndex;
                Line_setEmpty(lineHeader);
                lineIndex++;
                uint8_t size = 1;
                while(lineIndex < LINE_COUNT
                      && !Line_isMarked(lineHeader = Block_getLineHeader(blockHeader, lineIndex))) {
                    size++;
                    lineIndex++;
                    Line_setEmpty(lineHeader);
                }
                Block_getFreeLineHeader(blockHeader, lastRecyclable)->size = size;
            }
        }
        if(lastRecyclable == -1) {
            Block_setFlag(blockHeader, block_unavailable);

#ifdef ALLOCATOR_STATS
            allocator->stats->unavailableBlockCount++;
#endif

        } else {
            Block_getFreeLineHeader(blockHeader, lastRecyclable)->next = LAST_HOLE;
            Block_setFlag(blockHeader, block_recyclable);
            BlockList_addLast(&allocator->recycledBlocks, blockHeader);

            assert(blockHeader->header.first != -1);

#ifdef ALLOCATOR_STATS
            allocator->stats->recyclableBlockCount++;
#endif

        }
    }
}

void Block_print(BlockHeader* block) {
    printf("%p ", block);
    if(Block_isFree(block)) {
        printf("FREE\n");
    } else if (Block_isUnavailable(block)) {
        printf("UNAVAILABLE\n");
    } else {
        int lineIndex = block->header.first;
        while(lineIndex != LAST_HOLE) {
            FreeLineHeader* freeLineHeader = Block_getFreeLineHeader(block, lineIndex);
            printf("[index: %d, size: %d] -> ", lineIndex, freeLineHeader->size);
            lineIndex = freeLineHeader->next;
        }
        printf("\n");
    }
    fflush(stdout);
}