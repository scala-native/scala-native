#ifndef IMMIX_BLOCLIST_H
#define IMMIX_BLOCLIST_H

#include "../headers/BlockHeader.h"
#include <pthread.h>

#define LAST_BLOCK -1

typedef struct {
    word_t *heapStart;
    BlockHeader *first;
    BlockHeader *last;
    pthread_mutex_t mutex;
} BlockList;

void BlockList_Init(BlockList *blockList, word_t *offset);
void BlockList_Clear(BlockList *blockList);
bool BlockList_IsEmpty(BlockList *blockList);
BlockHeader *BlockList_PopFirstBlock(BlockList *blockList);
void BlockList_AddLast(BlockList *blockList, BlockHeader *block);
void BlockList_AddBlocksLast(BlockList *blockList, BlockHeader *first,
                             BlockHeader *last);
void BlockList_Print(BlockList *blockList);

#endif // IMMIX_BLOCLIST_H
