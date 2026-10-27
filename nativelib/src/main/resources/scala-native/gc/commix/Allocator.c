#if defined(SCALANATIVE_GC_COMMIX)

#include <stdlib.h>
#include "Allocator.h"
#include "State.h"
#include "Sweeper.h"
#include <stdio.h>
#include <memory.h>
#include "shared/ThreadUtil.h"
#include "shared/Log.h"
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

    BlockList_Init(&allocator->recycledBlocks);
    LocalBlockList_Clear(&allocator->parkedBlocks);
    allocator->recycledBlockCount = 0;
}

void Allocator_Clear(Allocator *allocator) {
    BlockList_Clear(&allocator->recycledBlocks);
    LocalBlockList_Clear(&allocator->parkedBlocks);
    allocator->recycledBlockCount = 0;
    allocator->block = NULL;
    allocator->cursor = NULL;
    allocator->limit = NULL;
    allocator->largeBlock = NULL;
    allocator->largeCursor = NULL;
    allocator->largeLimit = NULL;
}

static BlockMeta *Allocator_popParked(Allocator *allocator) {
    LocalBlockList *parked = &allocator->parkedBlocks;
    while (parked->first != NULL) {
        BlockMeta *block = parked->first;
        parked->first =
            BlockList_getNextBlock(allocator->blockMetaStart, block);
        if (parked->first == NULL)
            parked->last = NULL;
        // Medium allocation may have used the remaining lines.
        if (BlockMeta_FirstFreeLine(block) != LAST_HOLE)
            return block;
    }
    return NULL;
}

// Unlinks the first free-line run of at least `size` bytes.
static bool Allocator_takeHole(BlockMeta *block, word_t *blockStart,
                               size_t size, word_t **cursor, word_t **limit) {
    FreeLineMeta *previous = NULL;
    for (int line = BlockMeta_FirstFreeLine(block); line != LAST_HOLE;) {
        FreeLineMeta *hole =
            (FreeLineMeta *)Block_GetLineAddress(blockStart, line);
        if ((size_t)hole->size * LINE_SIZE >= size) {
            if (previous == NULL)
                BlockMeta_SetFirstFreeLine(block, hole->next);
            else
                previous->next = hole->next;
            *cursor = (word_t *)hole;
            *limit = (word_t *)hole + (size_t)hole->size * WORDS_IN_LINE;
            assert(*limit <= Block_GetBlockEnd(blockStart));
            return true;
        }
        previous = hole;
        line = hole->next;
    }
    return false;
}

static bool Allocator_takeMediumHole(Allocator *allocator, BlockMeta *block,
                                     word_t *blockStart, size_t size) {
    word_t *cursor, *limit;
    if (block == NULL ||
        !Allocator_takeHole(block, blockStart, size, &cursor, &limit))
        return false;
    allocator->largeBlock = block;
    allocator->largeBlockStart = blockStart;
    allocator->largeCursor = cursor;
    allocator->largeLimit = limit;
    return true;
}

// `block` was just popped from a recycled list and is on no other list.
static bool Allocator_useRecycledBlock(Allocator *allocator, BlockMeta *block,
                                       size_t size) {
    atomic_thread_fence(memory_order_acquire);
    assert(block->debugFlag == dbg_partial_free);
    word_t *blockStart = BlockMeta_GetBlockStart(allocator->blockMetaStart,
                                                 allocator->heapStart, block);
    if (size <= LINE_SIZE) {
        // Any free line fits a small object. The current block is exhausted.
        word_t *cursor, *limit;
        bool found =
            Allocator_takeHole(block, blockStart, size, &cursor, &limit);
        assert(found);
        (void)found;
#ifdef GC_ASSERTIONS
        block->debugFlag = dbg_in_use;
#endif
        allocator->block = block;
        allocator->blockStart = blockStart;
        allocator->cursor = cursor;
        allocator->limit = limit;
        return true;
    }
    bool found = Allocator_takeMediumHole(allocator, block, blockStart, size);
    if (BlockMeta_FirstFreeLine(block) != LAST_HOLE)
        LocalBlockList_Push(&allocator->parkedBlocks, allocator->blockMetaStart,
                            block);
    return found;
}

