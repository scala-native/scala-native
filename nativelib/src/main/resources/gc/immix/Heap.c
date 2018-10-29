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
#include "Settings.h"
#include "Memory.h"
#include <memory.h>
#include <time.h>

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

    word_t maxNumberOfBlocks = memoryLimit / BLOCK_TOTAL_SIZE;
    uint32_t initialBlockCount = initialSmallHeapSize / BLOCK_TOTAL_SIZE;

    // reserve space for block headers
    size_t blockMetaSpaceSize = maxNumberOfBlocks * BLOCK_METADATA_SIZE;
    word_t *blockMetaStart =
        Heap_mapAndAlign(blockMetaSpaceSize, BLOCK_METADATA_SIZE);
    heap->blockMetaStart = blockMetaStart;
    heap->blockMetaEnd =
        blockMetaStart + initialBlockCount * WORDS_IN_BLOCK_METADATA;

    // reserve space for line headers
    size_t lineMetaSpaceSize =
        maxNumberOfBlocks * LINE_COUNT * LINE_METADATA_SIZE;
    word_t *lineMetaStart = Heap_mapAndAlign(lineMetaSpaceSize, WORD_SIZE);
    heap->lineMetaStart = lineMetaStart;
    assert(LINE_COUNT * LINE_SIZE == BLOCK_TOTAL_SIZE);
    assert(LINE_COUNT * LINE_METADATA_SIZE % WORD_SIZE == 0);
    heap->lineMetaEnd = lineMetaStart + initialBlockCount * LINE_COUNT *
                                            LINE_METADATA_SIZE / WORD_SIZE;

    word_t *smallHeapStart = Heap_mapAndAlign(memoryLimit, BLOCK_TOTAL_SIZE);

    // reserve space for bytemap
    Bytemap *smallBytemap = (Bytemap *)Heap_mapAndAlign(
        memoryLimit / ALLOCATION_ALIGNMENT + sizeof(Bytemap),
        ALLOCATION_ALIGNMENT);
    heap->smallBytemap = smallBytemap;

    // Init heap for small objects
    heap->smallHeapSize = initialSmallHeapSize;
    heap->heapStart = smallHeapStart;
    heap->heapEnd = smallHeapStart + initialSmallHeapSize / WORD_SIZE;
    Bytemap_Init(smallBytemap, smallHeapStart, memoryLimit);
    Allocator_Init(&allocator, smallBytemap, blockMetaStart, smallHeapStart,
                   initialBlockCount);

    // reserve space for bytemap
    Bytemap *largeBytemap = (Bytemap *)Heap_mapAndAlign(
        memoryLimit / ALLOCATION_ALIGNMENT + sizeof(Bytemap), WORD_SIZE);
    heap->largeBytemap = largeBytemap;

    // Init heap for large objects
    word_t *largeHeapStart = Heap_mapAndAlign(memoryLimit, MIN_BLOCK_SIZE);
    heap->largeHeapSize = initialLargeHeapSize;
    heap->largeHeapStart = largeHeapStart;
    word_t *largeHeapEnd =
        (word_t *)((ubyte_t *)largeHeapStart + initialLargeHeapSize);
    heap->largeHeapEnd = largeHeapEnd;
    Bytemap_Init(largeBytemap, largeHeapStart, memoryLimit);
    assert(largeBytemap->end <= ((ubyte_t *)largeBytemap) +
                                    memoryLimit / ALLOCATION_ALIGNMENT +
                                    sizeof(Bytemap));
    LargeAllocator_Init(&largeAllocator, largeHeapStart, initialLargeHeapSize,
                        largeBytemap);

    char *statsFile = Settings_StatsFileName();
    if (statsFile != NULL) {
        heap->stats = malloc(sizeof(Stats));
        Stats_Init(heap->stats, statsFile);
    }
}
/**
 * Allocates large objects using the `LargeAllocator`.
 * If allocation fails, because there is not enough memory available, it will
 * trigger a collection of both the small and the large heap.
 */
