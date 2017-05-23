#ifndef IMMIX_ALLOCATOR_H
#define IMMIX_ALLOCATOR_H

#include "GCTypes.h"
#include <stddef.h>
#include "datastructures/BlockList.h"

typedef struct {
    word_t *heapStart;
    uint64_t blockCount;
    BlockList recycledBlocks;
    uint64_t recycledBlockCount;
    BlockList freeBlocks;
    uint64_t freeBlockCount;
    BlockHeader *block;
    word_t *cursor;
    word_t *limit;
    BlockHeader *largeBlock;
    word_t *largeCursor;
    word_t *largeLimit;
} Allocator;

Allocator *Allocator_create(word_t *, int);
bool Allocator_canInitCursors(Allocator *allocator);
void Allocator_initCursors(Allocator *allocator);
word_t *Allocator_alloc(Allocator *allocator, size_t size);

bool Allocator_shouldGrow(Allocator *allocator);

#endif // IMMIX_ALLOCATOR_H