static bool Allocator_claimFromList(Allocator *allocator, BlockList *list,
                                    size_t size) {
    BlockMeta *block;
    while ((block = BlockList_Pop(list, allocator->blockMetaStart)) != NULL) {
        if (Allocator_useRecycledBlock(allocator, block, size))
            return true;
    }
    return false;
}

// Sweep spreads recycled blocks over all mutators. After a collection, take
// free lines from any of them before growing the heap.
static bool Allocator_claimRecycled(Allocator *allocator, size_t size) {
    if (size > LINE_SIZE &&
        (Allocator_takeMediumHole(allocator, allocator->largeBlock,
                                  allocator->largeBlockStart, size) ||
         Allocator_takeMediumHole(allocator, allocator->block,
                                  allocator->blockStart, size)))
        return true;
    if (Allocator_claimFromList(allocator, &allocator->recycledBlocks, size))
        return true;

    // Blocking here could deadlock with a collector that holds the lock and
    // waits for this thread. Reach a safepoint before each retry.
    while (!MutatorThreads_tryLockRead()) {
        scalanative_GC_yield();
        thread_yield();
    }
    bool claimed = false;
    MutatorThreads_foreach(mutatorThreads, node) {
        Allocator *source = &node->value->allocator;
        if (source != allocator &&
            Allocator_claimFromList(allocator, &source->recycledBlocks, size)) {
            claimed = true;
            break;
        }
    }
    MutatorThreads_unlockRead();
    return claimed;
}

static word_t *Allocator_allocRecycled(Allocator *allocator, size_t size) {
    if (!Allocator_claimRecycled(allocator, size))
        return NULL;
    word_t **cursor =
        size > LINE_SIZE ? &allocator->largeCursor : &allocator->cursor;
    word_t *start = *cursor;
    *cursor = (word_t *)((uint8_t *)start + size);
    assert(*cursor <=
           (size > LINE_SIZE ? allocator->largeLimit : allocator->limit));
    return start;
}

