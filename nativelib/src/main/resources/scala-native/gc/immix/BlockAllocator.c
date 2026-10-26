#if defined(SCALANATIVE_GC_IMMIX)

#include "BlockAllocator.h"
#include "State.h"
#include "immix_commix/utils/MathUtils.h"
#include <stdio.h>
#include "shared/ThreadUtil.h"
#include <stdatomic.h>
#include <assert.h>

void BlockAllocator_addFreeBlocksInternal(BlockAllocator *blockAllocator,
                                          BlockMeta *block, uint32_t count);

void BlockAllocator_Init(BlockAllocator *blockAllocator, word_t *blockMetaStart,
                         uint32_t blockCount) {
    for (int i = 0; i < SUPERBLOCK_LIST_SIZE; i++) {
        BlockList_Init(&blockAllocator->freeSuperblocks[i], blockMetaStart);
    }
    BlockAllocator_Clear(blockAllocator);

    blockAllocator->freeBlockCount = blockCount;
    blockAllocator->smallestSuperblock.cursor = (BlockMeta *)blockMetaStart;
    blockAllocator->smallestSuperblock.limit =
        (BlockMeta *)blockMetaStart + blockCount;
    mutex_init(&blockAllocator->allocationLock);
}

INLINE void BlockAllocator_Acquire(BlockAllocator *blockAllocator) {
    mutex_lock(&blockAllocator->allocationLock);
    atomic_thread_fence(memory_order_acquire);
}
INLINE void BlockAllocator_Release(BlockAllocator *blockAllocator) {
    atomic_thread_fence(memory_order_release);
    mutex_unlock(&blockAllocator->allocationLock);
}

inline static int BlockAllocator_sizeToLinkedListIndex(uint32_t size) {
    int result = MathUtils_Log2Floor((size_t)size);
    assert(result >= 0);
    assert(result < SUPERBLOCK_LIST_SIZE);
    return result;
}

static void BlockAllocator_addFreeExtent(BlockAllocator *blockAllocator,
                                         BlockMeta *superblock,
                                         uint32_t count) {
    assert(BlockMeta_IsFree(superblock));
    int i = BlockAllocator_sizeToLinkedListIndex(count);
    if (i < blockAllocator->minNonEmptyIndex)
        blockAllocator->minNonEmptyIndex = i;
    if (i > blockAllocator->maxNonEmptyIndex)
        blockAllocator->maxNonEmptyIndex = i;
    BlockMeta_SetSuperblockSize(superblock, count);
    BlockList_AddLast(&blockAllocator->freeSuperblocks[i], superblock);
}

static BlockMeta *BlockAllocator_pollList(BlockList *list, uint32_t size) {
    BlockMeta *previous = NULL;
    for (BlockMeta *block = list->first; block != NULL;) {
        BlockMeta *next = BlockList_getNextBlock(list->blockMetaStart, block);
        if (BlockMeta_SuperblockSize(block) >= size) {
            if (previous != NULL)
                BlockList_SetNext(list, previous, next);
            else
                list->first = next;
            if (list->last == block)
                list->last = previous;
            block->nextBlock = LAST_BLOCK;
            return block;
        }
        previous = block;
        block = next;
    }
    return NULL;
}

inline static BlockMeta *
BlockAllocator_pollSuperblock(BlockAllocator *blockAllocator, int first,
                              uint32_t size) {
    int maxNonEmptyIndex = blockAllocator->maxNonEmptyIndex;
    for (int i = first; i <= maxNonEmptyIndex; i++) {
        BlockList *list = &blockAllocator->freeSuperblocks[i];
        BlockMeta *superblock = BlockAllocator_pollList(list, size);
        if (superblock != NULL)
            return superblock;
        if (list->first == NULL && i == blockAllocator->minNonEmptyIndex)
            blockAllocator->minNonEmptyIndex = i + 1;
    }
    return NULL;
}

NOINLINE BlockMeta *
BlockAllocator_getFreeBlockSlow(BlockAllocator *blockAllocator) {
    BlockMeta *superblock = BlockAllocator_pollSuperblock(
        blockAllocator, blockAllocator->minNonEmptyIndex, 1);
    if (superblock != NULL) {
        blockAllocator->smallestSuperblock.cursor = superblock + 1;
        blockAllocator->smallestSuperblock.limit =
            superblock + BlockMeta_SuperblockSize(superblock);
        // it might be safe to remove this
        BlockMeta_SetSuperblockSize(superblock, 0);
        BlockMeta_SetFlag(superblock, block_simple);
    }
    return superblock;
}

static BlockMeta *BlockAllocator_getFreeBlock(BlockAllocator *blockAllocator,
                                              bool canUseReserve) {
    BlockMeta *block;
    BlockAllocator_Acquire(blockAllocator);
    if (!canUseReserve) {
        uint32_t reserve = heap.blockCount / 64;
        if (reserve < 2)
            reserve = 2;
        if (reserve > 128)
            reserve = 128;
        if (atomic_load_explicit(&blockAllocator->freeBlockCount,
                                 memory_order_relaxed) <= reserve) {
            BlockAllocator_Release(blockAllocator);
            return NULL;
        }
    }
    if (blockAllocator->smallestSuperblock.cursor >=
        blockAllocator->smallestSuperblock.limit) {
        block = BlockAllocator_getFreeBlockSlow(blockAllocator);
    } else {
        block = blockAllocator->smallestSuperblock.cursor;
        BlockMeta_SetFlag(block, block_simple);
        blockAllocator->smallestSuperblock.cursor++;
    }
    if (block != NULL)
        atomic_fetch_sub_explicit(&blockAllocator->freeBlockCount, 1,
                                  memory_order_relaxed);
    BlockAllocator_Release(blockAllocator);
    return block;
}

