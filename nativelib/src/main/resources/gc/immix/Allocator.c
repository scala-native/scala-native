#include <stdlib.h>
#include "Allocator.h"
#include "Line.h"
#include "Block.h"
#include "stats/AllocatorStats.h"
#include <stdio.h>
#include <memory.h>

BlockHeader* getNextBlock(Allocator* allocator);
bool getNextLine(Allocator* allocator);


Allocator* Allocator_create(word_t *heapStart, int blockCount) {
    Allocator* allocator = malloc(sizeof(Allocator));
    allocator->heapStart = heapStart;

    BlockList_init(&allocator->recycledBlocks, heapStart);
    BlockList_init(&allocator->freeBlocks, heapStart);

    // Init the free block list
    allocator->freeBlocks.first = (BlockHeader*) heapStart;
    BlockHeader* lastBlockHeader = (BlockHeader*)(heapStart + ((blockCount - 1) * WORDS_IN_BLOCK));
    allocator->freeBlocks.last = lastBlockHeader;
    lastBlockHeader->header.nextBlock = LAST_BLOCK;

    //Block stats
#ifdef ALLOCATOR_STATS
    allocator->stats = AllocatorStats_create();
    allocator->stats->blockCount = (uint64_t)blockCount;
#endif

    Allocator_initCursors(allocator);

    return allocator;
}

bool Allocator_initCursors(Allocator *allocator) {

    // Init cursor
    allocator->block = NULL;
    allocator->cursor = NULL;
    allocator->limit = NULL;

    getNextLine(allocator);

    // Init large cursor
    if(BlockList_isEmpty(&allocator->freeBlocks)) {
        return false;
    }
    BlockHeader* largeHeader = BlockList_removeFirstBlock(&allocator->freeBlocks);
    allocator->largeBlock = largeHeader;
    allocator->largeCursor = Block_getFirstWord(largeHeader);
    allocator->largeLimit = Block_getBlockEnd(largeHeader);

    return true;
}

/*
 * Overflow allocation uses only free blocks, it is used when the bump limit of the fast allocator is too small to fit
 * the block to alloc.
 */
word_t* overflowAllocation(Allocator *allocator, size_t size) {
    word_t* start = allocator->largeCursor;
    word_t* end = (word_t*)((uint8_t*)start + size);

    if(end > allocator->largeLimit) {
        // DIFFERENT FROM IMMIX
        if(BlockList_isEmpty(&allocator->freeBlocks)) {
            return NULL;
        }
        BlockHeader* block = BlockList_removeFirstBlock(&allocator->freeBlocks);
        allocator->largeBlock = block;
        allocator->largeCursor = Block_getFirstWord(block);
        allocator->largeLimit = Block_getBlockEnd(block);
        return overflowAllocation(allocator, size);
    }

    if(end == allocator->largeLimit) {
        memset(start, 0, size);
    } else {
        memset(start, 0, size + WORD_SIZE);
    }

    allocator->largeCursor = end;

    Line_update(allocator->largeBlock, start);

    return start;
}

INLINE word_t* Allocator_alloc(Allocator *allocator, size_t size) {
    word_t* start = allocator->cursor;
    word_t* end = (word_t*)((uint8_t*)start + size);

    if(end > allocator->limit) {
        if(size > LINE_SIZE) {
            return overflowAllocation(allocator, size);
        } else {
            if(getNextLine(allocator)) {
                return Allocator_alloc(allocator, size);
            }

            return NULL;
        }
    }

    if(end == allocator->limit) {
        memset(start, 0, size);
    } else {
        memset(start, 0, size + WORD_SIZE);
    }

    allocator->cursor = end;

    Line_update(allocator->block, start);

    return start;
}

bool getNextLine(Allocator* allocator) {
    // If cursor is null or the block was free, we need a new block
    // Can point on first word of next block, thus `- WORD_SIZE`
    if(allocator->cursor == NULL || Block_isFree(Block_getBlockHeader(allocator->cursor - WORD_SIZE))) {
        // request the new block.
        BlockHeader* block = getNextBlock(allocator);
        // return false if there is no block left.
        if(block == NULL) {
            return false;
        }

        allocator->block = block;

        // The block can be free or recycled.
        if(Block_isFree(block)) {
            allocator->cursor = Block_getFirstWord(block);
            allocator->limit = Block_getBlockEnd(block);
        } else {
            assert(Block_isRecyclable(block));
            int16_t lineIndex = block->header.first;
            assert(lineIndex < LINE_COUNT);
            word_t* line = Block_getLineAddress(block, lineIndex);

            allocator->cursor = line;
            FreeLineHeader* lineHeader = (FreeLineHeader*)line;
            block->header.first = lineHeader->next;
            uint16_t size = lineHeader->size;
            assert(size > 0);
            allocator->limit = line + (size * WORDS_IN_LINE);
        }

        return true;
    } else {
        // If we have a recycled block
        BlockHeader* block = Block_getBlockHeader(allocator->cursor - WORD_SIZE);
        assert(Block_isRecyclable(block));

        int16_t lineIndex = block->header.first;
        if(lineIndex == LAST_HOLE) {
            allocator->cursor = NULL;
            return getNextLine(allocator);
        }

        word_t* line = Block_getLineAddress(block, lineIndex);

        allocator->cursor = line;
        FreeLineHeader* lineHeader = (FreeLineHeader*)line;
        block->header.first = lineHeader->next;
        uint16_t size = lineHeader->size;
        allocator->limit = line + (size * WORDS_IN_LINE);
        return true;
    }

}

// Returns a block, first from recycled if available, otherwise from chunk_allocator
BlockHeader* getNextBlock(Allocator* allocator) {
    BlockHeader* block = NULL;
    if(!BlockList_isEmpty(&allocator->recycledBlocks)) {
        block = BlockList_removeFirstBlock(&allocator->recycledBlocks);
    } else if (!BlockList_isEmpty(&allocator->freeBlocks)){
        block = BlockList_removeFirstBlock(&allocator->freeBlocks);
    }
    return block;
}
