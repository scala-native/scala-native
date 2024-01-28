#if defined(SCALANATIVE_GC_IMMIX)

#include <stdlib.h>
#include "Allocator.h"
#include "State.h"
#include <stdio.h>
#include <memory.h>
#include <assert.h>

bool Allocator_getNextLine(Allocator *allocator);
bool Allocator_newBlock(Allocator *allocator);
bool Allocator_newOverflowBlock(Allocator *allocator);

void Allocator_Init(Allocator *allocator, BlockAllocator *blockAllocator,
                    Bytemap *bytemap, word_t *blockMetaStart,
                    word_t *heapStart) {
    allocator->blockMetaStart = blockMetaStart;
    allocator->blockAllocator = blockAllocator;
    allocator->bytemap = bytemap;
    allocator->heapStart = heapStart;

    BlockList_Init(&allocator->recycledBlocks, blockMetaStart);
    allocator->recycledBlockCount = 0;
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
    uint32_t freeBlockCount =
        (uint32_t)allocator->blockAllocator->freeBlockCount;
    return freeBlockCount >= 2 ||
           (freeBlockCount == 1 && allocator->recycledBlockCount > 0);
}

void Allocator_InitCursors(Allocator *allocator, bool canCollect) {
    while (!(Allocator_newBlock(allocator) &&
             Allocator_newOverflowBlock(allocator))) {
        if (Heap_isGrowingPossible(&heap, 2))
            Heap_Grow(&heap, 2);
        else if (canCollect)
            Heap_Collect(&heap, &stack);
        else
            Heap_exitWithOutOfMemory(
                "Not enough memory to allocate GC mutator thread allocator");
    }
}

void Allocator_Clear(Allocator *allocator) {
    BlockList_Clear(&allocator->recycledBlocks);
    allocator->recycledBlockCount = 0;
    allocator->limit = NULL;
    allocator->block = NULL;
    allocator->largeLimit = NULL;
    allocator->largeBlock = NULL;
}

bool Allocator_newOverflowBlock(Allocator *allocator) {
    BlockMeta *largeBlock =
        BlockAllocator_GetFreeBlock(allocator->blockAllocator);
    if (largeBlock == NULL) {
        return false;
    }
    allocator->largeBlock = largeBlock;
    word_t *largeBlockStart = BlockMeta_GetBlockStart(
        allocator->blockMetaStart, allocator->heapStart, largeBlock);
    allocator->largeBlockStart = largeBlockStart;
    allocator->largeCursor = largeBlockStart;
    allocator->largeLimit = Block_GetBlockEnd(largeBlockStart);
    return true;
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
        if (!Allocator_newOverflowBlock(allocator)) {
            return NULL;
        }
        return Allocator_overflowAllocation(allocator, size);
    }

    allocator->largeCursor = end;

    return start;
}

/**
 * Allocation fast path, uses the cursor and limit.
 */
INLINE word_t *Allocator_tryAlloc(Allocator *allocator, size_t size) {
    word_t *start = allocator->cursor;
    word_t *end = (word_t *)((uint8_t *)start + size);
    // Checks if the end of the block overlaps with the limit
    if (end > allocator->limit) {
        // If it overlaps but the block to allocate is a `medium` sized
        // block, use overflow allocation
        if (size > LINE_SIZE) {
            return Allocator_overflowAllocation(allocator, size);
        } else {
            // Otherwise try to get a new line.
            if (Allocator_getNextLine(allocator)) {
                return Allocator_tryAlloc(allocator, size);
            }
            return NULL;
        }
    }

    allocator->cursor = end;

    return start;
}

/**
 * Updates the cursor and the limit of the Allocator to point the next line.
 */
