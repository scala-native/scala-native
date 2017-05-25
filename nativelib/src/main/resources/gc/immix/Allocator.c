#include <stdlib.h>
#include "Allocator.h"
#include "Line.h"
#include "Block.h"
#include <stdio.h>
#include <memory.h>

BlockHeader *getNextBlock(Allocator *allocator);
bool getNextLine(Allocator *allocator);

/**
 *
 * Allocates the Allocator and initialises it's fields
 *
 * @param heapStart
 * @param blockCount Initial number of blocks in the heap
 * @return
 */
Allocator *Allocator_create(word_t *heapStart, int blockCount) {
    Allocator *allocator = malloc(sizeof(Allocator));
    allocator->heapStart = heapStart;

    BlockList_init(&allocator->recycledBlocks, heapStart);
    BlockList_init(&allocator->freeBlocks, heapStart);

    // Init the free block list
    allocator->freeBlocks.first = (BlockHeader *)heapStart;
    BlockHeader *lastBlockHeader =
        (BlockHeader *)(heapStart + ((blockCount - 1) * WORDS_IN_BLOCK));
    allocator->freeBlocks.last = lastBlockHeader;
    lastBlockHeader->header.nextBlock = LAST_BLOCK;

    // Block stats
    allocator->blockCount = (uint64_t)blockCount;
    allocator->freeBlockCount = (uint64_t)blockCount;
    allocator->recycledBlockCount = 0;

    Allocator_initCursors(allocator);

    return allocator;
}

/**
 * The Allocator needs one free block for overflow allocation and a free or
 * recyclable block for normal allocation.
 *
 * @param allocator
 * @return `true` if there are enough block to initialise the cursors, `false`
 * otherwise.
 */
bool Allocator_canInitCursors(Allocator *allocator) {
    return allocator->freeBlockCount >= 2 ||
           (allocator->freeBlockCount == 1 &&
            allocator->recycledBlockCount > 0);
}

/**
 *
 * Used to initialise the cursors of the Allocator after collection
 *
 * @param allocator
 */
void Allocator_initCursors(Allocator *allocator) {

    // Init cursor
    allocator->block = NULL;
    allocator->cursor = NULL;
    allocator->limit = NULL;

    getNextLine(allocator);

    // Init large cursor
    assert(!BlockList_isEmpty(&allocator->freeBlocks));

    BlockHeader *largeHeader =
        BlockList_removeFirstBlock(&allocator->freeBlocks);
    allocator->largeBlock = largeHeader;
    allocator->largeCursor = Block_getFirstWord(largeHeader);
    allocator->largeLimit = Block_getBlockEnd(largeHeader);
}

/**
 * Heuristic that tells if the heap should be grown or not.
 */
bool Allocator_shouldGrow(Allocator *allocator) {
    uint64_t unavailableBlockCount =
        allocator->blockCount -
        (allocator->freeBlockCount + allocator->recycledBlockCount);

#ifdef DEBUG_PRINT
    printf("\n\nBlock count: %llu\n", allocator->blockCount);
    printf("Unavailable: %llu\n", unavailableBlockCount);
    printf("Free: %llu\n", allocator->freeBlockCount);
    printf("Recycled: %llu\n", allocator->recycledBlockCount);
    fflush(stdout);
#endif

    return allocator->freeBlockCount < allocator->blockCount / 3 ||
           4 * unavailableBlockCount > allocator->blockCount ||
           allocator->freeMemoryAfterCollection * 2 <
               allocator->blockCount * BLOCK_TOTAL_SIZE;
}

/**
 * Overflow allocation uses only free blocks, it is used when the bump limit of
 * the fast allocator is too small to fit
 * the block to alloc.
 */
word_t *overflowAllocation(Allocator *allocator, size_t size) {
    word_t *start = allocator->largeCursor;
    word_t *end = (word_t *)((uint8_t *)start + size);

    if (end > allocator->largeLimit) {
        if (BlockList_isEmpty(&allocator->freeBlocks)) {
            return NULL;
        }
        BlockHeader *block = BlockList_removeFirstBlock(&allocator->freeBlocks);
        allocator->largeBlock = block;
        allocator->largeCursor = Block_getFirstWord(block);
        allocator->largeLimit = Block_getBlockEnd(block);
        return overflowAllocation(allocator, size);
    }

    if (end == allocator->largeLimit) {
        memset(start, 0, size);
    } else {
        memset(start, 0, size + WORD_SIZE);
    }

    allocator->largeCursor = end;

    Line_update(allocator->largeBlock, start);

    return start;
}