word_t *Heap_AllocLarge(Heap *heap, uint32_t size) {

    assert(size % ALLOCATION_ALIGNMENT == 0);
    assert(size >= MIN_BLOCK_SIZE);

    // Request an object from the `LargeAllocator`
    Object *object = LargeAllocator_GetBlock(&largeAllocator, size);
    // If the object is not NULL, update it's metadata and return it
    if (object != NULL) {
        return (word_t *)object;
    } else {
        // Otherwise collect
        Heap_Collect(heap, &stack);

        // After collection, try to alloc again, if it fails, grow the heap by
        // at least the size of the object we want to alloc
        object = LargeAllocator_GetBlock(&largeAllocator, size);
        if (object != NULL) {
            assert(Heap_IsWordInLargeHeap(heap, (word_t *)object));
            return (word_t *)object;
        } else {
            Heap_GrowLarge(heap, size);

            object = LargeAllocator_GetBlock(&largeAllocator, size);
            assert(Heap_IsWordInLargeHeap(heap, (word_t *)object));
            return (word_t *)object;
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
    assert(Heap_IsWordInSmallHeap(heap, (word_t *)object));
    assert(object != NULL);
    ObjectMeta *objectMeta = Bytemap_Get(allocator.bytemap, (word_t *)object);
    ObjectMeta_SetAllocated(objectMeta);
    return (word_t *)object;
}

INLINE word_t *Heap_AllocSmall(Heap *heap, uint32_t size) {
    assert(size % ALLOCATION_ALIGNMENT == 0);
    assert(size < MIN_BLOCK_SIZE);

    word_t *start = allocator.cursor;
    word_t *end = (word_t *)((uint8_t *)start + size);

    // Checks if the end of the block overlaps with the limit
    if (end >= allocator.limit) {
        return Heap_allocSmallSlow(heap, size);
    }

    allocator.cursor = end;

    memset(start, 0, size);

    Object *object = (Object *)start;
    ObjectMeta *objectMeta = Bytemap_Get(allocator.bytemap, (word_t *)object);
    ObjectMeta_SetAllocated(objectMeta);

    __builtin_prefetch(object + 36, 0, 3);

    assert(Heap_IsWordInSmallHeap(heap, (word_t *)object));
    return (word_t *)object;
}

word_t *Heap_Alloc(Heap *heap, uint32_t objectSize) {
    assert(objectSize % ALLOCATION_ALIGNMENT == 0);

    if (objectSize >= LARGE_BLOCK_SIZE) {
        return Heap_AllocLarge(heap, objectSize);
    } else {
        return Heap_AllocSmall(heap, objectSize);
    }
}

void Heap_Collect(Heap *heap, Stack *stack) {
    uint64_t start_ns, sweep_start_ns, end_ns;
    Stats *stats = heap->stats;
#ifdef DEBUG_PRINT
    printf("\nCollect\n");
    fflush(stdout);
#endif
    if (stats != NULL) {
        start_ns = scalanative_nano_time();
    }
    Marker_MarkRoots(heap, stack);
    if (stats != NULL) {
        sweep_start_ns = scalanative_nano_time();
    }
    Heap_Recycle(heap);
    if (stats != NULL) {
        end_ns = scalanative_nano_time();
        Stats_RecordCollection(stats, start_ns, sweep_start_ns, end_ns);
    }
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

    word_t *current = heap->blockMetaStart;
    word_t *currentBlockStart = heap->heapStart;
    LineMeta *lineMetas = (LineMeta *)heap->lineMetaStart;
    while (current < heap->blockMetaEnd) {
        BlockMeta *blockMeta = (BlockMeta *)current;
        Block_Recycle(&allocator, blockMeta, currentBlockStart, lineMetas);
        // block_print(blockMeta);
        current += WORDS_IN_BLOCK_METADATA;
        currentBlockStart += WORDS_IN_BLOCK;
        lineMetas += LINE_COUNT;
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
    uint32_t incrementInBlocks = increment / WORDS_IN_BLOCK;

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
    word_t *blockMetaEnd = heap->blockMetaEnd;
    heap->blockMetaEnd += incrementInBlocks * WORDS_IN_BLOCK_METADATA;
    heap->lineMetaEnd +=
        incrementInBlocks * LINE_COUNT * LINE_METADATA_SIZE / WORD_SIZE;

    BlockMeta *lastBlock =
        (BlockMeta *)(heap->blockMetaEnd - WORDS_IN_BLOCK_METADATA);
    BlockList_AddBlocksLast(&allocator.freeBlocks, (BlockMeta *)blockMetaEnd,
                            lastBlock);

    allocator.blockCount += incrementInBlocks;
    allocator.freeBlockCount += incrementInBlocks;
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

    LargeAllocator_AddChunk(&largeAllocator, (Chunk *)heapEnd,
                            increment * WORD_SIZE);
}
