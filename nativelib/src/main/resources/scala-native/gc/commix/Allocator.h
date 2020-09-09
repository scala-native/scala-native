#ifndef IMMIX_ALLOCATOR_H
#define IMMIX_ALLOCATOR_H

#include "GCTypes.h"
#include <stddef.h>
#include "datastructures/BlockList.h"
#include "datastructures/Bytemap.h"
#include "metadata/BlockMeta.h"
#include "metadata/ObjectMeta.h"
#include "BlockAllocator.h"
#include "Heap.h"

typedef struct {
    // The fields here are sorted by how often it is accessed.
    // This should improve cache performance.
    // frequently used by Heap_AllocSmall
    // this is on the fast path
    Bytemap *bytemap;
    word_t *cursor;
    word_t *limit;

    // additional things used for Allocator_getNextLine
    BlockMeta *block;
    word_t *blockStart;
    // additional things used for Allocator_overflowAllocation
    word_t *largeCursor;
    word_t *largeLimit;
    // additional things used for Allocator_newBlock
    BlockList recycledBlocks;
    word_t *blockMetaStart;
    word_t *heapStart;
    BlockAllocator *blockAllocator;
    // additional things used for
    BlockMeta *largeBlock;
    word_t *largeBlockStart;
    // This gets concurrently updated by other threads, keep if it as far away
    // as possible from fast path.
    atomic_uint_fast32_t recycledBlockCount;
} Allocator;

void Allocator_Init(Allocator *allocator, BlockAllocator *blockAllocator,
                    Bytemap *bytemap, word_t *blockMetaStart,
                    word_t *heapStart);
bool Allocator_CanInitCursors(Allocator *allocator);
void Allocator_Clear(Allocator *allocator);
word_t *Allocator_Alloc(Heap *heap, uint32_t objectSize);

#endif // IMMIX_ALLOCATOR_H
