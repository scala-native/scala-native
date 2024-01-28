#if defined(SCALANATIVE_GC_IMMIX)

#include "BlockAllocator.h"
#include "immix_commix/Log.h"
#include "immix_commix/utils/MathUtils.h"
#include <stdio.h>
#include "shared/ThreadUtil.h"
#include <stdatomic.h>

void BlockAllocator_addFreeBlocksInternal(BlockAllocator *blockAllocator,
                                          BlockMeta *block, uint32_t count);

void BlockAllocator_Init(BlockAllocator *blockAllocator, word_t *blockMetaStart,
                         uint32_t blockCount) {
    for (int i = 0; i < SUPERBLOCK_LIST_SIZE; i++) {
        BlockList_Init(&blockAllocator->freeSuperblocks[i], blockMetaStart);
    }
    BlockAllocator_Clear(blockAllocator);

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

inline static BlockMeta *
BlockAllocator_pollSuperblock(BlockAllocator *blockAllocator, int first) {
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    atomic_thread_fence(memory_order_acquire);
#endif
    int maxNonEmptyIndex = blockAllocator->maxNonEmptyIndex;
    for (int i = first; i <= maxNonEmptyIndex; i++) {
        BlockMeta *superblock =
            BlockList_Poll(&blockAllocator->freeSuperblocks[i]);
        if (superblock != NULL) {
            assert(BlockMeta_SuperblockSize(superblock) > 0);
            return superblock;
        } else {
            blockAllocator->minNonEmptyIndex = i + 1;
        }
    }
    return NULL;
}

NOINLINE BlockMeta *
BlockAllocator_getFreeBlockSlow(BlockAllocator *blockAllocator) {
    BlockMeta *superblock = BlockAllocator_pollSuperblock(
        blockAllocator, blockAllocator->minNonEmptyIndex);
    if (superblock != NULL) {
        blockAllocator->smallestSuperblock.cursor = superblock + 1;
        blockAllocator->smallestSuperblock.limit =
            superblock + BlockMeta_SuperblockSize(superblock);
        // it might be safe to remove this
        BlockMeta_SetSuperblockSize(superblock, 0);
        BlockMeta_SetFlag(superblock, block_simple);
        return superblock;
    } else {
        return NULL;
    }
}

INLINE BlockMeta *BlockAllocator_GetFreeBlock(BlockAllocator *blockAllocator) {
    BlockMeta *block;
    BlockAllocator_Acquire(blockAllocator);
    if (blockAllocator->smallestSuperblock.cursor >=
        blockAllocator->smallestSuperblock.limit) {
        block = BlockAllocator_getFreeBlockSlow(blockAllocator);
    } else {
        block = blockAllocator->smallestSuperblock.cursor;
        BlockMeta_SetFlag(block, block_simple);
        blockAllocator->smallestSuperblock.cursor++;
    }
    BlockAllocator_Release(blockAllocator);

    // not decrementing freeBlockCount, because it is only used after sweep
    return block;
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
        int target = MathUtils_Log2Ceil((size_t)size);
        int minNonEmptyIndex = blockAllocator->minNonEmptyIndex;
        int first = (minNonEmptyIndex > target) ? minNonEmptyIndex : target;
        superblock = BlockAllocator_pollSuperblock(blockAllocator, first);
        if (superblock == NULL) {
            BlockAllocator_Release(blockAllocator);
            return NULL;
        }
        if (BlockMeta_SuperblockSize(superblock) > size) {
            BlockMeta *leftover = superblock + size;
            BlockAllocator_addFreeBlocksInternal(
                blockAllocator, leftover,
                BlockMeta_SuperblockSize(superblock) - size);
        }
    }
    BlockAllocator_Release(blockAllocator);

    BlockMeta_SetFlag(superblock, block_superblock_start);
    BlockMeta_SetSuperblockSize(superblock, size);
    BlockMeta *limit = superblock + size;
    for (BlockMeta *current = superblock + 1; current < limit; current++) {
        BlockMeta_SetFlag(current, block_superblock_middle);
    }
    // not decrementing freeBlockCount, because it is only used after sweep
    return superblock;
}

static inline void
BlockAllocator_addFreeBlocksInternal0(BlockAllocator *blockAllocator,
                                      BlockMeta *superblock, uint32_t count) {
    int i = BlockAllocator_sizeToLinkedListIndex(count);
    if (i < blockAllocator->minNonEmptyIndex) {
        blockAllocator->minNonEmptyIndex = i;
    }
    if (i > blockAllocator->maxNonEmptyIndex) {
        blockAllocator->maxNonEmptyIndex = i;
    }
    BlockMeta *limit = superblock + count;
    for (BlockMeta *current = superblock; current < limit; current++) {
        BlockMeta_SetFlag(current, block_free);
    }
    BlockMeta_SetSuperblockSize(superblock, count);
    BlockList_AddLast(&blockAllocator->freeSuperblocks[i], superblock);
}

void BlockAllocator_addFreeBlocksInternal(BlockAllocator *blockAllocator,
                                          BlockMeta *superblock,
                                          uint32_t count) {
    uint32_t remaining_count = count;
    uint32_t powerOf2 = 1;
    BlockMeta *current = superblock;
    // splits the superblock into smaller superblocks that are a powers of 2
    while (remaining_count > 0) {
        if ((powerOf2 & remaining_count) > 0) {
            BlockAllocator_addFreeBlocksInternal0(blockAllocator, current,
                                                  powerOf2);
            remaining_count -= powerOf2;
            current += powerOf2;
        }
        powerOf2 <<= 1;
    }
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
