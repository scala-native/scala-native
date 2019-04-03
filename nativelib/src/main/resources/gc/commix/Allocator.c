#include <stdlib.h>
#include "Allocator.h"
#include "State.h"
#include "Sweeper.h"
#include <stdio.h>
#include <memory.h>

bool Allocator_newBlock(Allocator *allocator);
bool Allocator_newOverflowBlock(Allocator *allocator);
bool Allocator_newPretenureBlock(Allocator *allocator);

INLINE bool Allocator_YoungGenerationFull() {
    uint32_t heapSize = (void *)heap.heapEnd - (void *)heap.heapStart;
    uint32_t maxBlocksInHeap = heapSize / SPACE_USED_PER_BLOCK;
    uint32_t bound = (MAX_YOUNG_BLOCKS_RATIO * maxBlocksInHeap)/100;
    uint_fast32_t blockCount = atomic_load_explicit(&blockAllocator.youngBlockCount, memory_order_acquire);
    return blockCount >= bound;
}

void Allocator_Init(Allocator *allocator, BlockAllocator *blockAllocator,
                    Bytemap *bytemap, word_t *blockMetaStart,
                    word_t *heapStart) {
    allocator->blockMetaStart = blockMetaStart;
    allocator->blockAllocator = blockAllocator;
    allocator->bytemap = bytemap;
    allocator->heapStart = heapStart;

    // Init cursor
    bool didInit = Allocator_newBlock(allocator);
    assert(didInit);

    // Init large cursor
    bool didLargeInit = Allocator_newOverflowBlock(allocator);
    assert(didLargeInit);

    // Init pretenure cursors only if needed
    if (PRETENURE_OBJECT) {
        bool didPretenureInit = Allocator_newPretenureBlock(allocator);
        assert(didPretenureInit);
    }
}

/**
 * The Allocator needs one free block for overflow allocation; a free
 * block for normal allocation and one free block for pretenure allocation
 *
 * @param allocator
 * @return `true` if there are enough block to initialise the cursors, `false`
 * otherwise.
 */
bool Allocator_CanInitCursors(Allocator *allocator) {
    return allocator->blockAllocator->freeBlockCount >= 3;
}

void Allocator_Clear(Allocator *allocator) {
    allocator->limit = NULL;
    allocator->block = NULL;
    allocator->pretenureLimit = NULL;
    allocator->pretenureBlock = NULL;
    allocator->largeLimit = NULL;
    allocator->largeBlock = NULL;
}

bool Allocator_newOverflowBlock(Allocator *allocator) {
    BlockMeta *largeBlock =
        BlockAllocator_GetFreeBlock(allocator->blockAllocator);
    if (largeBlock == NULL) {
        return false;
    }
    uint_fast32_t youngBlocks = atomic_fetch_add(&allocator->blockAllocator->youngBlockCount, 1);

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

    // allocator->largeLimit == NULL implies end > allocator->largeLimit
    assert(allocator->largeLimit != NULL || end > allocator->largeLimit);
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

    // allocator->limit == NULL implies end > allocator->limit
    assert(allocator->limit != NULL || end > allocator->limit);
    if (end > allocator->limit) {
        // If it overlaps but the block to allocate is a `medium` sized block,
        // use overflow allocation
        if (size > LINE_SIZE) {
            return Allocator_overflowAllocation(allocator, size);
        } else {
            // Otherwise try to get a new block.
            if (Allocator_newBlock(allocator)) {
                return Allocator_tryAlloc(allocator, size);
            }

            return NULL;
        }
    }

    allocator->cursor = end;

    return start;
}

/**
 * Allocation of pretenure object, fast path
 */
INLINE word_t *Allocator_tryAllocPretenure(Allocator *allocator, size_t size) {
    assert(false);
    word_t *start = allocator->pretenureCursor;
    word_t *end = (word_t *)((uint8_t *)start + size);

    assert(allocator->pretenureLimit != NULL || end > allocator->pretenureLimit);
    if (end > allocator->pretenureLimit) {
        if (Allocator_newPretenureBlock(allocator)) {
            return Allocator_tryAllocPretenure(allocator, size);
        }
        return NULL;
    }

    allocator->pretenureCursor = end;
    return start;
}

/**
 * Updates the the cursor and the limit of the Allocator to point to the first
 * free line of the new block.
 */
