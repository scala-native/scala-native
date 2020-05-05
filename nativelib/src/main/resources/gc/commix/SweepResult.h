#ifndef IMMIX_SWEEPRESULT_H
#define IMMIX_SWEEPRESULT_H

#include "metadata/BlockMeta.h"
#include "datastructures/BlockList.h"

#define SUPERBLOCK_LOCAL_LIST_SIZE 4
#define SUPERBLOCK_LOCAL_LIST_MAX ((1 << SUPERBLOCK_LOCAL_LIST_SIZE) - 1)

typedef struct {
    LocalBlockList recycledBlocks;
    LocalBlockList freeSuperblocks[SUPERBLOCK_LOCAL_LIST_SIZE];
} SweepResult;

static inline void SweepResult_clear(SweepResult *result) {
    LocalBlockList_Clear(&result->recycledBlocks);
    for (int i = 0; i < SUPERBLOCK_LOCAL_LIST_SIZE; i++) {
        LocalBlockList_Clear(&result->freeSuperblocks[i]);
    }
}

static inline void SweepResult_Init(SweepResult *result) {
    SweepResult_clear(result);
}

#endif // IMMIX_SWEEPRESULT_H