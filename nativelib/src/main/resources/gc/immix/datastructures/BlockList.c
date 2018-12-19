#include <stddef.h>
#include <stdio.h>
#include "BlockList.h"
#include "../Log.h"
#include "../metadata/BlockMeta.h"

#define LAST_BLOCK -1

BlockMeta *BlockList_getNextBlock(word_t *blockMetaStart,
                                  BlockMeta *blockMeta) {
    int32_t nextBlockId = blockMeta->nextBlock;
    if (nextBlockId == LAST_BLOCK) {
        return NULL;
    } else {
        return BlockMeta_GetFromIndex(blockMetaStart, nextBlockId);
    }
}

void BlockList_Init(BlockList *blockList, word_t *blockMetaStart) {
    blockList->blockMetaStart = blockMetaStart;
    blockList->head = (word_t)NULL;
}

BlockMeta *BlockList_Pop(BlockList *blockList) {
    BlockMeta *block = (BlockMeta *)blockList->head;
    word_t newValue;
    do {
        // block will be replaced with actual value if
        // atomic_compare_exchange_strong fails
        if (block == NULL) {
            return NULL;
        }
        newValue =
            (word_t)BlockList_getNextBlock(blockList->blockMetaStart, block);
    } while (!atomic_compare_exchange_strong(&blockList->head, (word_t *)&block,
                                             newValue));
    return block;
}

BlockMeta *BlockList_Pop_OnlyThread(BlockList *blockList) {
    BlockMeta *block = (BlockMeta *)blockList->head;
    if (block == NULL) {
        return NULL;
    }
    blockList->head = (word_t)BlockList_getNextBlock(blockList->blockMetaStart, block);
    return block;
}

void BlockList_Push(BlockList *blockList, BlockMeta *blockMeta) {
    BlockMeta *block = (BlockMeta *)blockList->head;
    do {
        // block will be replaced with actual value if
        // atomic_compare_exchange_strong fails
        if (block == NULL) {
            blockMeta->nextBlock = LAST_BLOCK;
        } else {
            blockMeta->nextBlock =
                BlockMeta_GetBlockIndex(blockList->blockMetaStart, block);
        }
    } while (!atomic_compare_exchange_strong(&blockList->head, (word_t *)&block,
                                             (word_t)blockMeta));
}

void BlockList_Clear(BlockList *blockList) {
    blockList->head = (word_t)NULL;
}