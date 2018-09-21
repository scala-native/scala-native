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
#include "StackTrace.h"
#include "Memory.h"
#include <memory.h>

// Allow read and write
#define HEAP_MEM_PROT (PROT_READ | PROT_WRITE)
// Map private anonymous memory, and prevent from reserving swap
#define HEAP_MEM_FLAGS (MAP_NORESERVE | MAP_PRIVATE | MAP_ANONYMOUS)
// Map anonymous memory (not a file)
#define HEAP_MEM_FD -1
#define HEAP_MEM_FD_OFFSET 0

size_t Heap_getMemoryLimit() { return getMemorySize(); }

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
void Heap_Init(Heap *heap, size_t initialSmallHeapSize,
               size_t initialLargeHeapSize) {
    assert(initialSmallHeapSize >= 2 * BLOCK_TOTAL_SIZE);
    assert(initialSmallHeapSize % BLOCK_TOTAL_SIZE == 0);
    assert(initialLargeHeapSize >= 2 * BLOCK_TOTAL_SIZE);
    assert(initialLargeHeapSize % BLOCK_TOTAL_SIZE == 0);

    size_t memoryLimit = Heap_getMemoryLimit();
    heap->memoryLimit = memoryLimit;

    word_t *smallHeapStart = Heap_mapAndAlign(memoryLimit, BLOCK_TOTAL_SIZE);

    // Init heap for small objects
    heap->smallHeapSize = initialSmallHeapSize;
    heap->heapStart = smallHeapStart;
    heap->heapEnd = smallHeapStart + initialSmallHeapSize / WORD_SIZE;
    Allocator_Init(&allocator, smallHeapStart,
                   initialSmallHeapSize / BLOCK_TOTAL_SIZE);

    // Init heap for large objects
    word_t *largeHeapStart = Heap_mapAndAlign(memoryLimit, MIN_BLOCK_SIZE);
    heap->largeHeapSize = initialLargeHeapSize;
    LargeAllocator_Init(&largeAllocator, largeHeapStart, initialLargeHeapSize);
    heap->largeHeapStart = largeHeapStart;
    heap->largeHeapEnd =
        (word_t *)((ubyte_t *)largeHeapStart + initialLargeHeapSize);
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
    Object *object = LargeAllocator_GetBlock(&largeAllocator, size);
    // If the object is not NULL, update it's metadata and return it
    if (object != NULL) {
        ObjectHeader *objectHeader = &object->header;

        Object_SetObjectType(objectHeader, object_large);
        Object_SetSize(objectHeader, size);
        return Object_ToMutatorAddress(object);
    } else {
        // Otherwise collect
        Heap_Collect(heap, &stack);

        // After collection, try to alloc again, if it fails, grow the heap by
        // at least the size of the object we want to alloc
        object = LargeAllocator_GetBlock(&largeAllocator, size);
        if (object != NULL) {
            Object_SetObjectType(&object->header, object_large);
            Object_SetSize(&object->header, size);
            return Object_ToMutatorAddress(object);
        } else {
            Heap_GrowLarge(heap, size);

            object = LargeAllocator_GetBlock(&largeAllocator, size);
            ObjectHeader *objectHeader = &object->header;

            Object_SetObjectType(objectHeader, object_large);
            Object_SetSize(objectHeader, size);
            return Object_ToMutatorAddress(object);
        }
    }
}

NOINLINE word_t *Heap_allocSmallSlow(Heap *heap, uint32_t size) {
    Object *object;
    object = (Object *)Allocator_Alloc(&allocator, size);

    if (object != NULL)
        goto done;

    Heap_Collect(heap, &stack);
    object = (Object *)Allocator_Alloc(&allocator, size);

    if (object != NULL)
        goto done;

    // A small object can always fit in a single free block
    // because it is no larger than 8K while the block is 32K.
    Heap_Grow(heap, WORDS_IN_BLOCK);
    object = (Object *)Allocator_Alloc(&allocator, size);

done:
    assert(object != NULL);
    ObjectHeader *objectHeader = &object->header;
    Object_SetObjectType(objectHeader, object_standard);
    Object_SetSize(objectHeader, size);
    Object_SetAllocated(objectHeader);
    return Object_ToMutatorAddress(object);
}

INLINE word_t *Heap_AllocSmall(Heap *heap, uint32_t objectSize) {
    // Add header
    uint32_t size = objectSize + OBJECT_HEADER_SIZE;

    assert(objectSize % WORD_SIZE == 0);
    assert(size < MIN_BLOCK_SIZE);

    word_t *start = allocator.cursor;
    word_t *end = (word_t *)((uint8_t *)start + size);

    // Checks if the end of the block overlaps with the limit
    if (end >= allocator.limit) {
        return Heap_allocSmallSlow(heap, size);
    }

    allocator.cursor = end;

    Line_Update(allocator.block, start);

    memset(start, 0, size + WORD_SIZE);

    Object *object = (Object *)start;
    ObjectHeader *objectHeader = &object->header;
    Object_SetObjectType(objectHeader, object_standard);
    Object_SetSize(objectHeader, size);
    Object_SetAllocated(objectHeader);

    __builtin_prefetch(object + 36, 0, 3);

    return Object_ToMutatorAddress(object);
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
    BlockList_Clear(&allocator.recycledBlocks);
    BlockList_Clear(&allocator.freeBlocks);

    allocator.freeBlockCount = 0;
    allocator.recycledBlockCount = 0;
    allocator.freeMemoryAfterCollection = 0;

    word_t *current = heap->heapStart;
    while (current != heap->heapEnd) {
        BlockHeader *blockHeader = (BlockHeader *)current;
        Block_Recycle(&allocator, blockHeader);
        // block_print(blockHeader);
        current += WORDS_IN_BLOCK;
    }
    LargeAllocator_Sweep(&largeAllocator);

    if (Allocator_ShouldGrow(&allocator)) {
        double growth;
        if (heap->smallHeapSize < EARLY_GROWTH_THRESHOLD) {
            growth = EARLY_GROWTH_RATE;
        } else {
            growth = GROWTH_RATE;
        }
        size_t blocks = allocator.blockCount * (growth - 1);
        size_t increment = blocks * WORDS_IN_BLOCK;
        Heap_Grow(heap, increment);
    }
    Allocator_InitCursors(&allocator);
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
        if (Allocator_CanInitCursors(&allocator)) {
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
    BlockList_AddBlocksLast(&allocator.freeBlocks, (BlockHeader *)heapEnd,
                            lastBlock);

    allocator.blockCount += increment / WORDS_IN_BLOCK;
    allocator.freeBlockCount += increment / WORDS_IN_BLOCK;
}

/** Grows the large heap by at least `increment` words */
void Heap_GrowLarge(Heap *heap, size_t increment) {
    increment = 1UL << MathUtils_Log2Ceil(increment);

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
    largeAllocator.size += increment * WORD_SIZE;

    Bitmap_Grow(largeAllocator.bitmap, increment * WORD_SIZE);

    LargeAllocator_AddChunk(&largeAllocator, (Chunk *)heapEnd,
                            increment * WORD_SIZE);
}
