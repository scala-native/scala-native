#include <stdlib.h>
#include "Allocator.h"
#include "Line.h"
#include "Block.h"
#include <stdio.h>
#include <memory.h>

BlockHeader *Allocator_getNextBlock(Allocator *allocator);
bool Allocator_getNextLine(Allocator *allocator);
bool Allocator_newBlock(Allocator *allocator);

/**
 *
 * Allocates the Allocator and initialises it's fields
 *
 * @param blockHeaderStart
 * @param blockCount Initial number of blocks in the heap
 * @return
 */
void Allocator_Init(Allocator *allocator, Bytemap *bytemap, word_t *blockHeaderStart, word_t * heapStart, uint32_t blockCount) {
    allocator->blockHeaderStart = blockHeaderStart;
    allocator->bytemap = bytemap;
    allocator->heapStart = heapStart;

    BlockList_Init(&allocator->recycledBlocks, blockHeaderStart);
    BlockList_Init(&allocator->freeBlocks, blockHeaderStart);

    // Init the free block list
    allocator->freeBlocks.first = (BlockHeader *)blockHeaderStart;
    BlockHeader *lastBlockHeader =
        (BlockHeader *)(blockHeaderStart + ((blockCount - 1) * WORDS_IN_BLOCK_METADATA));
    allocator->freeBlocks.last = lastBlockHeader;
    lastBlockHeader->header.nextBlock = LAST_BLOCK;

    // Block stats
    allocator->blockCount = (uint64_t)blockCount;
    allocator->freeBlockCount = (uint64_t)blockCount;
    allocator->recycledBlockCount = 0;

    Allocator_InitCursors(allocator);
}

/**
 * The Allocator needs one free block for overflow allocation and a free or
 * recyclable block for normal allocation.
 *
 * @param allocator
 * @return `true` if there are enough block to initialise the cursors, `false`
 * otherwise.
 */
