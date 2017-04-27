#ifndef IMMIX_ALLOCATOR_H
#define IMMIX_ALLOCATOR_H

#include "GCTypes.h"
#include "datastructures/BlocList.h"
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


Allocator* allocator_create(word_t*, int);
bool allocator_initCursors(Allocator* allocator);
word_t* allocator_alloc(Allocator* allocator, size_t size);
void countBlockList(Allocator* allocator);

#endif //IMMIX_ALLOCATOR_H
