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
void BlockList_AddBlocksLast(BlockList *blockList, BlockMeta *first,
                             BlockMeta *last);

#endif // IMMIX_BLOCLIST_H