bool Allocator_getNextLine(Allocator *allocator) {
    BlockMeta *block = allocator->block;
    if (block == NULL) {
        return Allocator_newBlock(allocator);
    }
    word_t *blockStart = allocator->blockStart;

    int lineIndex = BlockMeta_FirstFreeLine(block);
    if (lineIndex == LAST_HOLE) {
        return Allocator_newBlock(allocator);
    }

    word_t *line = Block_GetLineAddress(blockStart, lineIndex);

    allocator->cursor = line;
    FreeLineMeta *lineMeta = (FreeLineMeta *)line;
    uint16_t size = lineMeta->size;
    if (size == 0)
        return Allocator_newBlock(allocator);
    BlockMeta_SetFirstFreeLine(block, lineMeta->next);
    allocator->limit = line + (size * WORDS_IN_LINE);
    assert(allocator->limit <= Block_GetBlockEnd(blockStart));

    return true;
}

/**
 * Updates the the cursor and the limit of the Allocator to point to the
 * first free line of the new block.
 */
bool Allocator_newBlock(Allocator *allocator) {
    assert(allocator != NULL);
    BlockMeta *block = BlockList_Poll(&allocator->recycledBlocks);
    word_t *blockStart;

    if (block != NULL) {
        blockStart = BlockMeta_GetBlockStart(allocator->blockMetaStart,
                                             allocator->heapStart, block);

        int lineIndex = BlockMeta_FirstFreeLine(block);
        assert(lineIndex < LINE_COUNT);
        word_t *line = Block_GetLineAddress(blockStart, lineIndex);

        allocator->cursor = line;
        FreeLineMeta *lineMeta = (FreeLineMeta *)line;
        BlockMeta_SetFirstFreeLine(block, lineMeta->next);
        uint16_t size = lineMeta->size;
        assert(size > 0);
        allocator->limit = line + (size * WORDS_IN_LINE);
        assert(allocator->limit <= Block_GetBlockEnd(blockStart));
    } else {
        block = BlockAllocator_GetFreeBlock(allocator->blockAllocator);
        if (block == NULL) {
            return false;
        }
        blockStart = BlockMeta_GetBlockStart(allocator->blockMetaStart,
                                             allocator->heapStart, block);

        allocator->cursor = blockStart;
        allocator->limit = Block_GetBlockEnd(blockStart);
        BlockMeta_SetFirstFreeLine(block, LAST_HOLE);
    }

    allocator->block = block;
    allocator->blockStart = blockStart;

    return true;
}

NOINLINE word_t *Allocator_allocSlow(Allocator *allocator, Heap *heap,
                                     uint32_t size) {
    do {
        word_t *object = Allocator_tryAlloc(allocator, size);

        if (object != NULL) {
        done:
            assert(Heap_IsWordInHeap(heap, object));
            assert(object != NULL);
            memset(object, 0, size);
            ObjectMeta *objectMeta = Bytemap_Get(allocator->bytemap, object);
            ObjectMeta_SetAllocated(objectMeta);
            return object;
        }
        Heap_Collect(heap, &stack);
        object = Allocator_tryAlloc(allocator, size);

        if (object != NULL)
            goto done;

        // A small object can always fit in a single free block
        // because it is no larger than 8K while the block is 32K.
        if (Heap_isGrowingPossible(heap, 1))
            Heap_Grow(heap, 1);
        else
            Heap_exitWithOutOfMemory("");
    } while (true);
    return NULL; // unreachable
}

INLINE word_t *Allocator_Alloc(Heap *heap, uint32_t size) {
    assert(size % ALLOCATION_ALIGNMENT == 0);
    assert(size < MIN_BLOCK_SIZE);

    Allocator *allocator = &currentMutatorThread->allocator;
    word_t *start = allocator->cursor;
    word_t *end = (word_t *)((uint8_t *)start + size);

    // Checks if the end of the block overlaps with the limit
    if (end > allocator->limit) {
        return Allocator_allocSlow(allocator, heap, size);
    }

    allocator->cursor = end;

    memset(start, 0, size);

    word_t *object = start;
    ObjectMeta *objectMeta = Bytemap_Get(heap->bytemap, object);
    ObjectMeta_SetAllocated(objectMeta);

    // prefetch starting from 36 words away from the object start
    // rw = 0 => prefetch for reading
    // locality = 3 => data has high locality, leave the values in as many
    // caches as possible
    __builtin_prefetch(object + 36, 0, 3);

    assert(Heap_IsWordInHeap(heap, object));
    return object;
}

#endif
