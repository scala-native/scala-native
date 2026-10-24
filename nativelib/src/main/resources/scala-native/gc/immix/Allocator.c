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
    allocator->canUseReserve = false;
}

void Allocator_Clear(Allocator *allocator) {
    allocator->canUseReserve = false;
    BlockList_Clear(&allocator->recycledBlocks);
    allocator->recycledBlockCount = 0;
    allocator->block = NULL;
    allocator->cursor = NULL;
    allocator->limit = NULL;
    allocator->largeBlock = NULL;
    allocator->largeCursor = NULL;
    allocator->largeLimit = NULL;
}

bool Allocator_newOverflowBlock(Allocator *allocator) {
    BlockMeta *largeBlock = BlockAllocator_GetFreeBlockForSmallAllocation(
        allocator->blockAllocator, allocator->canUseReserve);
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

    if (start == NULL ||
        (word_t *)((uint8_t *)start + size) > allocator->largeLimit) {
        if (!Allocator_newOverflowBlock(allocator)) {
            return NULL;
        }
        return Allocator_overflowAllocation(allocator, size);
    }

    allocator->largeCursor = (word_t *)((uint8_t *)start + size);

    return start;
}

/**
 * Allocation fast path, uses the cursor and limit.
 */
bool Allocator_Reserve(Allocator *allocator, size_t size) {
    word_t *start = allocator->cursor;
    // Recovery can provide an overflow run without a primary block. Retrying
    // that medium request must consume its reservation before seeking a block.
    if (start == NULL && size > LINE_SIZE && allocator->largeCursor != NULL &&
        (uint8_t *)allocator->largeCursor + size <=
            (uint8_t *)allocator->largeLimit)
        return true;
    if (start == NULL && !Allocator_newBlock(allocator))
        return false;

    start = allocator->cursor;
    word_t *end = (word_t *)((uint8_t *)start + size);
    // Checks if the end of the block overlaps with the limit
    if (end > allocator->limit) {
        // If it overlaps but the block to allocate is a `medium` sized
        // block, use overflow allocation
        if (size > LINE_SIZE) {
            return (allocator->largeCursor != NULL &&
                    (uint8_t *)allocator->largeCursor + size <=
                        (uint8_t *)allocator->largeLimit) ||
                   Allocator_newOverflowBlock(allocator);
        } else {
            // Otherwise try to get a new line.
            if (Allocator_getNextLine(allocator)) {
                return Allocator_Reserve(allocator, size);
            }
            return false;
        }
    }

    return true;
}

INLINE word_t *Allocator_tryAlloc(Allocator *allocator, size_t size) {
    if (!Allocator_Reserve(allocator, size))
        return NULL;
    if (allocator->cursor == NULL ||
        (uint8_t *)allocator->cursor + size > (uint8_t *)allocator->limit)
        return Allocator_overflowAllocation(allocator, size);
    word_t *object = allocator->cursor;
    allocator->cursor = (word_t *)((uint8_t *)object + size);
    return object;
}

static bool Allocator_reserveHole(Allocator *allocator, BlockMeta *block,
                                  word_t *start, size_t size) {
    FreeLineMeta *previous = NULL;
    for (int line = BlockMeta_FirstFreeLine(block); line != LAST_HOLE;) {
        FreeLineMeta *hole = (FreeLineMeta *)Block_GetLineAddress(start, line);
        if ((size_t)hole->size * LINE_SIZE >= size) {
            if (previous != NULL)
                previous->next = hole->next;
            else
                BlockMeta_SetFirstFreeLine(block, hole->next);
            word_t *cursor = (word_t *)hole;
            word_t *limit = cursor + hole->size * WORDS_IN_LINE;
            if (size > LINE_SIZE) {
                allocator->largeCursor = cursor;
                allocator->largeLimit = limit;
            } else {
                allocator->block = NULL;
                allocator->cursor = cursor;
                allocator->limit = limit;
            }
            return true;
        }
        previous = hole;
        line = hole->next;
    }
    return false;
}

