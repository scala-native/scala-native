#ifndef IMMIX_BLOCLIST_H
#define IMMIX_BLOCLIST_H

#include "../headers/BlockHeader.h"

#define LAST_BLOCK -1


typedef struct {
    word_t* heapStart;
    BlockHeader* first;
    BlockHeader* last;
} BlockList;

void BlockList_init(BlockList *blockList, word_t *offset);
void BlockList_clear(BlockList *blockList);
bool BlockList_isEmpty(BlockList *blockList);
BlockHeader* BlockList_removeFirstBlock(BlockList *blockList);
void BlockList_addLast(BlockList *blockList, BlockHeader *block);
void BlockList_print(BlockList *blockList);

#endif //IMMIX_BLOCLIST_H
