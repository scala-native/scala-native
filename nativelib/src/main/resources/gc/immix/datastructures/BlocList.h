#ifndef IMMIX_BLOCLIST_H
#define IMMIX_BLOCLIST_H

#include "../headers/BlockHeader.h"

#define LAST_BLOCK -1


typedef struct {
    word_t* heapStart;
    BlockHeader* first;
    BlockHeader* last;
} BlockList;

void blockList_init(BlockList*, word_t*);
void blockList_clear(BlockList*);
bool blockList_isEmpty(BlockList* blockList);
BlockHeader* blockList_removeFirstBlock(BlockList*);
void blockList_addLast(BlockList*, BlockHeader*);
void blockList_print(BlockList* blockList);

#endif //IMMIX_BLOCLIST_H
