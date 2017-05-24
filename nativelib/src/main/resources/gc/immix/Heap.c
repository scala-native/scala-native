#include <stdlib.h>
#include <sys/mman.h>
#include <stdio.h>
#include "Heap.h"
#include "Block.h"
#include "Log.h"
#include "Allocator.h"
#include "Marker.h"
#include "State.h"
#include "utils/MathUtils.h"

#define MAX_SIZE 64 * 1024 * 1024 * 1024L
// Allow read and write
#define HEAP_MEM_PROT (PROT_READ | PROT_WRITE)
// Map private anonymous memory, and prevent from reserving swap
#define HEAP_MEM_FLAGS (MAP_NORESERVE | MAP_PRIVATE | MAP_ANONYMOUS)
// Map anonymous memory (not a file)
#define HEAP_MEM_FD -1
#define HEAP_MEM_FD_OFFSET 0

/**
 * Maps `MAX_SIZE` of memory and returns the first address aligned on `alignement` mask
 */
word_t *mapAndAlign(int alignmentMask) {
    word_t *heapStart = mmap(NULL, MAX_SIZE, HEAP_MEM_PROT, HEAP_MEM_FLAGS,
                             HEAP_MEM_FD, HEAP_MEM_FD_OFFSET);

    // Heap start not aligned on
    if (((word_t)heapStart & alignmentMask) != (word_t)heapStart) {
        word_t *previousBlock =
            (word_t *)((word_t)heapStart & BLOCK_SIZE_IN_BYTES_INVERSE_MASK);
        heapStart = previousBlock + WORDS_IN_BLOCK;
    }
    return heapStart;
}

/**
 * Allocates the heap struct and initializes it
 */
Heap *Heap_create(size_t initialSize) {
    assert(initialSize >= 2 * BLOCK_TOTAL_SIZE);
    assert(initialSize % BLOCK_TOTAL_SIZE == 0);

    Heap *heap = malloc(sizeof(Heap));

    word_t *smallHeapStart = mapAndAlign(BLOCK_SIZE_IN_BYTES_INVERSE_MASK);

    // Init heap for small objects
    heap->smallHeapSize = initialSize;
    heap->heapStart = smallHeapStart;
    heap->heapEnd = smallHeapStart + initialSize / WORD_SIZE;
    heap->allocator =
        Allocator_create(smallHeapStart, initialSize / BLOCK_TOTAL_SIZE);

    // Init heap for large objects
    word_t *largeHeapStart = mapAndAlign(LARGE_BLOCK_MASK);
    heap->largeHeapSize = initialSize;
    heap->largeAllocator = LargeAllocator_create(largeHeapStart, initialSize);
    heap->largeHeapStart = largeHeapStart;
    heap->largeHeapEnd = (word_t *)((ubyte_t *)largeHeapStart + initialSize);

    return heap;
}
/**
 * Allocates large objects using the `LargeAllocator`.
 * If allocation fails, because there is not enough memory available, it will trigger a collection of both the small and the large heap.
 */
word_t *Heap_allocLarge(Heap *heap, uint32_t objectSize) {

    // Add header
    uint32_t size = objectSize + OBJECT_HEADER_SIZE;

    assert(objectSize % WORD_SIZE == 0);
    assert(size >= MIN_BLOCK_SIZE);

    // Request an object from the `LargeAllocator`
    Object *object = LargeAllocator_getBlock(heap->largeAllocator, size);
    // If the object is not NULL, update it's metadata and return it
    if (object != NULL) {
        ObjectHeader *objectHeader = &object->header;

        Object_setObjectType(objectHeader, object_large);
        Object_setSize(objectHeader, size);
        return Object_toMutatorAddress(object);
    } else {
        // Otherwise collect
        Heap_collect(heap, stack);

        // After collection, try to alloc again, if it fails, grow the heap by at least the size of the object we want to alloc
        object = LargeAllocator_getBlock(heap->largeAllocator, size);
        if (object != NULL) {
            Object_setObjectType(&object->header, object_large);
            Object_setSize(&object->header, size);
            return Object_toMutatorAddress(object);
        } else {
            Heap_growLarge(heap, size);

            object = LargeAllocator_getBlock(heap->largeAllocator, size);
            ObjectHeader *objectHeader = &object->header;

            Object_setObjectType(objectHeader, object_large);
            Object_setSize(objectHeader, size);
            return Object_toMutatorAddress(object);
        }
    }
}

word_t *allocSmallSlow(Heap *heap, uint32_t size) {

    Heap_collect(heap, stack);

    Object *object = (Object *)Allocator_alloc(heap->allocator, size);
    if (object != NULL) {
        ObjectHeader *objectHeader = &object->header;

        Object_setObjectType(objectHeader, object_standard);
        Object_setSize(objectHeader, size);
        Object_setAllocated(objectHeader);
    }

    if (object == NULL) {
        Heap_grow(heap, size);

        object = (Object *)Allocator_alloc(heap->allocator, size);
        assert(object != NULL);

        ObjectHeader *objectHeader = &object->header;

        Object_setObjectType(objectHeader, object_standard);
        Object_setSize(objectHeader, size);
        Object_setAllocated(objectHeader);
    }

    return Object_toMutatorAddress(object);
}

