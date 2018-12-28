#include <stddef.h>
#include <stdio.h>
#include "BlockList.h"
#include "../Log.h"
#include "../metadata/BlockMeta.h"

BlockMeta *BlockList_getNextBlock(word_t *blockMetaStart,
                                  BlockMeta *blockMeta) {
    int32_t nextBlockId = blockMeta->nextBlock;
    if (nextBlockId == LAST_BLOCK) {
        return NULL;
    } else if (nextBlockId == 0) {
        nextBlockId = BlockMeta_GetBlockIndex(blockMetaStart, blockMeta) + 1;
    }
    return BlockMeta_GetFromIndex(blockMetaStart, nextBlockId);
}

void BlockList_Init(BlockList *blockList, word_t *blockMetaStart) {
    blockList->blockMetaStart = blockMetaStart;
    blockList->first = NULL;
    blockList->last = NULL;
}

BlockMeta *BlockList_Poll(BlockList *blockList) {
    BlockMeta *block = blockList->first;
    if (block != NULL) {
        if (block == blockList->last) {
            blockList->first = NULL;
        }
        blockList->first =
            BlockList_getNextBlock(blockList->blockMetaStart, block);
    }
    return block;
}

void BlockList_AddLast(BlockList *blockList, BlockMeta *blockMeta) {
    if (blockList->first == NULL) {
        blockList->first = blockMeta;
    } else {
        blockList->last->nextBlock =
            BlockMeta_GetBlockIndex(blockList->blockMetaStart, blockMeta);
    }
    blockList->last = blockMeta;
    blockMeta->nextBlock = LAST_BLOCK;
}

void BlockList_AddBlocksLast(BlockList *blockList, BlockMeta *first,
                             BlockMeta *last) {
    if (blockList->first == NULL) {
        blockList->first = first;
    } else {
        blockList->last->nextBlock =
            BlockMeta_GetBlockIndex(blockList->blockMetaStart, first);
    }
    blockList->last = last;
    last->nextBlock = LAST_BLOCK;
}

void BlockList_Clear(BlockList *blockList) {
    blockList->first = NULL;
    blockList->last = NULL;
}