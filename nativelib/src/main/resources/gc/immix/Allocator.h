#ifndef IMMIX_ALLOCATOR_H
#define IMMIX_ALLOCATOR_H

#include "GCTypes.h"
#include <stddef.h>
#include <stdatomic.h>
#include "datastructures/BlockList.h"

typedef struct {
    word_t *heapStart;
    uint64_t blockCount;
    BlockList recycledBlocks;
    atomic_long recycledBlockCount;
    BlockList freeBlocks;
    atomic_long freeBlockCount;
    BlockHeader *block;
    word_t *cursor;
    word_t *limit;
    BlockHeader *largeBlock;
    word_t *largeCursor;
    word_t *largeLimit;
    atomic_size_t freeMemoryAfterCollection;
} Allocator;

void Allocator_Init(Allocator *allocator, word_t *, int);
bool Allocator_CanInitCursors(Allocator *allocator);
void Allocator_InitCursors(Allocator *allocator);
word_t *Allocator_Alloc(Allocator *allocator, size_t size);

bool Allocator_ShouldGrow(Allocator *allocator);

#endif // IMMIX_ALLOCATOR_H
