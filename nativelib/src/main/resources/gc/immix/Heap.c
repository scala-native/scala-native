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

Heap *Heap_create(size_t initialSize) {
    assert(initialSize >= 2 * BLOCK_TOTAL_SIZE);
    assert(initialSize % BLOCK_TOTAL_SIZE == 0);

    Heap *heap = malloc(sizeof(Heap));

    word_t *smallHeapStart = mapAndAlign(BLOCK_SIZE_IN_BYTES_INVERSE_MASK);

    heap->smallHeapSize = initialSize;
    heap->heapStart = smallHeapStart;
    heap->heapEnd = smallHeapStart + initialSize / WORD_SIZE;
    heap->allocator =
        Allocator_create(smallHeapStart, initialSize / BLOCK_TOTAL_SIZE);

    word_t *largeHeapStart = mapAndAlign(LARGE_BLOCK_MASK);
    heap->largeHeapSize = initialSize;
    heap->largeAllocator = LargeAllocator_create(largeHeapStart, initialSize);
    heap->largeHeapStart = largeHeapStart;
    heap->largeHeapEnd = (word_t *)((ubyte_t *)largeHeapStart + initialSize);

    return heap;
}

word_t *Heap_allocLarge(Heap *heap, uint32_t objectSize) {
    uint32_t size = objectSize + OBJECT_HEADER_SIZE; // Add header

    assert(objectSize % 8 == 0);
    assert(size >= MIN_BLOCK_SIZE);
    Object *object = LargeAllocator_getBlock(heap->largeAllocator, size);
    if (object != NULL) {
        ObjectHeader *objectHeader = &object->header;

        Object_setObjectType(objectHeader, object_large);
        Object_setSize(objectHeader, size);
        return Object_toMutatorAddress(object);
    } else {
        Heap_collect(heap, stack);

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

            /*
            LargeAllocator_print(heap->largeAllocator);
            printf("Failed to alloc: %u\n", size + 8);
            printf("No more memory available\n");
            fflush(stdout);
            exit(1);*/
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
    uint32_t size = objectSize + OBJECT_HEADER_SIZE; // Add header

    assert(objectSize % 8 == 0);
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
    assert(objectSize % 8 == 0);

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

    word_t *current = heap->heapStart;
    while (current != heap->heapEnd) {
        BlockHeader *blockHeader = (BlockHeader *)current;
        Block_recycle(heap->allocator, blockHeader);
        // block_print(blockHeader);
        current += WORDS_IN_BLOCK;
    }
    LargeAllocator_sweep(heap->largeAllocator);

    if(!Allocator_canInitCursors(heap->allocator) || Allocator_shouldGrow(heap->allocator)) {
        size_t increment = heap->smallHeapSize / WORD_SIZE * GROWTH_RATE / 100;
        increment = (increment - 1 + WORDS_IN_BLOCK) / WORDS_IN_BLOCK * WORDS_IN_BLOCK;
        Heap_grow(heap, increment);
    }
    Allocator_initCursors(heap->allocator);
}

// increment in words
void Heap_grow(Heap* heap, size_t increment) {
    assert(increment % WORDS_IN_BLOCK == 0);

#ifdef DEBUG_PRINT
    printf("Growing small heap by %zu bytes, to %zu bytes\n", increment * WORD_SIZE, heap->smallHeapSize + increment * WORD_SIZE);
    fflush(stdout);
#endif

    word_t* heapEnd = heap->heapEnd;
    heap->heapEnd = heapEnd + increment;
    heap->smallHeapSize += increment * WORD_SIZE;


    BlockHeader* lastBlock = (BlockHeader*)(heap->heapEnd - WORDS_IN_BLOCK);
    BlockList_addBlocksLast(&heap->allocator->freeBlocks, (BlockHeader*)heapEnd, lastBlock);

    heap->allocator->blockCount += increment / WORDS_IN_BLOCK;
    heap->allocator->freeBlockCount += increment / WORDS_IN_BLOCK;
}

void Heap_growLarge(Heap* heap, size_t increment) {
    increment = 1UL << log2_ceil(increment);

#ifdef DEBUG_PRINT
    printf("Growing large heap by %zu bytes, to %zu bytes\n", increment * WORD_SIZE, heap->largeHeapSize + increment * WORD_SIZE);
    fflush(stdout);
#endif

    word_t* heapEnd = heap->largeHeapEnd;
    heap->largeHeapEnd += increment;
    heap->largeHeapSize += increment * WORD_SIZE;
    heap->largeAllocator->size += increment * WORD_SIZE;

    Bitmap_grow(heap->largeAllocator->bitmap, increment * WORD_SIZE);

    LargeAllocator_addChunk(heap->largeAllocator, (Chunk*)heapEnd, increment * WORD_SIZE);
}