bool Allocator_newBlock(Allocator *allocator) {
    if (Allocator_YoungGenerationFull()) {
#ifdef DEBUG_PRINT
        printf("Allocator_newBlock : young generation is full\n");
        fflush(stdout);
#endif
        return NULL;
    }
    word_t *blockMetaStart = allocator->blockMetaStart;
    BlockMeta *block = BlockAllocator_GetFreeBlock(allocator->blockAllocator);
#ifdef DEBUG_PRINT
    printf("Allocator_newBlock %p %" PRIu32 "\n", block,
           BlockMeta_GetBlockIndex(blockMetaStart, block));
    fflush(stdout);
#endif
    if (block == NULL) {
        return false;
    }
    assert(!BlockMeta_IsOld(block));
    word_t *blockStart = BlockMeta_GetBlockStart(blockMetaStart,
                                         allocator->heapStart, block);

    allocator->cursor = blockStart;
    allocator->limit = Block_GetBlockEnd(blockStart);
    BlockMeta_SetFirstFreeLine(block, LAST_HOLE);

    allocator->block = block;
    allocator->blockStart = blockStart;

    uint_fast32_t youngBlocks = atomic_fetch_add(&allocator->blockAllocator->youngBlockCount, 1);

    return true;
}

bool Allocator_newPretenureBlock(Allocator *allocator) {
    word_t *blockMetaStart = allocator->blockMetaStart;
    BlockMeta *block = BlockAllocator_GetFreeBlock(allocator->blockAllocator);
#ifdef DEBUG_PRINT
    printf("Allocator_newPretenureBlock %p %" PRIu32 "\n", block,
           BlockMeta_GetBlockIndex(blockMetaStart, block));
    fflush(stdout);
#endif
    if (block == NULL) {
        return false;
    }
    BlockMeta_SetOld(block);
    word_t *blockStart = BlockMeta_GetBlockStart(blockMetaStart,
                                         allocator->heapStart, block);

    allocator->pretenureCursor = blockStart;
    allocator->pretenureLimit = Block_GetBlockEnd(blockStart);
    BlockMeta_SetFirstFreeLine(block, LAST_HOLE);

    allocator->pretenureBlock = block;
    allocator->pretenureBlockStart = blockStart;

    return true;
}

INLINE
word_t *Allocator_lazySweep(Heap *heap, uint32_t size, bool pretenureObject) {
    word_t *object = NULL;
    Stats_DefineOrNothing(stats, heap->stats);
    Stats_RecordTime(stats, start_ns);
    // mark as active
    printf("Lazy sweeping %d\n", heap->lazySweep.nextSweepOld);
    fflush(stdout);
    heap->lazySweep.lastActivity = BlockRange_Pack(1, heap->sweep.cursor);
    while (object == NULL && heap->sweep.cursor < heap->sweep.limit) {
        Sweeper_Sweep(heap, heap->stats, &heap->lazySweep.cursorDone,
                      LAZY_SWEEP_MIN_BATCH, heap->lazySweep.nextSweepOld);
        if (!pretenureObject) {
            object = Allocator_tryAlloc(&allocator, size);
        } else {
            object = Allocator_tryAllocPretenure(&allocator, size);
        }
    }
    // mark as inactive
    heap->lazySweep.lastActivity = BlockRange_Pack(0, heap->sweep.cursor);
    while (object == NULL && !Sweeper_IsSweepDone(heap)) {
        if (!pretenureObject) {
            object = Allocator_tryAlloc(&allocator, size);
        } else {
            object = Allocator_tryAllocPretenure(&allocator, size);
        }
        if (object == NULL) {
            sched_yield();
        }
    }
    Stats_RecordTime(stats, end_ns);
    Stats_RecordEvent(stats, event_sweep, start_ns, end_ns);
    return object;
}

NOINLINE word_t *Allocator_allocSlow(Heap *heap, uint32_t size) {
    word_t *object = Allocator_tryAlloc(&allocator, size);

    if (object != NULL) {
    done:
        if (!Heap_IsWordInHeap(heap, object)) {
            printf("Object %p allocated outstide the heap\n", object);
            fflush(stdout);
        }
        assert(Heap_IsWordInHeap(heap, object));
        assert(object != NULL);
        memset(object, 0, size);
        ObjectMeta *objectMeta = Bytemap_Get(allocator.bytemap, object);
#ifdef DEBUG_ASSERT
        ObjectMeta_AssertIsValidAllocation(objectMeta, size);
#endif
        ObjectMeta_SetAllocatedNew(objectMeta);
        return object;
    }

    if (!Sweeper_IsSweepDone(heap)) {
        object = Allocator_lazySweep(heap, size, false);

        if (object != NULL)
            goto done;
    }

    Heap_Collect(heap, false);
    object = Allocator_tryAlloc(&allocator, size);

    if (object != NULL)
        goto done;

    if (!Sweeper_IsSweepDone(heap)) {
        object = Allocator_lazySweep(heap, size, false);

        if (object != NULL)
            goto done;
    }

    Heap_Collect(heap, true);
    object = Allocator_tryAlloc(&allocator, size);

    if (object != NULL)
        goto done;

    if (!Sweeper_IsSweepDone(heap)) {
        object = Allocator_lazySweep(heap, size, false);

        if (object != NULL)
            goto done;
    }

    // A small object can always fit in a single free block
    // because it is no larger than 8K while the block is 32K.
    Heap_Grow(heap, 1);
    object = Allocator_tryAlloc(&allocator, size);

    goto done;
}

