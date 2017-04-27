#include <stdlib.h>
#include <sys/mman.h>
#include <stdio.h>
#include "Heap.h"
#include "Block.h"
#include "Log.h"
#include "Allocator.h"
#include "stats/AllocatorStats.h"

#define MAX_SIZE 64*1024*1024*1024L
// Allow read and write
#define HEAP_MEM_PROT (PROT_READ | PROT_WRITE)
// Map private anonymous memory, and prevent from reserving swap
#define HEAP_MEM_FLAGS (MAP_NORESERVE | MAP_PRIVATE | MAP_ANONYMOUS)
// Map anonymous memory (not a file)
#define HEAP_MEM_FD -1
#define HEAP_MEM_FD_OFFSET 0


word_t* mapAndAlign(int alignmentMask) {
    word_t* heapStart = mmap(NULL, MAX_SIZE, HEAP_MEM_PROT, HEAP_MEM_FLAGS, HEAP_MEM_FD, HEAP_MEM_FD_OFFSET);

    // Heap start not aligned on
    if(((word_t)heapStart & alignmentMask) != (word_t)heapStart) {
        word_t* previousBlock = (word_t*)((word_t)heapStart & BLOCK_SIZE_IN_BYTES_INVERSE_MASK);
        heapStart = previousBlock + WORDS_IN_BLOCK;
    }
    return heapStart;
}

Heap* heap_create(size_t initialSize) {
    assert(initialSize >= 2*BLOCK_TOTAL_SIZE);

    Heap* heap = malloc(sizeof(Heap));

    word_t* smallHeapStart = mapAndAlign(BLOCK_SIZE_IN_BYTES_INVERSE_MASK);


    heap->smallHeapSize = initialSize;
    heap->heapStart = smallHeapStart;
    heap->heapEnd = smallHeapStart + initialSize / sizeof(word_t);
    heap->allocator = allocator_create(smallHeapStart, initialSize / BLOCK_TOTAL_SIZE);


    word_t* largeHeapStart = mapAndAlign(LARGE_BLOCK_MASK);
    heap->largeAllocator = largeAllocator_create(largeHeapStart, initialSize);
    heap->largeHeapStart = largeHeapStart;
    heap->largeHeapEnd = (word_t*)((ubyte_t*)largeHeapStart + initialSize);


    return heap;
}

ObjectHeader* heap_alloc(Heap* heap, uint32_t objectSize) {
    uint32_t size = objectSize + sizeof(word_t); // Add header

    if(size >= LARGE_BLOCK_SIZE) {
        ObjectHeader* object = largeAllocator_getBlock(heap->largeAllocator, size);
        if(object == NULL) {
            return NULL;
        }
        object_setObjectType(object, object_large);
        object_setSize(object, size);
        return object;
    } else {
        ObjectHeader* block = (ObjectHeader*) allocator_alloc(heap->allocator, size);
        if(block != NULL) {
            object_setObjectType(block, object_standard);
            object_setSize(block, size);

#ifdef ALLOCATOR_STATS
            heap->allocator->stats->bytesAllocated += objectSize;
            heap->allocator->stats->totalBytesAllocated += objectSize;
            heap->allocator->stats->totalAllocatedObjectCount++;
#endif

        }
        return block;
    }
}

bool heap_recycle(Heap* heap) {
    blockList_clear(&heap->allocator->recycledBlocks);
    blockList_clear(&heap->allocator->freeBlocks);

#ifdef ALLOCATOR_STATS
    allocatorStats_resetBlockDistribution(heap->allocator->stats);
#endif

    word_t* current = heap->heapStart;
    while(current != heap->heapEnd) {
        BlockHeader* blockHeader = (BlockHeader*) current;
        block_recycle(heap->allocator, blockHeader);
        //block_print(blockHeader);
        current += WORDS_IN_BLOCK;
    }
    largeAllocator_sweep(heap->largeAllocator);

#ifdef ALLOCATOR_STATS
    allocatorStats_print(heap->allocator->stats);
    heap->allocator->stats->liveObjectCount = 0;
    heap->allocator->stats->bytesAllocated = 0;
#endif

    return allocator_initCursors(heap->allocator);
}


void heap_grow(Heap* heap, size_t size) {}