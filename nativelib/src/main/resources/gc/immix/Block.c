#include <stdio.h>
#include <memory.h>
#include "Block.h"
#include "Object.h"
#include "Log.h"
#include "Allocator.h"
#include "stats/AllocatorStats.h"
#include "headers/BlockHeader.h"
#include "headers/LineHeader.h"


void block_recycle(Allocator* allocator, BlockHeader* blockHeader) {

    if(!block_isMarked(blockHeader)) {
        memset(blockHeader, 0, LINE_SIZE);
        blockList_addLast(&allocator->freeBlocks, blockHeader);
        block_setFlag(blockHeader, block_free);
#ifdef ALLOCATOR_STATS
        allocator->stats->availableBlockCount++;
#endif

    } else {
        assert(block_isMarked(blockHeader));
        block_unmark(blockHeader);
        int16_t lineIndex = 0;
        int lastRecyclable = -1;
        while(lineIndex < LINE_COUNT) {
            LineHeader* lineHeader = &blockHeader->lineHeaders[lineIndex];
            if(line_header_isMarked(lineHeader)) {
                // Unmark line
                line_header_unmark(lineHeader);
                // If the line contains an object
                if(line_header_containsObject(lineHeader)) {
                    //Unmark all objects in line
                    ObjectHeader *object = line_header_getFirstObject(lineHeader);
                    word_t *lineEnd = block_getLineAddress(blockHeader, lineIndex) + WORDS_IN_LINE;
                    while (object != NULL && (word_t *) object < lineEnd) {
                        if(object_isMarked(object)) {
                            object_unmarkObjectHeader(object);
                        } else {
                            object_setNotAllocated(object);
                        }
                        object = object_nextObject(object);
                    }
                }
                lineIndex++;
            } else {
                if(lastRecyclable == -1) {
                    blockHeader->header.first = lineIndex;
                } else {
                    block_getFreeLineHeader(blockHeader, lastRecyclable)->next = lineIndex;
                }
                lastRecyclable = lineIndex;
                line_header_setEmpty(lineHeader);
                lineIndex++;
                uint8_t size = 1;
                while(lineIndex < LINE_COUNT
                      && !line_header_isMarked(lineHeader = &blockHeader->lineHeaders[lineIndex])) {
                    size++;
                    lineIndex++;
                    line_header_setEmpty(lineHeader);
                }
                block_getFreeLineHeader(blockHeader, lastRecyclable)->size = size;
            }
        }
        if(lastRecyclable == -1) {
            block_setFlag(blockHeader, block_unavailable);

#ifdef ALLOCATOR_STATS
            allocator->stats->unavailableBlockCount++;
#endif

        } else {
            block_getFreeLineHeader(blockHeader, lastRecyclable)->next = LAST_HOLE;
            block_setFlag(blockHeader, block_recyclable);
            blockList_addLast(&allocator->recycledBlocks, blockHeader);

            assert(blockHeader->header.first != -1);

#ifdef ALLOCATOR_STATS
            allocator->stats->recyclableBlockCount++;
#endif

        }
    }
}

void block_print(BlockHeader* block) {
    printf("%p ", block);
    if(block_isFree(block)) {
        printf("FREE\n");
    } else if (block_isUnavailable(block)) {
        printf("UNAVAILABLE\n");
    } else {
        int lineIndex = block->header.first;
        while(lineIndex != LAST_HOLE) {
            FreeLineHeader* freeLineHeader = block_getFreeLineHeader(block, lineIndex);
            printf("[index: %d, size: %d] -> ", lineIndex, freeLineHeader->size);
            lineIndex = freeLineHeader->next;
        }
        printf("\n");
    }
    fflush(stdout);
}