bool Allocator_CanInitCursors(Allocator *allocator) {
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
void Allocator_InitCursors(Allocator *allocator) {

    // Init cursor
    bool didInit = Allocator_newBlock(allocator);
    assert(didInit);

    // Init large cursor
    assert(!BlockList_IsEmpty(&allocator->freeBlocks));

    BlockHeader *largeHeader =
        BlockList_RemoveFirstBlock(&allocator->freeBlocks);
    allocator->largeBlock = largeHeader;
    word_t* largeBlockStart = BlockHeader_GetBlockStart(allocator->blockHeaderStart, allocator->heapStart, largeHeader);
    allocator->largeBlockStart = largeBlockStart;
    allocator->largeCursor = largeBlockStart;
    allocator->largeLimit = Block_GetBlockEnd(largeBlockStart);
}

/**
 * Heuristic that tells if the heap should be grown or not.
 */
bool Allocator_ShouldGrow(Allocator *allocator) {
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

    return allocator->freeBlockCount * 2 < allocator->blockCount ||
           4 * unavailableBlockCount > allocator->blockCount;
}

/**
 * Overflow allocation uses only free blocks, it is used when the bump limit of
 * the fast allocator is too small to fit
 * the block to alloc.
 */
word_t *Allocator_overflowAllocation(Allocator *allocator, size_t size) {
    word_t *start = allocator->largeCursor;
    word_t *end = (word_t *)((uint8_t *)start + size);

    if (end > allocator->largeLimit) {
        if (BlockList_IsEmpty(&allocator->freeBlocks)) {
            return NULL;
        }
        BlockHeader *block = BlockList_RemoveFirstBlock(&allocator->freeBlocks);
        allocator->largeBlock = block;
        word_t * blockStart = BlockHeader_GetBlockStart(allocator->blockHeaderStart, allocator->heapStart, block);
        allocator->largeBlockStart = blockStart;
        allocator->largeCursor = blockStart;
        allocator->largeLimit = Block_GetBlockEnd(blockStart);
        return Allocator_overflowAllocation(allocator, size);
    }

    memset(start, 0, size);

    allocator->largeCursor = end;

    return start;
}

/**
 * Allocation fast path, uses the cursor and limit.
 */
INLINE word_t *Allocator_Alloc(Allocator *allocator, size_t size) {
    word_t *start = allocator->cursor;
    word_t *end = (word_t *)((uint8_t *)start + size);

    // Checks if the end of the block overlaps with the limit
    if (end > allocator->limit) {
        // If it overlaps but the block to allocate is a `medium` sized block,
        // use overflow allocation
        if (size > LINE_SIZE) {
            return Allocator_overflowAllocation(allocator, size);
        } else {
            // Otherwise try to get a new line.
            if (Allocator_getNextLine(allocator)) {
                return Allocator_Alloc(allocator, size);
            }

            return NULL;
        }
    }

    memset(start, 0, size);

    allocator->cursor = end;

    return start;
}

/**
 * Updates the cursor and the limit of the Allocator to point the next line of
 * the recycled block
 */
bool Allocator_nextLineRecycled(Allocator *allocator) {
    BlockHeader *block = allocator->block;
    word_t *blockStart = allocator->blockStart;
    assert(BlockHeader_IsRecyclable(block));

    int16_t lineIndex = block->header.first;
    if (lineIndex == LAST_HOLE) {
        return Allocator_newBlock(allocator);
    }

    word_t *line = Block_GetLineAddress(blockStart, lineIndex);

    allocator->cursor = line;
    FreeLineHeader *lineHeader = (FreeLineHeader *)line;
    block->header.first = lineHeader->next;
    uint16_t size = lineHeader->size;
    allocator->limit = line + (size * WORDS_IN_LINE);
    assert(allocator->limit <= Block_GetBlockEnd(blockStart));

    return true;
}

/**
 * Updates the the cursor and the limit of the Allocator to point to the first
 * free line of the new block.
 */
bool Allocator_newBlock(Allocator *allocator) {
    // request the new block.
    BlockHeader *block = Allocator_getNextBlock(allocator);
    // return false if there is no block left.
    if (block == NULL) {
        return false;
    }
    allocator->block = block;
    word_t *blockStart = BlockHeader_GetBlockStart(allocator->blockHeaderStart, allocator->heapStart, block);
    allocator->blockStart = blockStart;

    // The block can be free or recycled.
    if (BlockHeader_IsFree(block)) {
        allocator->cursor = blockStart;
        allocator->limit = Block_GetBlockEnd(blockStart);
    } else {
        assert(BlockHeader_IsRecyclable(block));
        int16_t lineIndex = block->header.first;
        assert(lineIndex < LINE_COUNT);
        word_t *line = Block_GetLineAddress(blockStart, lineIndex);

        allocator->cursor = line;
        FreeLineHeader *lineHeader = (FreeLineHeader *)line;
        block->header.first = lineHeader->next;
        uint16_t size = lineHeader->size;
        assert(size > 0);
        allocator->limit = line + (size * WORDS_IN_LINE);
        assert(allocator->limit <= Block_GetBlockEnd(blockStart));
    }

    return true;
}

bool Allocator_getNextLine(Allocator *allocator) {
    // If cursor is null or the block was free, we need a new block
    if (allocator->cursor == NULL || BlockHeader_IsFree(allocator->block)) {
        return Allocator_newBlock(allocator);
    } else {
        // If we have a recycled block
        return Allocator_nextLineRecycled(allocator);
    }
}

/**
 * Returns a block, first from recycled if available, otherwise from
 * chunk_allocator
 */
BlockHeader *Allocator_getNextBlock(Allocator *allocator) {
    BlockHeader *block = NULL;
    if (!BlockList_IsEmpty(&allocator->recycledBlocks)) {
        block = BlockList_RemoveFirstBlock(&allocator->recycledBlocks);
    } else if (!BlockList_IsEmpty(&allocator->freeBlocks)) {
        block = BlockList_RemoveFirstBlock(&allocator->freeBlocks);
    }
    assert(block == NULL || BlockHeader_GetBlockIndex(allocator->blockHeaderStart, block) < allocator -> blockCount);
    return block;
}
