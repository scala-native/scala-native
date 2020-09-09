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

void BlockList_Init(BlockList *blockList) { blockList->head = (word_t)NULL; }

// This could suffer from the ABA problem. However, during a single phase each
// BlockMeta is removed no more than once. It would need to be swept before
// re-use.
BlockMeta *BlockList_Pop(BlockList *blockList, word_t *blockMetaStart) {
    BlockMeta *head = (BlockMeta *)blockList->head;
    word_t newValue;
    do {
        // block will be replaced with actual value if
        // atomic_compare_exchange_strong fails
        if (head == NULL) {
            return NULL;
        }
        newValue = (word_t)BlockList_getNextBlock(blockMetaStart, head);
    } while (!atomic_compare_exchange_strong(&blockList->head, (word_t *)&head,
                                             newValue));
    return head;
}

BlockMeta *BlockList_PopOnlyThread(BlockList *blockList,
                                   word_t *blockMetaStart) {
    BlockMeta *head = (BlockMeta *)blockList->head;
    if (head == NULL) {
        return NULL;
    }
    blockList->head = (word_t)BlockList_getNextBlock(blockMetaStart, head);
    return head;
}

void BlockList_Push(BlockList *blockList, word_t *blockMetaStart,
                    BlockMeta *blockMeta) {
    BlockMeta *head = (BlockMeta *)blockList->head;
    do {
        // head will be replaced with actual value if
        // atomic_compare_exchange_strong fails
        if (head == NULL) {
            blockMeta->nextBlock = LAST_BLOCK;
        } else {
            blockMeta->nextBlock =
                BlockMeta_GetBlockIndex(blockMetaStart, head);
        }
    } while (!atomic_compare_exchange_strong(&blockList->head, (word_t *)&head,
                                             (word_t)blockMeta));
}

void BlockList_PushAll(BlockList *blockList, word_t *blockMetaStart,
                       BlockMeta *first, BlockMeta *last) {
    BlockMeta *head = (BlockMeta *)blockList->head;
    do {
        // block will be replaced with actual value if
        // atomic_compare_exchange_strong fails
        if (head == NULL) {
            last->nextBlock = LAST_BLOCK;
        } else {
            last->nextBlock = BlockMeta_GetBlockIndex(blockMetaStart, head);
        }
    } while (!atomic_compare_exchange_strong(&blockList->head, (word_t *)&head,
                                             (word_t)first));
}

void BlockList_Clear(BlockList *blockList) { blockList->head = (word_t)NULL; }

void LocalBlockList_Push(LocalBlockList *list, word_t *blockMetaStart,
                         BlockMeta *block) {
    BlockMeta *head = list->first;
    if (head == NULL) {
        block->nextBlock = LAST_BLOCK;
        list->first = list->last = block;
    } else {
        block->nextBlock = BlockMeta_GetBlockIndex(blockMetaStart, head);
        list->first = block;
    }
}

void LocalBlockList_Clear(LocalBlockList *list) {
    list->first = list->last = NULL;
}