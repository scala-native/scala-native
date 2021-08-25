#ifndef IMMIX_BLOCKALLOCATOR_H
#define IMMIX_BLOCKALLOCATOR_H

#include "datastructures/BlockList.h"
#include "datastructures/BlockRange.h"
#include "Constants.h"
#include <stddef.h>
#include <stdatomic.h>
#include <stdbool.h>

#define SUPERBLOCK_LIST_SIZE (BLOCK_COUNT_BITS + 1)

typedef struct {
    // no need to synchronize smallestSuperblock,
    // it is only accessed from the mutator thread
    struct {
        BlockMeta *cursor;
        BlockMeta *limit;
    } smallestSuperblock;
    atomic_uint_fast32_t freeBlockCount;
    BlockRange coalescingSuperblock;
    word_t *blockMetaStart;
    atomic_bool concurrent;
    BlockList freeSuperblocks[SUPERBLOCK_LIST_SIZE];
    atomic_uintptr_t reservedSuperblock;
} BlockAllocator;

void BlockAllocator_Init(BlockAllocator *blockAllocator, word_t *blockMetaStart,
                         uint32_t blockCount);
BlockMeta *BlockAllocator_GetFreeBlock(BlockAllocator *blockAllocator);
BlockMeta *BlockAllocator_GetFreeSuperblock(BlockAllocator *blockAllocator,
                                            uint32_t size);
void BlockAllocator_AddFreeBlocks(BlockAllocator *blockAllocator,
                                  BlockMeta *block, uint32_t count);
void BlockAllocator_AddFreeSuperblock(BlockAllocator *blockAllocator,
                                      BlockMeta *block, uint32_t count);
void BlockAllocator_AddFreeSuperblockLocal(BlockAllocator *blockAllocator,
                                           LocalBlockList *localBlockListStart,
                                           BlockMeta *superblock,
                                           uint32_t count);
void BlockAllocator_FinishCoalescing(BlockAllocator *blockAllocator);
void BlockAllocator_ReserveBlocks(BlockAllocator *blockAllocator);
void BlockAllocator_UseReserve(BlockAllocator *blockAllocator);
void BlockAllocator_Clear(BlockAllocator *blockAllocator);

#endif // IMMIX_BLOCKALLOCATOR_H