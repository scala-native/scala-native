#ifndef IMMIX_ALLOCATOR_H
#define IMMIX_ALLOCATOR_H

#include "GCTypes.h"
#include <stddef.h>
#include "datastructures/BlockList.h"
#include "datastructures/Bytemap.h"

typedef struct {
    word_t *blockMetaStart;
    Bytemap *bytemap;
    word_t *heapStart;
    uint64_t blockCount;
    BlockList recycledBlocks;
    uint64_t recycledBlockCount;
    BlockList freeBlocks;
    uint64_t freeBlockCount;
    BlockMeta *block;
    word_t *blockStart;
    word_t *cursor;
    word_t *limit;
    BlockMeta *largeBlock;
    word_t *largeBlockStart;
    word_t *largeCursor;
    word_t *largeLimit;
    size_t freeMemoryAfterCollection;
} Allocator;

void Allocator_Init(Allocator *allocator, Bytemap *bytemap,
                    word_t *blockMetaStart, word_t *heapStart,
                    uint32_t blockCount);
bool Allocator_CanInitCursors(Allocator *allocator);
void Allocator_InitCursors(Allocator *allocator);
word_t *Allocator_Alloc(Allocator *allocator, size_t size);

bool Allocator_ShouldGrow(Allocator *allocator);

#endif // IMMIX_ALLOCATOR_H