NOINLINE word_t *Allocator_allocPretenureSlow(Heap *heap, uint32_t size) {
    word_t *object = Allocator_tryAllocPretenure(&allocator, size);

    if (object != NULL) {
    done:
        assert(Heap_IsWordInHeap(heap, object));
        assert(object != NULL);
        memset(object, 0, size);
        ObjectMeta *objectMeta = Bytemap_Get(allocator.bytemap, object);
#ifdef DEBUG_ASSERT
        ObjectMeta_AssertIsValidAllocation(objectMeta, size);
#endif
        ObjectMeta_SetMarkedNew(objectMeta);
        return object;
    }

    if (!Sweeper_IsSweepDone(heap)) {
        object = Allocator_lazySweep(heap, size, true);

        if (object != NULL)
            goto done;
    }

    Heap_Collect(heap, false);
    object = Allocator_tryAllocPretenure(&allocator, size);

    if (object != NULL)
        goto done;

    if (!Sweeper_IsSweepDone(heap)) {
        object = Allocator_lazySweep(heap, size, true);

        if (object != NULL)
            goto done;
    }

    Heap_Collect(heap, true);
    object = Allocator_tryAllocPretenure(&allocator, size);

    if (object != NULL)
        goto done;

    if (!Sweeper_IsSweepDone(heap)) {
        object = Allocator_lazySweep(heap, size, true);

        if (object != NULL)
            goto done;
    }

    // A small object can always fit in a single free block
    // because it is no larger than 8K while the block is 32K.
    Heap_Grow(heap, 1);
    object = Allocator_tryAllocPretenure(&allocator, size);

    goto done;
}

INLINE word_t *Allocator_Alloc(Heap *heap, uint32_t size) {
    assert(size % ALLOCATION_ALIGNMENT == 0);
    assert(size < MIN_BLOCK_SIZE);

    word_t *start = allocator.cursor;
    word_t *end = (word_t *)((uint8_t *)start + size);

    // Checks if the end of the block overlaps with the limit
    if (end > allocator.limit) {
        return Allocator_allocSlow(heap, size);
    }

    allocator.cursor = end;

    memset(start, 0, size);

    word_t *object = start;
    ObjectMeta *objectMeta = Bytemap_Get(allocator.bytemap, object);
#ifdef DEBUG_ASSERT
    ObjectMeta_AssertIsValidAllocation(objectMeta, size);
#endif
    ObjectMeta_SetAllocatedNew(objectMeta);

    // prefetch starting from 36 words away from the object start
    // rw = 0 => prefetch for reading
    // locality = 3 => data has high locality, leave the values in as many caches as possible
    __builtin_prefetch(object + 36, 0, 3);

    assert(Heap_IsWordInHeap(heap, object));
    return object;
}

INLINE word_t *Allocator_AllocPretenure(Heap *heap, uint32_t size) {
    assert(size % ALLOCATION_ALIGNMENT == 0);
    assert(size < MIN_BLOCK_SIZE);

    word_t *start = allocator.pretenureCursor;
    word_t *end = (word_t *)((uint8_t *)start + size);

    // Checks if the end of the block overlaps with the limit
    if (end > allocator.pretenureLimit) {
        return Allocator_allocPretenureSlow(heap, size);
    }

    allocator.pretenureCursor = end;

    memset(start, 0, size);

    word_t *object = start;
    ObjectMeta *objectMeta = Bytemap_Get(allocator.bytemap, object);
#ifdef DEBUG_ASSERT
    ObjectMeta_AssertIsValidAllocation(objectMeta, size);
#endif
    ObjectMeta_SetMarkedNew(objectMeta);

    __builtin_prefetch(object + 36, 0, 3);

    assert(Heap_IsWordInHeap(heap, object));
    return object;
}

