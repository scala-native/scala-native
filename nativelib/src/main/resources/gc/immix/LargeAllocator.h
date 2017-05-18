#ifndef IMMIX_LARGEALLOCATOR_H
#define IMMIX_LARGEALLOCATOR_H

#include "datastructures/Bitmap.h"
#include "GCTypes.h"
#include "Constants.h"
#include "headers/ObjectHeader.h"

#define FREE_LIST_COUNT (LARGE_OBJECT_MAX_SIZE_BITS - LARGE_OBJECT_MIN_SIZE_BITS + 1)


typedef struct Chunk Chunk;

struct Chunk {
    ObjectHeaderLine header;
    Chunk* next;
};

typedef struct {
    Chunk* first;
    Chunk* last;
} FreeList;


typedef struct {
    word_t* offset;
    size_t size;
    FreeList freeLists[FREE_LIST_COUNT];
    Bitmap* bitmap;
} LargeAllocator;


LargeAllocator* LargeAllocator_create(word_t* offset, size_t largeHeapSize);
void LargeAllocator_addChunk(LargeAllocator *allocator, Chunk *chunk, size_t total_block_size);
ObjectHeader* LargeAllocator_getBlock(LargeAllocator *allocator, size_t requestedBlockSize);
void LargeAllocator_sweep(LargeAllocator *allocator);
void LargeAllocator_print(LargeAllocator *alloc);

#endif //IMMIX_LARGEALLOCATOR_H
