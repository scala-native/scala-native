#ifndef IMMIX_LARGEALLOCATOR_H
#define IMMIX_LARGEALLOCATOR_H

#include "datastructures/Bitmap.h"
#include "GCTypes.h"
#include "Constants.h"
#include "headers/ObjectHeader.h"

#define FREE_LIST_COUNT                                                        \
    (LARGE_OBJECT_MAX_SIZE_BITS - LARGE_OBJECT_MIN_SIZE_BITS + 1)

typedef struct Chunk Chunk;

struct Chunk {
    ObjectHeader header;
    Chunk *next;
};

typedef struct {
    Chunk *first;
    Chunk *last;
} FreeList;

typedef struct {
    word_t *offset;
    size_t size;
    FreeList freeLists[FREE_LIST_COUNT];
    Bitmap *bitmap;
} LargeAllocator;

LargeAllocator *LargeAllocator_Create(word_t *offset, size_t largeHeapSize);
void LargeAllocator_AddChunk(LargeAllocator *allocator, Chunk *chunk,
                             size_t total_block_size);
Object *LargeAllocator_GetBlock(LargeAllocator *allocator,
                                size_t requestedBlockSize);
void LargeAllocator_Sweep(LargeAllocator *allocator);
void LargeAllocator_Print(LargeAllocator *alloc);

#endif // IMMIX_LARGEALLOCATOR_H
