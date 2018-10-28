#ifndef IMMIX_BLOCLIST_H
#define IMMIX_BLOCLIST_H

#include "../metadata/BlockMeta.h"

#define LAST_BLOCK -1

typedef struct {
    word_t *blockMetaStart;
    BlockMeta *first;
    BlockMeta *last;
} BlockList;

void BlockList_Init(BlockList *blockList, word_t *blockMetaStart);
void BlockList_Clear(BlockList *blockList);
BlockMeta *BlockList_Poll(BlockList *blockList);
void BlockList_AddLast(BlockList *blockList, BlockMeta *block);

#endif // IMMIX_BLOCLIST_H
