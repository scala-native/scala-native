#include <stdlib.h>
#ifndef _WIN32
#include <sys/mman.h>
#else
#include "../../os_win_mman.h"
#endif
#include <stdio.h>
#include "Heap.h"
#include "Block.h"
#include "Log.h"
#include "Allocator.h"
#include "Marker.h"
#include "State.h"
#include "utils/MathUtils.h"
#include "StackTrace.h"
#include "Memory.h"

// Allow read and write
#define HEAP_MEM_PROT (PROT_READ | PROT_WRITE)
// Map private anonymous memory, and prevent from reserving swap
#define HEAP_MEM_FLAGS (MAP_NORESERVE | MAP_PRIVATE | MAP_ANONYMOUS)
// Map anonymous memory (not a file)
#define HEAP_MEM_FD -1
#define HEAP_MEM_FD_OFFSET 0

size_t Heap_getMemoryLimit() {
#ifndef _WIN32
    return getMemorySize();
#else
    size_t maximum = getMemorySize();
    // temporary fix for Windows 7 and less
    const unsigned long long TEMP_LIMIT =
        ((unsigned long long)1 * 1024 * 1024 * 1024);
    if (maximum > TEMP_LIMIT) {
        maximum = TEMP_LIMIT;
    }
    return maximum;
#endif
}

/**
 * Maps `MAX_SIZE` of memory and returns the first address aligned on
 * `alignement` mask
 */