// Sweep retains per-mutator allocation lists. Before growing or reporting OOM,
// recover a suitable run even if sweep assigned it to an idle mutator. The
// collection owner holds the stopped-world mutator-list lock throughout.
bool Allocator_ReserveRecycled(Allocator *allocator, size_t size) {
    if (size > LINE_SIZE && allocator->block != NULL &&
        Allocator_reserveHole(allocator, allocator->block,
                              allocator->blockStart, size))
        return true;
    MutatorThreads_foreach(mutatorThreads, node) {
        Allocator *source = &node->value->allocator;
        BlockList *list = &source->recycledBlocks;
        BlockMeta *previous = NULL;
        for (BlockMeta *block = list->first; block != NULL;) {
            BlockMeta *next =
                BlockList_getNextBlock(list->blockMetaStart, block);
            word_t *start = BlockMeta_GetBlockStart(
                allocator->blockMetaStart, allocator->heapStart, block);
            if (Allocator_reserveHole(allocator, block, start, size)) {
                if (BlockMeta_FirstFreeLine(block) == LAST_HOLE) {
                    if (previous == NULL)
                        list->first = next;
                    else
                        BlockList_SetNext(list, previous, next);
                    if (list->last == block)
                        list->last = previous;
                    block->nextBlock = LAST_BLOCK;
                    atomic_fetch_sub_explicit(&source->recycledBlockCount, 1,
                                              memory_order_relaxed);
                }
                return true;
            }
            previous = block;
            block = next;
        }
    }
    return false;
}

/**
 * Updates the cursor and the limit of the Allocator to point the next line.
 */
bool Allocator_getNextLine(Allocator *allocator) {
    BlockMeta *block = allocator->block;
    if (block == NULL) {
        return Allocator_newBlock(allocator);
    }

    int lineIndex = BlockMeta_FirstFreeLine(block);
    if (lineIndex == LAST_HOLE) {
        return Allocator_newBlock(allocator);
    }

    word_t *blockStart = allocator->blockStart;
    word_t *line = Block_GetLineAddress(blockStart, lineIndex);

    allocator->cursor = line;
    FreeLineMeta *lineMeta = (FreeLineMeta *)line;
    uint16_t size = lineMeta->size;
    if (size == 0)
        return Allocator_newBlock(allocator);
    assert(lineMeta->next == LAST_HOLE ||
           (lineMeta->next >= 0 && lineMeta->next <= LINE_COUNT));
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
        atomic_fetch_sub_explicit(&allocator->recycledBlockCount, 1,
                                  memory_order_relaxed);
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
        atomic_thread_fence(memory_order_acquire);
#endif
        blockStart = BlockMeta_GetBlockStart(allocator->blockMetaStart,
                                             allocator->heapStart, block);

        int lineIndex = BlockMeta_FirstFreeLine(block);
        assert(lineIndex < LINE_COUNT);
        word_t *line = Block_GetLineAddress(blockStart, lineIndex);

        FreeLineMeta *lineMeta = (FreeLineMeta *)line;
        uint16_t size = lineMeta->size;
        assert(size > 0);
        assert(lineMeta->next == LAST_HOLE ||
               (lineMeta->next >= 0 && lineMeta->next <= LINE_COUNT));
        BlockMeta_SetFirstFreeLine(block, lineMeta->next);
        allocator->cursor = line;
        allocator->limit = line + (size * WORDS_IN_LINE);
        assert(allocator->limit <= Block_GetBlockEnd(blockStart));
    } else {
        block = BlockAllocator_GetFreeBlockForSmallAllocation(
            allocator->blockAllocator, allocator->canUseReserve);
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
        uint64_t generation = atomic_load_explicit(&heap->collectionGeneration,
                                                   memory_order_acquire);
        word_t *object = Allocator_tryAlloc(allocator, size);

        if (object != NULL) {
            assert(Heap_IsWordInHeap(heap, object));
            assert(object != NULL);
            memset(object, 0, size);
            ObjectMeta *objectMeta = Bytemap_Get(allocator->bytemap, object);
#ifdef GC_ASSERTIONS
            ObjectMeta_AssertIsValidAllocation(objectMeta, size);
#endif
            ObjectMeta_SetAllocated(objectMeta);
            return object;
        }
        if (!Heap_RecoverAllocation(heap, size, generation))
            Heap_ThrowOutOfMemory(heap);
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
    if (start == NULL || end > allocator->limit) {
        return Allocator_allocSlow(allocator, heap, size);
    }

    allocator->cursor = end;

    memset(start, 0, size);

    word_t *object = start;
    ObjectMeta *objectMeta = Bytemap_Get(heap->bytemap, object);
#ifdef GC_ASSERTIONS
    ObjectMeta_AssertIsValidAllocation(objectMeta, size);
#endif
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