INLINE word_t *Heap_allocSmall(Heap *heap, uint32_t objectSize) {
    // Add header
    uint32_t size = objectSize + OBJECT_HEADER_SIZE;

    assert(objectSize % WORD_SIZE == 0);
    assert(size < MIN_BLOCK_SIZE);

    Object *object = (Object *)Allocator_alloc(heap->allocator, size);
    if (object != NULL) {
        ObjectHeader *objectHeader = &object->header;
        Object_setObjectType(objectHeader, object_standard);
        Object_setSize(objectHeader, size);
        Object_setAllocated(objectHeader);

        return Object_toMutatorAddress(object);
    } else {
        return allocSmallSlow(heap, size);
    }
}

word_t *Heap_alloc(Heap *heap, uint32_t objectSize) {
    assert(objectSize % WORD_SIZE == 0);

    if (objectSize + OBJECT_HEADER_SIZE >= LARGE_BLOCK_SIZE) {
        return Heap_allocLarge(heap, objectSize);
    } else {
        return Heap_allocSmall(heap, objectSize);
    }
}

void Heap_collect(Heap *heap, Stack *stack) {
#ifdef DEBUG_PRINT
    printf("\nCollect\n");
    fflush(stdout);
#endif
    Mark_markRoots(heap, stack);
    Heap_recycle(heap);

#ifdef DEBUG_PRINT
    printf("End collect\n");
    fflush(stdout);
#endif
}

void Heap_recycle(Heap *heap) {
    BlockList_clear(&heap->allocator->recycledBlocks);
    BlockList_clear(&heap->allocator->freeBlocks);

    heap->allocator->freeBlockCount = 0;
    heap->allocator->recycledBlockCount = 0;

    heap->allocator->freeMemoryAfterCollection = 0;

    word_t *current = heap->heapStart;
    while (current != heap->heapEnd) {
        BlockHeader *blockHeader = (BlockHeader *)current;
        Block_recycle(heap->allocator, blockHeader);
        // block_print(blockHeader);
        current += WORDS_IN_BLOCK;
    }
    LargeAllocator_sweep(heap->largeAllocator);

    if (!Allocator_canInitCursors(heap->allocator) ||
        Allocator_shouldGrow(heap->allocator)) {
        size_t increment = heap->smallHeapSize / WORD_SIZE * GROWTH_RATE / 100;
        increment =
            (increment - 1 + WORDS_IN_BLOCK) / WORDS_IN_BLOCK * WORDS_IN_BLOCK;
        Heap_grow(heap, increment);
    }
    Allocator_initCursors(heap->allocator);
}

/** Grows the small heap by at least `increment` words */
void Heap_grow(Heap *heap, size_t increment) {
    assert(increment % WORDS_IN_BLOCK == 0);

#ifdef DEBUG_PRINT
    printf("Growing small heap by %zu bytes, to %zu bytes\n",
           increment * WORD_SIZE, heap->smallHeapSize + increment * WORD_SIZE);
    fflush(stdout);
#endif

    word_t *heapEnd = heap->heapEnd;
    heap->heapEnd = heapEnd + increment;
    heap->smallHeapSize += increment * WORD_SIZE;

    BlockHeader *lastBlock = (BlockHeader *)(heap->heapEnd - WORDS_IN_BLOCK);
    BlockList_addBlocksLast(&heap->allocator->freeBlocks,
                            (BlockHeader *)heapEnd, lastBlock);

    heap->allocator->blockCount += increment / WORDS_IN_BLOCK;
    heap->allocator->freeBlockCount += increment / WORDS_IN_BLOCK;
}

/** Grows the large heap by at least `increment` words */
void Heap_growLarge(Heap *heap, size_t increment) {
    increment = 1UL << log2_ceil(increment);

#ifdef DEBUG_PRINT
    printf("Growing large heap by %zu bytes, to %zu bytes\n",
           increment * WORD_SIZE, heap->largeHeapSize + increment * WORD_SIZE);
    fflush(stdout);
#endif

    word_t *heapEnd = heap->largeHeapEnd;
    heap->largeHeapEnd += increment;
    heap->largeHeapSize += increment * WORD_SIZE;
    heap->largeAllocator->size += increment * WORD_SIZE;

    Bitmap_grow(heap->largeAllocator->bitmap, increment * WORD_SIZE);

    LargeAllocator_addChunk(heap->largeAllocator, (Chunk *)heapEnd,
                            increment * WORD_SIZE);
}