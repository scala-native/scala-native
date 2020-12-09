#ifndef IMMIX_LARGEALLOCATOR_H
#define IMMIX_LARGEALLOCATOR_H

#include "datastructures/Bytemap.h"
#include "GCTypes.h"
#include "Constants.h"
#include "headers/ObjectHeader.h"
#include "BlockAllocator.h"
#include "Heap.h"

#define FREE_LIST_COUNT                                                        \
    ((1UL << (BLOCK_SIZE_BITS - LARGE_OBJECT_MIN_SIZE_BITS)) - 1)

typedef struct {
    atomic_uintptr_t head;
} FreeList;

typedef struct {
    FreeList freeLists[FREE_LIST_COUNT];
    word_t *heapStart;
    word_t *blockMetaStart;
    Bytemap *bytemap;
    BlockAllocator *blockAllocator;
} LargeAllocator;

void LargeAllocator_Init(LargeAllocator *allocator,
                         BlockAllocator *blockAllocator, Bytemap *bytemap,
                         word_t *blockMetaStart, word_t *heapStart);
word_t *LargeAllocator_Alloc(Heap *heap, uint32_t objectSize);
void LargeAllocator_Clear(LargeAllocator *allocator);
void LargeAllocator_AddChunk(LargeAllocator *allocator, Chunk *chunk,
                             size_t total_block_size);
#endif // IMMIX_LARGEALLOCATOR_H
