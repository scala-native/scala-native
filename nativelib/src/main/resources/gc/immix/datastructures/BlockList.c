#include <stddef.h>
#include <stdio.h>
#include <pthread.h>
#include "BlockList.h"
#include "../Log.h"
#include "../headers/BlockHeader.h"

int32_t _getBlockIndex(word_t *heapStart, BlockHeader *blockHeader) {
    return (uint32_t)((word_t *)blockHeader - heapStart) / WORDS_IN_BLOCK;
}

BlockHeader *_getBlockFromIndex(word_t *heapStart, int32_t index) {
    return (BlockHeader *)(heapStart + (index * WORDS_IN_BLOCK));
}

BlockHeader *_getNextBlock(word_t *heapStart, BlockHeader *header) {
    int32_t nextBlockId = header->header.nextBlock;
    if (nextBlockId == LAST_BLOCK) {
        return NULL;
    } else if (nextBlockId == 0) {
        nextBlockId = _getBlockIndex(heapStart, header) + 1;
    }
    return _getBlockFromIndex(heapStart, nextBlockId);
}

void BlockList_Init(BlockList *blockList, word_t *heapStart) {
    blockList->heapStart = heapStart;
    blockList->first = NULL;
    blockList->last = NULL;
    pthread_mutex_init(&blockList->mutex, NULL);
}

inline bool BlockList_IsEmpty(BlockList *blockList) {
    pthread_mutex_lock(&blockList->mutex);
    bool result = blockList->first == NULL;
    pthread_mutex_unlock(&blockList->mutex);
    return result;
}

BlockHeader *BlockList_PopFirstBlock(BlockList *blockList) {
    pthread_mutex_lock(&blockList->mutex);
    BlockHeader *block;
    if (blockList->first == NULL) {
        block = NULL;
    } else {
        block = blockList->first;
        if (block == blockList->last) {
            blockList->first = NULL;
        }
        blockList->first = _getNextBlock(blockList->heapStart, block);
    }
    pthread_mutex_unlock(&blockList->mutex);
    return block;
}

void BlockList_AddLast(BlockList *blockList, BlockHeader *blockHeader) {
    pthread_mutex_lock(&blockList->mutex);
    if (blockList->first == NULL) {
        blockList->first = blockHeader;
    } else {
        blockList->last->header.nextBlock =
            _getBlockIndex(blockList->heapStart, blockHeader);
    }
    blockList->last = blockHeader;
    blockHeader->header.nextBlock = LAST_BLOCK;
    pthread_mutex_unlock(&blockList->mutex);
}

void BlockList_AddBlocksLast(BlockList *blockList, BlockHeader *first,
                             BlockHeader *last) {
    pthread_mutex_lock(&blockList->mutex);
    if (blockList->first == NULL) {
        blockList->first = first;
    } else {
        blockList->last->header.nextBlock =
            _getBlockIndex(blockList->heapStart, first);
    }
    blockList->last = last;
    last->header.nextBlock = LAST_BLOCK;
    pthread_mutex_unlock(&blockList->mutex);
}

void BlockList_Clear(BlockList *blockList) {
    pthread_mutex_lock(&blockList->mutex);
    blockList->first = NULL;
    blockList->last = NULL;
    pthread_mutex_unlock(&blockList->mutex);
}

void BlockList_Print(BlockList *blockList) {
    pthread_mutex_lock(&blockList->mutex);
    printf("BlockList: ");
    BlockHeader *current = blockList->first;
    while (current != NULL) {
        printf("[%p %d] -> ", current, current->header.first);
        current = _getNextBlock(blockList->heapStart, current);
    }
    pthread_mutex_unlock(&blockList->mutex);
    printf("\n");
}
