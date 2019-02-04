#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "LargeAllocator.h"
#include "utils/MathUtils.h"
#include "Object.h"
#include "State.h"
#include "Sweeper.h"
#include "Log.h"
#include "headers/ObjectHeader.h"

inline static int LargeAllocator_sizeToLinkedListIndex(size_t size) {
    assert(size >= MIN_BLOCK_SIZE);
    assert(size % MIN_BLOCK_SIZE == 0);
    int index = size / MIN_BLOCK_SIZE - 1;
    assert(index < FREE_LIST_COUNT);
    return index;
}

Chunk *LargeAllocator_chunkAddOffset(Chunk *chunk, size_t words) {
    return (Chunk *)((ubyte_t *)chunk + words);
}

void LargeAllocator_freeListPush(FreeList *freeList, Chunk *chunk) {
    Chunk *head = (Chunk *)freeList->head;
    do {
        // head will be replaced with actual value if
        // atomic_compare_exchange_strong fails
        chunk->next = head;
    } while (!atomic_compare_exchange_strong(&freeList->head, (word_t *)&head,
                                             (word_t)chunk));
}

// This could suffer from the ABA problem. However, during a single phase each BlockMeta is removed no more than once.
// It would need to be swept before re-use.
Chunk *LargeAllocator_freeListPop(FreeList *freeList) {
    Chunk *head = (Chunk *)freeList->head;
    word_t newValue;
    do {
        // head will be replaced with actual value if
        // atomic_compare_exchange_strong fails
        if (head == NULL) {
            return NULL;
        }
        newValue = (word_t)head->next;
    } while (!atomic_compare_exchange_strong(&freeList->head, (word_t *)&head,
                                             newValue));
    return head;
}

void LargeAllocator_freeListInit(FreeList *freeList) {
    freeList->head = (word_t)NULL;
}

void LargeAllocator_Init(LargeAllocator *allocator,
                         BlockAllocator *blockAllocator, Bytemap *bytemap,
                         word_t *blockMetaStart, word_t *heapStart) {
    allocator->heapStart = heapStart;
    allocator->blockMetaStart = blockMetaStart;
    allocator->bytemap = bytemap;
    allocator->blockAllocator = blockAllocator;

    for (int i = 0; i < FREE_LIST_COUNT; i++) {
        LargeAllocator_freeListInit(&allocator->freeLists[i]);
    }
}

void LargeAllocator_Clear(LargeAllocator *allocator) {
    for (int i = 0; i < FREE_LIST_COUNT; i++) {
        LargeAllocator_freeListInit(&allocator->freeLists[i]);
    }
}

void LargeAllocator_addChunk(LargeAllocator *allocator, Chunk *chunk,
                             size_t total_block_size) {
    assert(total_block_size >= MIN_BLOCK_SIZE);
    assert(total_block_size < BLOCK_TOTAL_SIZE);
    assert(total_block_size % MIN_BLOCK_SIZE == 0);

    int listIndex = LargeAllocator_sizeToLinkedListIndex(total_block_size);
    chunk->nothing = NULL;
    chunk->size = total_block_size;
    ObjectMeta *chunkMeta = Bytemap_Get(allocator->bytemap, (word_t *)chunk);
    ObjectMeta_SetPlaceholder(chunkMeta);

    LargeAllocator_freeListPush(&allocator->freeLists[listIndex], chunk);
}

static inline Chunk *LargeAllocator_getChunkForSize(LargeAllocator *allocator,
                                                    size_t requiredChunkSize) {
    for (int listIndex =
             LargeAllocator_sizeToLinkedListIndex(requiredChunkSize);
         listIndex < FREE_LIST_COUNT; listIndex++) {
        Chunk *chunk =
            LargeAllocator_freeListPop(&allocator->freeLists[listIndex]);
        if (chunk != NULL) {
            return chunk;
        }
    }
    return NULL;
}