INLINE BlockMeta *BlockAllocator_GetFreeBlock(BlockAllocator *allocator) {
    return BlockAllocator_getFreeBlock(allocator, true);
}

BlockMeta *
BlockAllocator_GetFreeBlockForSmallAllocation(BlockAllocator *allocator,
                                              bool canUseReserve) {
    return BlockAllocator_getFreeBlock(allocator, canUseReserve);
}

BlockMeta *BlockAllocator_GetFreeSuperblock(BlockAllocator *blockAllocator,
                                            uint32_t size) {
    BlockAllocator_Acquire(blockAllocator);
    BlockMeta *superblock;
    if (blockAllocator->smallestSuperblock.limit -
            blockAllocator->smallestSuperblock.cursor >=
        size) {
        // first check the smallestSuperblock
        superblock = blockAllocator->smallestSuperblock.cursor;
        blockAllocator->smallestSuperblock.cursor += size;
    } else {
        // look in the freelists
        int target = MathUtils_Log2Floor((size_t)size);
        int minNonEmptyIndex = blockAllocator->minNonEmptyIndex;
        int first = (minNonEmptyIndex > target) ? minNonEmptyIndex : target;
        superblock = BlockAllocator_pollSuperblock(blockAllocator, first, size);
        if (superblock != NULL && BlockMeta_SuperblockSize(superblock) > size) {
            BlockMeta *leftover = superblock + size;
            // The remainder was already free. Only publish its new extent
            // header; clearing every remaining block here makes repeated
            // allocations from a large extent quadratic in its block count.
            BlockAllocator_addFreeExtent(blockAllocator, leftover,
                                         BlockMeta_SuperblockSize(superblock) -
                                             size);
        }
    }
    if (superblock != NULL) {
        BlockMeta_SetFlag(superblock, block_superblock_start);
        BlockMeta_SetSuperblockSize(superblock, size);
        BlockMeta *limit = superblock + size;
        for (BlockMeta *current = superblock + 1; current < limit; current++) {
            BlockMeta_SetFlag(current, block_superblock_middle);
        }
        atomic_fetch_sub_explicit(&blockAllocator->freeBlockCount, size,
                                  memory_order_relaxed);
    }
    BlockAllocator_Release(blockAllocator);
    return superblock;
}

void BlockAllocator_addFreeBlocksInternal(BlockAllocator *blockAllocator,
                                          BlockMeta *superblock,
                                          uint32_t count) {
    BlockMeta *limit = superblock + count;
    for (BlockMeta *current = superblock; current < limit; current++) {
        BlockMeta_SetFlag(current, block_free);
    }
    // Preserve contiguous extents instead of splitting them into powers of two.
    BlockAllocator_addFreeExtent(blockAllocator, superblock, count);
}

void BlockAllocator_AddFreeBlocks(BlockAllocator *blockAllocator,
                                  BlockMeta *block, uint32_t count) {
    // Executed during StopTheWorld, no need for synchronization
    assert(count > 0);
    if (blockAllocator->coalescingSuperblock.first == NULL) {
        blockAllocator->coalescingSuperblock.first = block;
        blockAllocator->coalescingSuperblock.limit = block + count;
    } else if (blockAllocator->coalescingSuperblock.limit == block) {
        blockAllocator->coalescingSuperblock.limit = block + count;
    } else {
        uint32_t size = (uint32_t)(blockAllocator->coalescingSuperblock.limit -
                                   blockAllocator->coalescingSuperblock.first);
        BlockAllocator_addFreeBlocksInternal(
            blockAllocator, blockAllocator->coalescingSuperblock.first, size);
        blockAllocator->coalescingSuperblock.first = block;
        blockAllocator->coalescingSuperblock.limit = block + count;
    }
    atomic_fetch_add_explicit(&blockAllocator->freeBlockCount, count,
                              memory_order_relaxed);
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    atomic_thread_fence(memory_order_release);
#endif
}

void BlockAllocator_SweepDone(BlockAllocator *blockAllocator) {
    // Executed during StopTheWorld, no need for synchronization
    if (blockAllocator->coalescingSuperblock.first != NULL) {
        uint32_t size = (uint32_t)(blockAllocator->coalescingSuperblock.limit -
                                   blockAllocator->coalescingSuperblock.first);
        BlockAllocator_addFreeBlocksInternal(
            blockAllocator, blockAllocator->coalescingSuperblock.first, size);
        blockAllocator->coalescingSuperblock.first = NULL;
        blockAllocator->coalescingSuperblock.limit = NULL;
    }
}

void BlockAllocator_Clear(BlockAllocator *blockAllocator) {
    // Executed during StopTheWorld, no need for synchronization
    for (int i = 0; i < SUPERBLOCK_LIST_SIZE; i++) {
        BlockList_Clear(&blockAllocator->freeSuperblocks[i]);
    }
    blockAllocator->freeBlockCount = 0;
    blockAllocator->smallestSuperblock.cursor = NULL;
    blockAllocator->smallestSuperblock.limit = NULL;
    blockAllocator->coalescingSuperblock.first = NULL;
    blockAllocator->coalescingSuperblock.limit = NULL;
    blockAllocator->minNonEmptyIndex = SUPERBLOCK_LIST_SIZE;
    blockAllocator->maxNonEmptyIndex = -1;
}

#endif
