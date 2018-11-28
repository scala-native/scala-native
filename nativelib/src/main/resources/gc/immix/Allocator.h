#ifndef IMMIX_ALLOCATOR_H
#define IMMIX_ALLOCATOR_H

#include "GCTypes.h"
#include <stddef.h>
#include "datastructures/BlockList.h"
#include "datastructures/Bytemap.h"
#include "metadata/BlockMeta.h"
#include "metadata/ObjectMeta.h"
#include "BlockAllocator.h"

typedef struct {
    word_t *blockMetaStart;
    Bytemap *bytemap;
    BlockAllocator *blockAllocator;
    word_t *heapStart;
    BlockList recycledBlocks;
    atomic_uint_fast32_t recycledBlockCount;
    BlockMeta *block;
    word_t *blockStart;
    word_t *cursor;
    word_t *limit;
    BlockMeta *largeBlock;
    word_t *largeBlockStart;
    word_t *largeCursor;
    word_t *largeLimit;
} Allocator;

void Allocator_Init(Allocator *allocator, BlockAllocator *blockAllocator,
                    Bytemap *bytemap, word_t *blockMetaStart,
                    word_t *heapStart);
bool Allocator_CanInitCursors(Allocator *allocator);
void Allocator_Clear(Allocator *allocator);
word_t *Allocator_Alloc(Allocator *allocator, size_t size);
uint32_t Allocator_Sweep(Allocator *allocator, BlockMeta *block, word_t *blockStart,
                    LineMeta *lineMetas);

#endif // IMMIX_ALLOCATOR_H
