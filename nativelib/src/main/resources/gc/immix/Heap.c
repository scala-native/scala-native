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
    assert(alignmentSize % WORD_SIZE == 0);
    word_t *heapStart = mmap(NULL, memoryLimit, HEAP_MEM_PROT, HEAP_MEM_FLAGS,
                             HEAP_MEM_FD, HEAP_MEM_FD_OFFSET);

    size_t alignmentMask = ~(alignmentSize - 1);
    // Heap start not aligned on
    if (((word_t)heapStart & alignmentMask) != (word_t)heapStart) {
        word_t *previousBlock = (word_t *)((word_t)heapStart & alignmentMask);
        heapStart = previousBlock + alignmentSize / WORD_SIZE;
    }
    return heapStart;
}

/**
 * Allocates the heap struct and initializes it
 */
void Heap_Init(Heap *heap, size_t initialHeapSize) {
    assert(initialHeapSize >= 2 * BLOCK_TOTAL_SIZE);
    assert(initialHeapSize % BLOCK_TOTAL_SIZE == 0);

    size_t memoryLimit = Heap_getMemoryLimit();
    heap->memoryLimit = memoryLimit;

    word_t maxNumberOfBlocks = memoryLimit / BLOCK_TOTAL_SIZE;
    uint32_t initialBlockCount = initialHeapSize / BLOCK_TOTAL_SIZE;

    // reserve space for block headers
    size_t blockMetaSpaceSize = maxNumberOfBlocks * sizeof(BlockMeta);
    word_t *blockMetaStart = Heap_mapAndAlign(blockMetaSpaceSize, WORD_SIZE);
    heap->blockMetaStart = blockMetaStart;
    heap->blockMetaEnd =
        blockMetaStart + initialBlockCount * sizeof(BlockMeta) / WORD_SIZE;

    // reserve space for line headers
    size_t lineMetaSpaceSize =
        maxNumberOfBlocks * LINE_COUNT * LINE_METADATA_SIZE;
    word_t *lineMetaStart = Heap_mapAndAlign(lineMetaSpaceSize, WORD_SIZE);
    heap->lineMetaStart = lineMetaStart;
    assert(LINE_COUNT * LINE_SIZE == BLOCK_TOTAL_SIZE);
    assert(LINE_COUNT * LINE_METADATA_SIZE % WORD_SIZE == 0);
    heap->lineMetaEnd = lineMetaStart + initialBlockCount * LINE_COUNT *
                                            LINE_METADATA_SIZE / WORD_SIZE;

    word_t *heapStart = Heap_mapAndAlign(memoryLimit, BLOCK_TOTAL_SIZE);

    BlockAllocator_Init(&blockAllocator, blockMetaStart, initialBlockCount);

    // reserve space for bytemap
    Bytemap *bytemap = (Bytemap *)Heap_mapAndAlign(
        memoryLimit / ALLOCATION_ALIGNMENT + sizeof(Bytemap),
        ALLOCATION_ALIGNMENT);
    heap->bytemap = bytemap;

    // Init heap for small objects
    heap->heapSize = initialHeapSize;
    heap->heapStart = heapStart;
    heap->heapEnd = heapStart + initialHeapSize / WORD_SIZE;
    Bytemap_Init(bytemap, heapStart, memoryLimit);
    Allocator_Init(&allocator, &blockAllocator, bytemap, blockMetaStart,
                   heapStart);

    LargeAllocator_Init(&largeAllocator, &blockAllocator, bytemap,
                        blockMetaStart, heapStart);
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
            assert(Heap_IsWordInHeap(heap, (word_t *)object));
            return (word_t *)object;
        } else {
            size_t increment = MathUtils_DivAndRoundUp(size, BLOCK_TOTAL_SIZE);
            uint32_t pow2increment = 1U << MathUtils_Log2Ceil(increment);
            Heap_Grow(heap, pow2increment);

            object = LargeAllocator_GetBlock(&largeAllocator, size);
            assert(object != NULL);
            assert(Heap_IsWordInHeap(heap, (word_t *)object));
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
    Heap_Grow(heap, 1);
    object = (Object *)Allocator_Alloc(&allocator, size);

done:
    assert(Heap_IsWordInHeap(heap, (word_t *)object));
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

    assert(Heap_IsWordInHeap(heap, (word_t *)object));
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
    Allocator_Clear(&allocator);
    LargeAllocator_Clear(&largeAllocator);
    BlockAllocator_Clear(&blockAllocator);

    BlockMeta *current = (BlockMeta *)heap->blockMetaStart;
    word_t *currentBlockStart = heap->heapStart;
    LineMeta *lineMetas = (LineMeta *)heap->lineMetaStart;
    word_t *end = heap->blockMetaEnd;
    while ((word_t *)current < end) {
        int size = 1;
        assert(!BlockMeta_IsSuperblockMiddle(current));
        if (BlockMeta_IsSimpleBlock(current)) {
            Block_Recycle(&allocator, current, currentBlockStart, lineMetas);
        } else if (BlockMeta_IsSuperblockStart(current)) {
            size = BlockMeta_SuperblockSize(current);
            LargeAllocator_Sweep(&largeAllocator, current, currentBlockStart);
        } else {
            assert(BlockMeta_IsFree(current));
            BlockAllocator_AddFreeBlocks(&blockAllocator, current, 1);
        }
        assert(size > 0);
        current += size;
        currentBlockStart += WORDS_IN_BLOCK * size;
        lineMetas += LINE_COUNT * size;
    }

    if (Allocator_ShouldGrow(&allocator)) {
        double growth;
        if (heap->heapSize < EARLY_GROWTH_THRESHOLD) {
            growth = EARLY_GROWTH_RATE;
        } else {
            growth = GROWTH_RATE;
        }
        size_t blocks = blockAllocator.blockCount * (growth - 1);
        Heap_Grow(heap, blocks);
    }
    BlockAllocator_SweepDone(&blockAllocator);
    Allocator_InitCursors(&allocator);
}

void Heap_exitWithOutOfMemory() {
    printf("Out of heap space\n");
    StackTrace_PrintStackTrace();
    exit(1);
}

bool Heap_isGrowingPossible(Heap *heap, size_t increment) {
    return heap->heapSize + increment <= heap->memoryLimit;
}

/** Grows the small heap by at least `increment` words */
void Heap_Grow(Heap *heap, size_t incrementInBlocks) {

    // If we cannot grow because we reached the memory limit
    if (!Heap_isGrowingPossible(heap, incrementInBlocks * BLOCK_TOTAL_SIZE)) {
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
           increment * WORD_SIZE, heap->heapSize + increment * WORD_SIZE);
    fflush(stdout);
#endif

    word_t *heapEnd = heap->heapEnd;
    heap->heapEnd = heapEnd + incrementInBlocks * BLOCK_TOTAL_SIZE;
    heap->heapSize += incrementInBlocks * BLOCK_TOTAL_SIZE;
    word_t *blockMetaEnd = heap->blockMetaEnd;
    heap->blockMetaEnd =
        (word_t *)(((BlockMeta *)heap->blockMetaEnd) + incrementInBlocks);
    heap->lineMetaEnd +=
        incrementInBlocks * LINE_COUNT * LINE_METADATA_SIZE / WORD_SIZE;

    BlockAllocator_AddFreeBlocks(&blockAllocator, (BlockMeta *)blockMetaEnd,
                                 incrementInBlocks);

    blockAllocator.blockCount += incrementInBlocks;

    // immediately add the block to freelists
    BlockAllocator_SweepDone(&blockAllocator);
}