/**
 * Allocation fast path, uses the cursor and limit.
 */
INLINE word_t *Allocator_alloc(Allocator *allocator, size_t size) {
    word_t *start = allocator->cursor;
    word_t *end = (word_t *)((uint8_t *)start + size);

    // Checks if the end of the block overlaps with the limit
    if (end > allocator->limit) {
        // If it overlaps but the block to allocate is a `medium` sized block,
        // use overflow allocation
        if (size > LINE_SIZE) {
            return overflowAllocation(allocator, size);
        } else {
            // Otherwise try to get a new line.
            if (getNextLine(allocator)) {
                return Allocator_alloc(allocator, size);
            }

            return NULL;
        }
    }

    if (end == allocator->limit) {
        memset(start, 0, size);
    } else {
        memset(start, 0, size + WORD_SIZE);
    }

    allocator->cursor = end;

    Line_update(allocator->block, start);

    return start;
}

/**
 * Updates the cursor and the limit of the Allocator to point the next line of
 * the recycled block
 */
bool nextLineRecycled(Allocator *allocator) {
    // The cursor can point on first word of next block, thus `- WORD_SIZE`
    BlockHeader *block = Block_getBlockHeader(allocator->cursor - WORD_SIZE);
    assert(Block_isRecyclable(block));

    int16_t lineIndex = block->header.first;
    if (lineIndex == LAST_HOLE) {
        allocator->cursor = NULL;
        return getNextLine(allocator);
    }

    word_t *line = Block_getLineAddress(block, lineIndex);

    allocator->cursor = line;
    FreeLineHeader *lineHeader = (FreeLineHeader *)line;
    block->header.first = lineHeader->next;
    uint16_t size = lineHeader->size;
    allocator->limit = line + (size * WORDS_IN_LINE);

    return true;
}

/**
 * Updates the the cursor and the limit of the Allocator to point to the first
 * free line of the new block.
 */
void firstLineNewBlock(Allocator *allocator, BlockHeader *block) {
    allocator->block = block;

    // The block can be free or recycled.
    if (Block_isFree(block)) {
        allocator->cursor = Block_getFirstWord(block);
        allocator->limit = Block_getBlockEnd(block);
    } else {
        assert(Block_isRecyclable(block));
        int16_t lineIndex = block->header.first;
        assert(lineIndex < LINE_COUNT);
        word_t *line = Block_getLineAddress(block, lineIndex);

        allocator->cursor = line;
        FreeLineHeader *lineHeader = (FreeLineHeader *)line;
        block->header.first = lineHeader->next;
        uint16_t size = lineHeader->size;
        assert(size > 0);
        allocator->limit = line + (size * WORDS_IN_LINE);
    }
}

bool getNextLine(Allocator *allocator) {
    // If cursor is null or the block was free, we need a new block
    if (allocator->cursor == NULL ||
        // The cursor can point on first word of next block, thus `- WORD_SIZE`
        Block_isFree(Block_getBlockHeader(allocator->cursor - WORD_SIZE))) {
        // request the new block.
        BlockHeader *block = getNextBlock(allocator);
        // return false if there is no block left.
        if (block == NULL) {
            return false;
        }

        firstLineNewBlock(allocator, block);

        return true;

    } else {
        // If we have a recycled block
        return nextLineRecycled(allocator);
    }
}

/**
 * Returns a block, first from recycled if available, otherwise from
 * chunk_allocator
 */
BlockHeader *getNextBlock(Allocator *allocator) {
    BlockHeader *block = NULL;
    if (!BlockList_isEmpty(&allocator->recycledBlocks)) {
        block = BlockList_removeFirstBlock(&allocator->recycledBlocks);
    } else if (!BlockList_isEmpty(&allocator->freeBlocks)) {
        block = BlockList_removeFirstBlock(&allocator->freeBlocks);
    }
    return block;
}