word_t *LargeAllocator_tryAlloc(LargeAllocator *allocator,
                                size_t requestedBlockSize) {
    size_t actualBlockSize =
        MathUtils_RoundToNextMultiple(requestedBlockSize, MIN_BLOCK_SIZE);

    Chunk *chunk = NULL;
    if (actualBlockSize < BLOCK_TOTAL_SIZE) {
        // only need to look in free lists for chunks smaller than a block
        chunk = LargeAllocator_getChunkForSize(allocator, actualBlockSize);
    }

    if (chunk == NULL) {
        uint32_t superblockSize = (uint32_t)MathUtils_DivAndRoundUp(
            actualBlockSize, BLOCK_TOTAL_SIZE);
        BlockMeta *superblock = BlockAllocator_GetFreeSuperblock(
            allocator->blockAllocator, superblockSize);
        if (superblock != NULL) {
            chunk = (Chunk *)BlockMeta_GetBlockStart(
                allocator->blockMetaStart, allocator->heapStart, superblock);
            chunk->nothing = NULL;
            chunk->size = superblockSize * BLOCK_TOTAL_SIZE;
        }
    }

    if (chunk == NULL) {
        return NULL;
    }

    size_t chunkSize = chunk->size;
    assert(chunkSize >= MIN_BLOCK_SIZE);

    if (chunkSize - MIN_BLOCK_SIZE >= actualBlockSize) {
        Chunk *remainingChunk =
            LargeAllocator_chunkAddOffset(chunk, actualBlockSize);

        size_t remainingChunkSize = chunkSize - actualBlockSize;
        LargeAllocator_addChunk(allocator, remainingChunk, remainingChunkSize);
    }

    ObjectMeta *objectMeta = Bytemap_Get(allocator->bytemap, (word_t *)chunk);
#ifdef DEBUG_ASSERT
    ObjectMeta_AssertIsValidAllocation(objectMeta, actualBlockSize);
#endif
    ObjectMeta_SetAllocated(objectMeta);
    word_t *object = (word_t *)chunk;
    memset(object, 0, actualBlockSize);
    return object;
}

INLINE
word_t *LargeAllocator_lazySweep(Heap *heap, uint32_t size) {
    word_t *object = NULL;
#ifdef DEBUG_PRINT
    uint32_t increment =
        (uint32_t)MathUtils_DivAndRoundUp(size, BLOCK_TOTAL_SIZE);
    printf("Sweeper_LazySweepLarge (%" PRIu32 ") => %" PRIu32 "\n", size,
           increment);
    fflush(stdout);
#endif
    // lazy sweep will happen
    Stats_DefineOrNothing(stats, heap->stats);
    Stats_RecordTime(stats, start_ns);
    // mark as active
    heap->lazySweep.lastActivity = BlockRange_Pack(1, heap->sweep.cursor);
    while (object == NULL && heap->sweep.cursor < heap->sweep.limit) {
        Sweeper_Sweep(heap, heap->stats, &heap->lazySweep.cursorDone,
                      LAZY_SWEEP_MIN_BATCH);
        object = LargeAllocator_tryAlloc(&largeAllocator, size);
    }
    // mark as inactive
    heap->lazySweep.lastActivity = BlockRange_Pack(0, heap->sweep.cursor);
    while (object == NULL && !Sweeper_IsSweepDone(heap)) {
        object = LargeAllocator_tryAlloc(&largeAllocator, size);
        if (object == NULL) {
            sched_yield();
        }
    }
    Stats_RecordTime(stats, end_ns);
    Stats_RecordEvent(stats, event_sweep, start_ns, end_ns);
    return object;
}

word_t *LargeAllocator_Alloc(Heap *heap, uint32_t size) {

    assert(size % ALLOCATION_ALIGNMENT == 0);
    assert(size >= MIN_BLOCK_SIZE);

    word_t *object = LargeAllocator_tryAlloc(&largeAllocator, size);
    if (object != NULL) {
done:
        assert(object != NULL);
        assert(Heap_IsWordInHeap(heap, (word_t *)object));
        return object;
}

    if (!Sweeper_IsSweepDone(heap)) {
        object = LargeAllocator_lazySweep(heap, size);
        if (object != NULL)
            goto done;
    }

    Heap_Collect(heap);

    object = LargeAllocator_tryAlloc(&largeAllocator, size);
    if (object != NULL)
        goto done;

    if (!Sweeper_IsSweepDone(heap)) {
        object = LargeAllocator_lazySweep(heap, size);
        if (object != NULL)
            goto done;
    }

    size_t increment = MathUtils_DivAndRoundUp(size, BLOCK_TOTAL_SIZE);
    uint32_t pow2increment = 1U << MathUtils_Log2Ceil(increment);
    Heap_Grow(heap, pow2increment);

    object = LargeAllocator_tryAlloc(&largeAllocator, size);

    goto done;
}