word_t *Heap_mapAndAlign(size_t memoryLimit, size_t alignmentSize) {
    word_t *heapStart = mmap(NULL, memoryLimit, HEAP_MEM_PROT, HEAP_MEM_FLAGS,
                             HEAP_MEM_FD, HEAP_MEM_FD_OFFSET);

    size_t alignmentMask = ~(alignmentSize - 1);
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
Heap *Heap_Create(size_t initialSize) {
    assert(initialSize >= 2 * BLOCK_TOTAL_SIZE);
    assert(initialSize % BLOCK_TOTAL_SIZE == 0);

    Heap *heap = malloc(sizeof(Heap));

    size_t memoryLimit = Heap_getMemoryLimit();
    heap->memoryLimit = memoryLimit;

    word_t *smallHeapStart = Heap_mapAndAlign(memoryLimit, BLOCK_TOTAL_SIZE);

    // Init heap for small objects
    heap->smallHeapSize = initialSize;
    heap->heapStart = smallHeapStart;
    heap->heapEnd = smallHeapStart + initialSize / WORD_SIZE;
    heap->allocator =
        Allocator_Create(smallHeapStart, initialSize / BLOCK_TOTAL_SIZE);

    // Init heap for large objects
    word_t *largeHeapStart = Heap_mapAndAlign(memoryLimit, MIN_BLOCK_SIZE);
    heap->largeHeapSize = initialSize;
    heap->largeAllocator = LargeAllocator_Create(largeHeapStart, initialSize);
    heap->largeHeapStart = largeHeapStart;
    heap->largeHeapEnd = (word_t *)((ubyte_t *)largeHeapStart + initialSize);

    return heap;
}
/**
 * Allocates large objects using the `LargeAllocator`.
 * If allocation fails, because there is not enough memory available, it will
 * trigger a collection of both the small and the large heap.
 */
word_t *Heap_AllocLarge(Heap *heap, uint32_t objectSize) {

    // Add header
    uint32_t size = objectSize + OBJECT_HEADER_SIZE;

    assert(objectSize % WORD_SIZE == 0);
    assert(size >= MIN_BLOCK_SIZE);

    // Request an object from the `LargeAllocator`
    Object *object = LargeAllocator_GetBlock(heap->largeAllocator, size);
    // If the object is not NULL, update it's metadata and return it
    if (object != NULL) {
        ObjectHeader *objectHeader = &object->header;

        Object_SetObjectType(objectHeader, object_large);
        Object_SetSize(objectHeader, size);
        return Object_ToMutatorAddress(object);
    } else {
        // Otherwise collect
        Heap_Collect(heap, stack);

        // After collection, try to alloc again, if it fails, grow the heap by
        // at least the size of the object we want to alloc
        object = LargeAllocator_GetBlock(heap->largeAllocator, size);
        if (object != NULL) {
            Object_SetObjectType(&object->header, object_large);
            Object_SetSize(&object->header, size);
            return Object_ToMutatorAddress(object);
        } else {
            Heap_GrowLarge(heap, size);

            object = LargeAllocator_GetBlock(heap->largeAllocator, size);
            ObjectHeader *objectHeader = &object->header;

            Object_SetObjectType(objectHeader, object_large);
            Object_SetSize(objectHeader, size);
            return Object_ToMutatorAddress(object);
        }
    }
}

word_t *Heap_allocSmallSlow(Heap *heap, uint32_t size) {

    Heap_Collect(heap, stack);

    Object *object = (Object *)Allocator_Alloc(heap->allocator, size);
    if (object != NULL) {
        ObjectHeader *objectHeader = &object->header;

        Object_SetObjectType(objectHeader, object_standard);
        Object_SetSize(objectHeader, size);
        Object_SetAllocated(objectHeader);
    }

    if (object == NULL) {
        Heap_Grow(heap, size);

        object = (Object *)Allocator_Alloc(heap->allocator, size);
        assert(object != NULL);

        ObjectHeader *objectHeader = &object->header;

        Object_SetObjectType(objectHeader, object_standard);
        Object_SetSize(objectHeader, size);
        Object_SetAllocated(objectHeader);
    }

    return Object_ToMutatorAddress(object);
}

INLINE word_t *Heap_AllocSmall(Heap *heap, uint32_t objectSize) {
    // Add header
    uint32_t size = objectSize + OBJECT_HEADER_SIZE;

    assert(objectSize % WORD_SIZE == 0);
    assert(size < MIN_BLOCK_SIZE);

    Object *object = (Object *)Allocator_Alloc(heap->allocator, size);
    if (object != NULL) {
        ObjectHeader *objectHeader = &object->header;
        Object_SetObjectType(objectHeader, object_standard);
        Object_SetSize(objectHeader, size);
        Object_SetAllocated(objectHeader);

        return Object_ToMutatorAddress(object);
    } else {
        return Heap_allocSmallSlow(heap, size);
    }
}

word_t *Heap_Alloc(Heap *heap, uint32_t objectSize) {
    assert(objectSize % WORD_SIZE == 0);

    if (objectSize + OBJECT_HEADER_SIZE >= LARGE_BLOCK_SIZE) {
        return Heap_AllocLarge(heap, objectSize);
    } else {
        return Heap_AllocSmall(heap, objectSize);
    }
}

void Heap_Collect(Heap *heap, Stack *stack) {
#ifdef DEBUG_PRINT
    printf("\nCollect\n");
    fflush(stdout);
#endif
    Marker_MarkRoots(heap, stack);
    Heap_Recycle(heap);

#ifdef DEBUG_PRINT
    printf("End collect\n");
    fflush(stdout);
#endif
}

void Heap_Recycle(Heap *heap) {
    BlockList_Clear(&heap->allocator->recycledBlocks);
    BlockList_Clear(&heap->allocator->freeBlocks);

    heap->allocator->freeBlockCount = 0;
    heap->allocator->recycledBlockCount = 0;

    heap->allocator->freeMemoryAfterCollection = 0;

    word_t *current = heap->heapStart;
    while (current != heap->heapEnd) {
        BlockHeader *blockHeader = (BlockHeader *)current;
        Block_Recycle(heap->allocator, blockHeader);
        // block_print(blockHeader);
        current += WORDS_IN_BLOCK;
    }
    LargeAllocator_Sweep(heap->largeAllocator);

    if (!Allocator_CanInitCursors(heap->allocator) ||
        Allocator_ShouldGrow(heap->allocator)) {
        size_t increment = heap->smallHeapSize / WORD_SIZE * GROWTH_RATE / 100;
        increment =
            (increment - 1 + WORDS_IN_BLOCK) / WORDS_IN_BLOCK * WORDS_IN_BLOCK;
        Heap_Grow(heap, increment);
    }
    Allocator_InitCursors(heap->allocator);
}

void Heap_exitWithOutOfMemory() {
    printf("Out of heap space\n");
    StackTrace_PrintStackTrace();
    exit(1);
}

bool Heap_isGrowingPossible(Heap *heap, size_t increment) {
    return heap->smallHeapSize + heap->largeHeapSize + increment * WORD_SIZE <=
           heap->memoryLimit;
}

/** Grows the small heap by at least `increment` words */
void Heap_Grow(Heap *heap, size_t increment) {
    assert(increment % WORDS_IN_BLOCK == 0);

    // If we cannot grow because we reached the memory limit
    if (!Heap_isGrowingPossible(heap, increment)) {
        // If we can still init the cursors, grow by max possible increment
        if (Allocator_CanInitCursors(heap->allocator)) {
            // increment = heap->memoryLimit - (heap->smallHeapSize +
            // heap->largeHeapSize);
            // round down to block size
            // increment = increment / BLOCK_TOTAL_SIZE * BLOCK_TOTAL_SIZE;
            return;
        } else {
            // If the cursors cannot be initialised and we cannot grow, throw
            // out of memory exception.
            Heap_exitWithOutOfMemory();
        }
    }

#ifdef DEBUG_PRINT
    printf("Growing small heap by %zu bytes, to %zu bytes\n",
           increment * WORD_SIZE, heap->smallHeapSize + increment * WORD_SIZE);
    fflush(stdout);
#endif

    word_t *heapEnd = heap->heapEnd;
    heap->heapEnd = heapEnd + increment;
    heap->smallHeapSize += increment * WORD_SIZE;

    BlockHeader *lastBlock = (BlockHeader *)(heap->heapEnd - WORDS_IN_BLOCK);
    BlockList_AddBlocksLast(&heap->allocator->freeBlocks,
                            (BlockHeader *)heapEnd, lastBlock);

    heap->allocator->blockCount += increment / WORDS_IN_BLOCK;
    heap->allocator->freeBlockCount += increment / WORDS_IN_BLOCK;
}

/** Grows the large heap by at least `increment` words */
void Heap_GrowLarge(Heap *heap, size_t increment) {
    increment = (unsigned long long)1UL << MathUtils_Log2Ceil(increment);

    if (heap->smallHeapSize + heap->largeHeapSize + increment * WORD_SIZE >
        heap->memoryLimit) {
        Heap_exitWithOutOfMemory();
    }
#ifdef DEBUG_PRINT
    printf("Growing large heap by %zu bytes, to %zu bytes\n",
           increment * WORD_SIZE, heap->largeHeapSize + increment * WORD_SIZE);
    fflush(stdout);
#endif

    word_t *heapEnd = heap->largeHeapEnd;
    heap->largeHeapEnd += increment;
    heap->largeHeapSize += increment * WORD_SIZE;
    heap->largeAllocator->size += increment * WORD_SIZE;

    Bitmap_Grow(heap->largeAllocator->bitmap, increment * WORD_SIZE);

    LargeAllocator_AddChunk(heap->largeAllocator, (Chunk *)heapEnd,
                            increment * WORD_SIZE);
}
