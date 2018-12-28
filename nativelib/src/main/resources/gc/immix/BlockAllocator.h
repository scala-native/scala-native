#ifndef IMMIX_BLOCKALLOCATOR_H
#define IMMIX_BLOCKALLOCATOR_H

#include "datastructures/BlockList.h"
#include "Constants.h"
#include <stddef.h>

#define SUPERBLOCK_LIST_SIZE (BLOCK_COUNT_BITS + 1)

typedef struct {
    struct {
        BlockMeta *cursor;
        BlockMeta *limit;
    } smallestSuperblock;
    int minNonEmptyIndex;
    int maxNonEmptyIndex;
    uint32_t freeBlockCount;
    struct {
        BlockMeta *first;
        BlockMeta *limit;
    } coalescingSuperblock;
    BlockList freeSuperblocks[SUPERBLOCK_LIST_SIZE];
} BlockAllocator;

void BlockAllocator_Init(BlockAllocator *blockAllocator, word_t *blockMetaStart,
                         uint32_t blockCount);
BlockMeta *BlockAllocator_GetFreeBlock(BlockAllocator *blockAllocator);
BlockMeta *BlockAllocator_GetFreeSuperblock(BlockAllocator *blockAllocator,
                                            uint32_t size);
void BlockAllocator_AddFreeBlocks(BlockAllocator *blockAllocator,
                                  BlockMeta *block, uint32_t count);
void BlockAllocator_SweepDone(BlockAllocator *blockAllocator);
void BlockAllocator_Clear(BlockAllocator *blockAllocator);

#endif // IMMIX_BLOCKALLOCATOR_H