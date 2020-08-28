#ifndef IMMIX_BLOCLIST_H
#define IMMIX_BLOCLIST_H

#include "../metadata/BlockMeta.h"
#include <stdatomic.h>

typedef struct {
    atomic_uintptr_t head;
} BlockList;

typedef struct {
    BlockMeta *first;
    BlockMeta *last;
} LocalBlockList;

void BlockList_Init(BlockList *blockList);
void BlockList_Clear(BlockList *blockList);
BlockMeta *BlockList_Pop(BlockList *blockList, word_t *blockMetaStart);
BlockMeta *BlockList_PopOnlyThread(BlockList *blockList,
                                   word_t *blockMetaStart);
void BlockList_Push(BlockList *blockList, word_t *blockMetaStart,
                    BlockMeta *block);
void BlockList_PushAll(BlockList *blockList, word_t *blockMetaStart,
                       BlockMeta *first, BlockMeta *last);

void LocalBlockList_Clear(LocalBlockList *list);
void LocalBlockList_Push(LocalBlockList *list, word_t *blockMetaStart,
                         BlockMeta *block);

#endif // IMMIX_BLOCLIST_H