bool Allocator_newOverflowBlock(Allocator *allocator) {
    BlockMeta *largeBlock =
        BlockAllocator_GetFreeBlock(allocator->blockAllocator);
    if (largeBlock == NULL) {
        return false;
    }
    // A free block has no free-line list for Allocator_takeMediumHole.
    BlockMeta_SetFirstFreeLine(largeBlock, LAST_HOLE);
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
INLINE word_t *Allocator_tryAlloc(Allocator *allocator, size_t size) {
    word_t *start = allocator->cursor;
    if (start == NULL && !Allocator_newBlock(allocator))
        return NULL;

    start = allocator->cursor;
    word_t *end = (word_t *)((uint8_t *)start + size);

    // allocator->limit == NULL implies end > allocator->limit
    assert(allocator->limit != NULL || end > allocator->limit);
    if (end > allocator->limit) {
        // If it overlaps but the block to allocate is a `medium` sized block,
        // use overflow allocation
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
 * Updates the the cursor and the limit of the Allocator to point to the first
 * free line of the new block.
 */
bool Allocator_newBlock(Allocator *allocator) {
    word_t *blockMetaStart = allocator->blockMetaStart;
    BlockMeta *block = Allocator_popParked(allocator);
    if (block == NULL) {
        // Other mutators may pop this list too, see Allocator_claimRecycled.
        block = BlockList_Pop(&allocator->recycledBlocks, blockMetaStart);
    }
    word_t *blockStart;

    if (block != NULL) {
        // get all the changes done by sweeping
        atomic_thread_fence(memory_order_acquire);
        GC_LOG_DEBUG("Allocator_newBlock RECYCLED %p %" PRIu32 " for %p", block,
                     BlockMeta_GetBlockIndex(blockMetaStart, block), allocator);
        assert(block->debugFlag == dbg_partial_free);
#ifdef GC_ASSERTIONS
        block->debugFlag = dbg_in_use;
#endif
        blockStart = BlockMeta_GetBlockStart(blockMetaStart,
                                             allocator->heapStart, block);

        int lineIndex = BlockMeta_FirstFreeLine(block);
        assert(lineIndex >= 0 && lineIndex < LINE_COUNT);
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
        block = BlockAllocator_GetFreeBlock(allocator->blockAllocator);
        GC_LOG_DEBUG("Allocator_newBlock %p %" PRIu32 " for %p", block,
                     BlockMeta_GetBlockIndex(blockMetaStart, block), allocator);
        if (block == NULL) {
            return false;
        }
        blockStart = BlockMeta_GetBlockStart(blockMetaStart,
                                             allocator->heapStart, block);

        allocator->cursor = blockStart;
        allocator->limit = Block_GetBlockEnd(blockStart);
        BlockMeta_SetFirstFreeLine(block, LAST_HOLE);
    }

    allocator->block = block;
    allocator->blockStart = blockStart;

    return true;
}

INLINE
word_t *Allocator_lazySweep(Allocator *allocator, Heap *heap, uint32_t size) {
    word_t *object = NULL;
    Stats_DefineOrNothing(stats, heap->stats);
    Stats_RecordTime(stats, start_ns);
    // mark as active
    heap->lazySweep.lastActivity = BlockRange_Pack(1, heap->sweep.cursor);
    while (object == NULL && heap->sweep.cursor < heap->sweep.limit) {
        Sweeper_Sweep(heap->stats, &heap->lazySweep.cursorDone,
                      LAZY_SWEEP_MIN_BATCH, currentMutatorThread);
        object = Allocator_tryAlloc(allocator, size);
    }
    // mark as inactive
    heap->lazySweep.lastActivity = BlockRange_Pack(0, heap->sweep.cursor);
    while (object == NULL && !Sweeper_IsSweepDone(heap)) {
        object = Allocator_tryAlloc(allocator, size);
        if (object == NULL) {
            scalanative_GC_yield();
            thread_yield();
            atomic_thread_fence(memory_order_acquire);
        }
    }
    Stats_RecordTime(stats, end_ns);
    Stats_RecordEvent(stats, event_sweep, start_ns, end_ns);
    return object;
}

NOINLINE word_t *Allocator_allocSlow(Allocator *allocator, Heap *heap,
                                     uint32_t size) {
    do {
        word_t *object = Allocator_tryAlloc(allocator, size);

        if (object != NULL) {
        done:
            assert(Heap_IsWordInHeap(heap, object));
            assert(object != NULL);
            ObjectMeta *objectMeta = Bytemap_Get(allocator->bytemap, object);
#ifdef GC_ASSERTIONS
            ObjectMeta_AssertIsValidAllocation(objectMeta, size);
#endif
            memset(object, 0, size);
            ObjectMeta_SetAllocated(objectMeta);
            return object;
        }

        if (!Sweeper_IsSweepDone(heap)) {
            object = Allocator_lazySweep(allocator, heap, size);

            if (object != NULL)
                goto done;
        }

        // Another thread collects instead. Retry before growing the heap.
        if (!Heap_Collect(heap, true))
            continue;
        size_t collection = heap->gcCollectionStart_ns;
        object = Allocator_tryAlloc(allocator, size);

        if (object != NULL)
            goto done;

        if (!Sweeper_IsSweepDone(heap)) {
            object = Allocator_lazySweep(allocator, heap, size);

            if (object != NULL)
                goto done;
        }

        if (Sweeper_IsSweepDone(heap)) {
            object = Allocator_allocRecycled(allocator, size);
            if (object != NULL)
                goto done;
        }

        // A later collection may have freed memory while this thread yielded.
        if (heap->gcCollectionStart_ns != collection)
            continue;

        // A small object can always fit in a single free block
        // because it is no larger than 8K while the block is 32K.
        if (!Heap_TryGrow(heap, 1))
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

    word_t *object = start;
    ObjectMeta *objectMeta = Bytemap_Get(heap->bytemap, object);
#ifdef GC_ASSERTIONS
    ObjectMeta_AssertIsValidAllocation(objectMeta, size);
#endif
    memset(start, 0, size);
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
