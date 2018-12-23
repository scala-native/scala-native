#ifndef IMMIX_SWEEPRESULT_H
#define IMMIX_SWEEPRESULT_H

#include "metadata/BlockMeta.h"
#include "datastructures/BlockList.h"

typedef struct {
    LocalBlockList recycledBlocks;
//    LocalBlockList freeSuperblocks[SUPERBLOCK_LIST_SIZE];
} SweepResult;

static inline void SweepResult_clear(SweepResult *result) {
    LocalBlockList_Clear(&result->recycledBlocks);
}

static inline void SweepResult_Init(SweepResult *result) {
    SweepResult_clear(result);
}


#endif // IMMIX_SWEEPRESULT_H