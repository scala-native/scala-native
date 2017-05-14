#include <stdlib.h>
#include "Allocator.h"
#include "Line.h"
#include "Block.h"
#include "stats/AllocatorStats.h"
#include <stdio.h>
#include <memory.h>

word_t* _allocate_slow(Allocator* allocator, size_t size);
BlockHeader* _get_next_block(Allocator* allocator);
bool _get_next_line(Allocator* allocator);


Allocator* allocator_create(word_t* heapStart, int blockCount) {
    Allocator* allocator = malloc(sizeof(Allocator));
    allocator->heapStart = heapStart;

    blockList_init(&allocator->recycledBlocks, heapStart);
    blockList_init(&allocator->freeBlocks, heapStart);

    // Init the free block list
    allocator->freeBlocks.first = (BlockHeader*) heapStart;
    BlockHeader* lastBlockHeader = (BlockHeader*)(heapStart + ((blockCount - 1) * WORDS_IN_BLOCK));
    allocator->freeBlocks.last = lastBlockHeader;
    lastBlockHeader->header.nextBlock = LAST_BLOCK;

    //Block stats
#ifdef ALLOCATOR_STATS
    allocator->stats = allocatorStats_create();
    allocator->stats->blockCount = (uint64_t)blockCount;
#endif

    allocator_initCursors(allocator);

    return allocator;
}

bool allocator_initCursors(Allocator* allocator) {

    // Init cursor
    allocator->block = NULL;
    allocator->cursor = NULL;
    allocator->limit = NULL;

    _get_next_line(allocator);

    // Init large cursor
    if(blockList_isEmpty(&allocator->freeBlocks)) {
        return false;
    }
    BlockHeader* largeHeader = blockList_removeFirstBlock(&allocator->freeBlocks);
    allocator->largeBlock = largeHeader;
    allocator->largeCursor = block_getFirstWord(largeHeader);
    allocator->largeLimit = block_getBlockEnd(largeHeader);

    return true;
}

word_t* _overflow_allocation(Allocator* allocator, size_t size) {
    word_t* start = allocator->largeCursor;
    word_t* end = (word_t*)((uint8_t*)start + size);

    if(end > allocator->largeLimit) {
        // DIFFERENT FROM IMMIX
        if(blockList_isEmpty(&allocator->freeBlocks)) {
            return NULL;
        }
        BlockHeader* block = blockList_removeFirstBlock(&allocator->freeBlocks);
        allocator->largeBlock = block;
        allocator->largeCursor = block_getFirstWord(block);
        allocator->largeLimit = block_getBlockEnd(block);
        return _overflow_allocation(allocator, size);
    }

    if(end == allocator->largeLimit) {
        memset(start, 0, size);
    } else {
        memset(start, 0, size + WORD_SIZE);
    }

    allocator->largeCursor = end;

    line_header_update(allocator->largeBlock, start);

    return start;
}

INLINE word_t* allocator_alloc(Allocator* allocator, size_t size) {
    word_t* start = allocator->cursor;
    word_t* end = (word_t*)((uint8_t*)start + size);

    if(end > allocator->limit) {
        if(size > LINE_SIZE) {
            return _overflow_allocation(allocator, size);
        } else {
            return _allocate_slow(allocator, size);
        }
    }

    if(end == allocator->limit) {
        memset(start, 0, size);
    } else {
        memset(start, 0, size + WORD_SIZE);
    }

    allocator->cursor = end;

    line_header_update(allocator->block, start);

    return start;
}

word_t* _allocate_slow(Allocator* allocator, size_t size) {
    if(_get_next_line(allocator)) {
        return allocator_alloc(allocator, size);
    }

    return NULL;
}


bool _get_next_line(Allocator* allocator) {
    // If cursor is null or the block was free, we need a new block
    // Can point on first word of next block, thus `- WORD_SIZE`
    if(allocator->cursor == NULL || block_isFree(block_getBlockHeader(allocator->cursor - WORD_SIZE))) {
        // request the new block.
        BlockHeader* block = _get_next_block(allocator);
        // return false if there is no block left.
        if(block == NULL) {
            return false;
        }

        allocator->block = block;

        // The block can be free or recycled.
        if(block_isFree(block)) {
            allocator->cursor = block_getFirstWord(block);
            allocator->limit = block_getBlockEnd(block);
        } else {
            assert(block_isRecyclable(block));
            int16_t lineIndex = block->header.first;
            assert(lineIndex < LINE_COUNT);
            word_t* line = block_getLineAddress(block, lineIndex);

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
        BlockHeader* block = block_getBlockHeader(allocator->cursor - WORD_SIZE);
        assert(block_isRecyclable(block));

        int16_t lineIndex = block->header.first;
        if(lineIndex == LAST_HOLE) {
            allocator->cursor = NULL;
            return _get_next_line(allocator);
        }

        word_t* line = block_getLineAddress(block, lineIndex);

        allocator->cursor = line;
        FreeLineHeader* lineHeader = (FreeLineHeader*)line;
        block->header.first = lineHeader->next;
        uint16_t size = lineHeader->size;
        allocator->limit = line + (size * WORDS_IN_LINE);
        return true;
    }

}

// Returns a block, first from recycled if available, otherwise from chunk_allocator
BlockHeader* _get_next_block(Allocator* allocator) {
    //blockList_print(&allocator->recycledBlocks);
    BlockHeader* block = NULL;
    if(!blockList_isEmpty(&allocator->recycledBlocks)) {
        block = blockList_removeFirstBlock(&allocator->recycledBlocks);
    } else if (!blockList_isEmpty(&allocator->freeBlocks)){
        block = blockList_removeFirstBlock(&allocator->freeBlocks);
    }
    return block;
}