uint32_t LargeAllocator_Sweep(LargeAllocator *allocator, BlockMeta *blockMeta,
                              word_t *blockStart, BlockMeta *batchLimit) {
    // Objects that are larger than a block
    // are always allocated at the begining the smallest possible superblock.
    // Any gaps at the end can be filled with large objects, that are smaller
    // than a block. This means that objects can ONLY start at the begining at
    // the first block or anywhere at the last block, except the begining.
    // Therefore we only need to look at a few locations.
    uint32_t superblockSize = BlockMeta_SuperblockSize(blockMeta);
    word_t *blockEnd = blockStart + WORDS_IN_BLOCK * superblockSize;

#ifdef DEBUG_ASSERT
    for (BlockMeta *block = blockMeta; block < blockMeta + superblockSize;
         block++) {
        assert(block->debugFlag == dbg_must_sweep);
    }
#endif

    ObjectMeta *firstObject = Bytemap_Get(allocator->bytemap, blockStart);
    assert(!ObjectMeta_IsFree(firstObject));
    BlockMeta *lastBlock = blockMeta + superblockSize - 1;
    int freeCount = 0;
    if (superblockSize > 1 && !ObjectMeta_IsMarked(firstObject)) {
        // release free superblock starting from the first object
        freeCount = superblockSize - 1;
#ifdef DEBUG_ASSERT
        for (BlockMeta *block = blockMeta; block < blockMeta + freeCount;
             block++) {
            block->debugFlag = dbg_free;
        }
#endif
    } else {
#ifdef DEBUG_ASSERT
        for (BlockMeta *block = blockMeta;
             block < blockMeta + superblockSize - 1; block++) {
            block->debugFlag = dbg_not_free;
        }
#endif
    }

    word_t *lastBlockStart = blockEnd - WORDS_IN_BLOCK;
    word_t *chunkStart = NULL;

    // the tail end of the first object
    if (!ObjectMeta_IsMarked(firstObject)) {
        chunkStart = lastBlockStart;
    }
    ObjectMeta_Sweep(firstObject);

    word_t *current = lastBlockStart + (MIN_BLOCK_SIZE / WORD_SIZE);
    ObjectMeta *currentMeta = Bytemap_Get(allocator->bytemap, current);
    while (current < blockEnd) {
        if (chunkStart == NULL) {
            // if (ObjectMeta_IsAllocated(currentMeta)||
            // ObjectMeta_IsPlaceholder(currentMeta)) {
            if (*currentMeta & 0x3) {
                chunkStart = current;
            }
        } else {
            if (ObjectMeta_IsMarked(currentMeta)) {
                size_t currentSize = (current - chunkStart) * WORD_SIZE;
                LargeAllocator_addChunk(allocator, (Chunk *)chunkStart,
                                        currentSize);
                chunkStart = NULL;
            }
        }
        ObjectMeta_Sweep(currentMeta);

        current += MIN_BLOCK_SIZE / WORD_SIZE;
        currentMeta += MIN_BLOCK_SIZE / ALLOCATION_ALIGNMENT;
    }
    if (chunkStart == lastBlockStart) {
        // free chunk covers the entire last block, released it
        freeCount += 1;
#ifdef DEBUG_ASSERT
        lastBlock->debugFlag = dbg_free;
#endif
    } else {
#ifdef DEBUG_ASSERT
        lastBlock->debugFlag = dbg_not_free;
#endif
        if (ObjectMeta_IsFree(firstObject)) {
            // the first object was free
            // the end of first object becomes a placeholder
            ObjectMeta_SetPlaceholder(
                Bytemap_Get(allocator->bytemap, lastBlockStart));
        }
        if (freeCount > 0) {
            // the last block is its own superblock
            if (lastBlock < batchLimit) {
                // The block is within current batch, just create the superblock
                // yourself
                BlockMeta_SetFlagAndSuperblockSize(lastBlock, block_superblock_start, 1);
            } else {
                // If we cross the current batch, then it is not to mark a
                // block_superblock_tail to block_superblock_start. The other
                // sweeper threads could be in the middle of skipping
                // block_superblock_tail s. Then creating the superblock will
                // be done by Heap_lazyCoalesce
                BlockMeta_SetFlag(lastBlock, block_superblock_start_me);
            }
        }
        // handle the last chunk if any
        if (chunkStart != NULL) {
            size_t currentSize = (current - chunkStart) * WORD_SIZE;
            LargeAllocator_addChunk(allocator, (Chunk *)chunkStart,
                                    currentSize);
        }
    }
#ifdef DEBUG_PRINT
    printf("LargeAllocator_Sweep %p %" PRIu32 " => FREE %" PRIu32 "/ %" PRIu32
           "\n",
           blockMeta,
           BlockMeta_GetBlockIndex(allocator->blockMetaStart, blockMeta),
           freeCount, superblockSize);
    fflush(stdout);
#endif
    return freeCount;
}
