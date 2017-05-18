#ifndef IMMIX_ALLOCATOR_H
#define IMMIX_ALLOCATOR_H

#include "GCTypes.h"
#include <stddef.h>
#include "datastructures/BlockList.h"
#include "stats/AllocatorStats.h"

typedef struct {
    word_t* heapStart;
    BlockList recycledBlocks;
    BlockList freeBlocks;
    BlockHeader* block;
    word_t* cursor;
    word_t* limit;
    BlockHeader* largeBlock;
    word_t* largeCursor;
    word_t* largeLimit;
#ifdef ALLOCATOR_STATS
    AllocatorStats* stats;
#endif
} Allocator;


Allocator* Allocator_create(word_t *, int);
bool Allocator_initCursors(Allocator *allocator);
word_t* Allocator_alloc(Allocator *allocator, size_t size);


#endif //IMMIX_ALLOCATOR_H
