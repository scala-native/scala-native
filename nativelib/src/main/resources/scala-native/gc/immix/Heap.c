#include <stdlib.h>
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
#include "MemoryInfo.h"
#include "MemoryMap.h"
#include <time.h>
#include "WeakRefStack.h"
#include "Synchronizer.h"

void Heap_exitWithOutOfMemory(const char *details) {
    fprintf(stderr, "Out of heap space %s\n", details);
    StackTrace_PrintStackTrace();
    exit(1);
}

bool Heap_isGrowingPossible(Heap *heap, uint32_t incrementInBlocks) {
    return heap->blockCount + incrementInBlocks <= heap->maxBlockCount;
}

size_t Heap_getMemoryLimit() {
    size_t memorySize = getMemorySize();
    if ((uint64_t)memorySize > MAX_HEAP_SIZE) {
        return (size_t)MAX_HEAP_SIZE;
    } else {
        return memorySize;
    }
}

/**
 * Maps `MAX_SIZE` of memory and returns the first address aligned on
 * `alignement` mask
 */
word_t *Heap_mapAndAlign(size_t memoryLimit, size_t alignmentSize) {
    assert(alignmentSize % WORD_SIZE == 0);
    word_t *heapStart = memoryMap(memoryLimit);
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
void Heap_Init(Heap *heap, size_t minHeapSize, size_t maxHeapSize) {
    size_t memoryLimit = Heap_getMemoryLimit();

    if (maxHeapSize < MIN_HEAP_SIZE) {
        fprintf(stderr, "GC_MAXIMUM_HEAP_SIZE too small to initialize heap.\n");
        fprintf(stderr, "Minimum required: %zum \n",
                (size_t)(MIN_HEAP_SIZE / 1024 / 1024));
        fflush(stderr);
        exit(1);
    }

    if (minHeapSize > memoryLimit) {
        fprintf(stderr, "GC_INITIAL_HEAP_SIZE is too large.\n");
        fprintf(stderr, "Maximum possible: %zug \n",
                memoryLimit / 1024 / 1024 / 1024);
        fflush(stderr);
        exit(1);
    }

    if (maxHeapSize < minHeapSize) {
        fprintf(stderr, "GC_MAXIMUM_HEAP_SIZE should be at least "
                        "GC_INITIAL_HEAP_SIZE\n");
        fflush(stderr);
        exit(1);
    }

    if (minHeapSize < MIN_HEAP_SIZE) {
        minHeapSize = MIN_HEAP_SIZE;
    }

    if (maxHeapSize == UNLIMITED_HEAP_SIZE) {
        maxHeapSize = memoryLimit;
    }

    uint32_t maxNumberOfBlocks = maxHeapSize / SPACE_USED_PER_BLOCK;
    uint32_t initialBlockCount = minHeapSize / SPACE_USED_PER_BLOCK;
    heap->maxHeapSize = maxHeapSize;
    heap->blockCount = initialBlockCount;
    heap->maxBlockCount = maxNumberOfBlocks;

    // reserve space for block headers
    size_t blockMetaSpaceSize = maxNumberOfBlocks * sizeof(BlockMeta);
    word_t *blockMetaStart = Heap_mapAndAlign(blockMetaSpaceSize, WORD_SIZE);
    heap->blockMetaStart = blockMetaStart;
    heap->blockMetaEnd =
        blockMetaStart + initialBlockCount * sizeof(BlockMeta) / WORD_SIZE;

    // reserve space for line headers
    size_t lineMetaSpaceSize =
        (size_t)maxNumberOfBlocks * LINE_COUNT * LINE_METADATA_SIZE;
    word_t *lineMetaStart = Heap_mapAndAlign(lineMetaSpaceSize, WORD_SIZE);
    heap->lineMetaStart = lineMetaStart;
    assert(LINE_COUNT * LINE_SIZE == BLOCK_TOTAL_SIZE);
    assert(LINE_COUNT * LINE_METADATA_SIZE % WORD_SIZE == 0);
    heap->lineMetaEnd = lineMetaStart + initialBlockCount * LINE_COUNT *
                                            LINE_METADATA_SIZE / WORD_SIZE;

    // reserve space for bytemap
    size_t bytemapSpaceSize =
        maxHeapSize / ALLOCATION_ALIGNMENT + sizeof(Bytemap);
    Bytemap *bytemap =
        (Bytemap *)Heap_mapAndAlign(bytemapSpaceSize, ALLOCATION_ALIGNMENT);
    heap->bytemap = bytemap;

    // Init heap for small objects
    word_t *heapStart = Heap_mapAndAlign(maxHeapSize, BLOCK_TOTAL_SIZE);
    heap->heapSize = minHeapSize;
    heap->heapStart = heapStart;
    heap->heapEnd = heapStart + minHeapSize / WORD_SIZE;

#ifdef _WIN32
    // Commit memory chunks reserved using mapMemory
    bool commitStatus =
        memoryCommit(blockMetaStart, blockMetaSpaceSize) &&
        memoryCommit(lineMetaStart, lineMetaSpaceSize) &&
        memoryCommit(bytemap, bytemapSpaceSize) &&
        // Due to lack of over-committing on Windows on Heap init reserve memory
        // chunk equal to maximal size of heap, but commit only minimal needed
        // chunk of memory. Additional chunks of heap should be committed on
        // demand when growing the heap.
        memoryCommit(heapStart, minHeapSize);
    if (!commitStatus) {
        Heap_exitWithOutOfMemory("commit memmory");
    }
#endif // _WIN32

    BlockAllocator_Init(&blockAllocator, blockMetaStart, initialBlockCount);
    Bytemap_Init(bytemap, heapStart, maxHeapSize);
    char *statsFile = Settings_StatsFileName();
    if (statsFile != NULL) {
        heap->stats = malloc(sizeof(Stats));
        Stats_Init(heap->stats, statsFile);
    }
    mutex_init(&heap->lock);
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
    LargeAllocator *largeAllocator = &currentMutatorThread->largeAllocator;
    Object *object = LargeAllocator_GetBlock(largeAllocator, size);
    // If the object is not NULL, update it's metadata and return it
    if (object != NULL) {
        return (word_t *)object;
    } else {
        uint32_t pow2increment = 0;
        do {
            // Otherwise collect
            Heap_Collect(heap, &stack);

            // After collection, try to alloc again, if it fails, grow the heap
            // by at least the size of the object we want to alloc
            object = LargeAllocator_GetBlock(largeAllocator, size);
            if (object != NULL) {
                assert(Heap_IsWordInHeap(heap, (word_t *)object));
                return (word_t *)object;
            } else {
                if (pow2increment == 0) {
                    size_t increment =
                        MathUtils_DivAndRoundUp(size, BLOCK_TOTAL_SIZE);
                    pow2increment = 1ULL << MathUtils_Log2Ceil(increment);
                    assert(pow2increment > 0);
                }
                Heap_Grow(heap, pow2increment);

                object = LargeAllocator_GetBlock(largeAllocator, size);
                if (object != NULL) {
                    assert(Heap_IsWordInHeap(heap, (word_t *)object));
                    return (word_t *)object;
                }
            }
        } while (Heap_isGrowingPossible(heap, pow2increment));
        Heap_exitWithOutOfMemory("Cannot grow heap to allocate large object");
        return NULL;
    }
}

NOINLINE word_t *Heap_allocSmallSlow(Heap *heap, uint32_t size) {
    Object *object;
    Allocator *allocator = &currentMutatorThread->allocator;
    object = (Object *)Allocator_Alloc(allocator, size);

    if (object != NULL)
        goto done;

    do {
        Heap_Collect(heap, &stack);
        object = (Object *)Allocator_Alloc(allocator, size);

        if (object != NULL)
            goto done;

        // A small object can always fit in a single free block
        // because it is no larger than 8K while the block is 32K.
        Heap_Grow(heap, 1);
        object = (Object *)Allocator_Alloc(allocator, size);
        if (object != NULL)
            goto done;
    } while (Heap_isGrowingPossible(heap, 1));
    Heap_exitWithOutOfMemory("alloc-small cannot grow");

done:
    assert(object != NULL);
    assert(Heap_IsWordInHeap(heap, (word_t *)object));
    ObjectMeta *objectMeta = Bytemap_Get(allocator->bytemap, (word_t *)object);
    ObjectMeta_SetAllocated(objectMeta);
    return (word_t *)object;
}

INLINE word_t *Heap_AllocSmall(Heap *heap, uint32_t size) {
    assert(size % ALLOCATION_ALIGNMENT == 0);
    assert(size < MIN_BLOCK_SIZE);

    Allocator *allocator = &currentMutatorThread->allocator;
    word_t *start = allocator->cursor;
    word_t *end = (word_t *)((uint8_t *)start + size);

    // Checks if the end of the block overlaps with the limit
    if (end >= allocator->limit) {
        return Heap_allocSmallSlow(heap, size);
    }

    allocator->cursor = end;

    memset(start, 0, size);

    Object *object = (Object *)start;
    ObjectMeta *objectMeta = Bytemap_Get(allocator->bytemap, (word_t *)object);
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
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    if (!Synchronizer_acquire())
        return;
#else
    MutatorThread_switchState(currentMutatorThread,
                              MutatorThreadState_Unmanaged);
#endif
    uint64_t start_ns, nullify_start_ns, sweep_start_ns, end_ns;
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
        nullify_start_ns = scalanative_nano_time();
    }
    WeakRefStack_Nullify();
    if (stats != NULL) {
        sweep_start_ns = scalanative_nano_time();
    }
    Heap_Recycle(heap);
    if (stats != NULL) {
        end_ns = scalanative_nano_time();
        Stats_RecordCollection(stats, start_ns, nullify_start_ns,
                               sweep_start_ns, end_ns);
    }
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    Synchronizer_release();
#else
    MutatorThread_switchState(currentMutatorThread, MutatorThreadState_Managed);
#endif
    // Skip calling WeakRef handlers on thread which is being initialized
    // If the current block is set to null it means it failed to allocate
    // memory for allocator and forced GC
    if (currentMutatorThread->allocator.block)
        WeakRefStack_CallHandlers();
#ifdef DEBUG_PRINT
    printf("End collect\n");
    fflush(stdout);
#endif
}

bool Heap_shouldGrow(Heap *heap) {
    uint32_t freeBlockCount = blockAllocator.freeBlockCount;
    uint32_t blockCount = heap->blockCount;
    uint32_t recycledBlockCount = 0;
    MutatorThreads_foreach(mutatorThreads, node) {
        recycledBlockCount += node->value->allocator.recycledBlockCount;
    }
    uint32_t unavailableBlockCount =
        blockCount - (freeBlockCount + recycledBlockCount);

#ifdef DEBUG_PRINT
    printf("\n\nBlock count: %u\n", blockCount);
    printf("Unavailable: %u\n", unavailableBlockCount);
    printf("Free: %u\n", freeBlockCount);
    printf("Recycled: %u\n", recycledBlockCount);
    fflush(stdout);
#endif

    return freeBlockCount * 2 < blockCount ||
           4 * unavailableBlockCount > blockCount;
}

void Heap_Recycle(Heap *heap) {
    MutatorThreads_foreach(mutatorThreads, node) {
        MutatorThread *thread = node->value;
        Allocator_Clear(&thread->allocator);
        LargeAllocator_Clear(&thread->largeAllocator);
    }
    BlockAllocator_Clear(&blockAllocator);

    BlockMeta *current = (BlockMeta *)heap->blockMetaStart;
    word_t *currentBlockStart = heap->heapStart;
    LineMeta *lineMetas = (LineMeta *)heap->lineMetaStart;
    word_t *end = heap->blockMetaEnd;
    MutatorThreads threadsCursor = mutatorThreads;
    while ((word_t *)current < end) {
        int size = 1;
        MutatorThread *recycleBlocksTo = threadsCursor->value;
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
        threadsCursor = threadsCursor->next;
        if (threadsCursor == NULL) {
            threadsCursor = mutatorThreads;
        }
#endif

        assert(!BlockMeta_IsSuperblockMiddle(current));
        if (BlockMeta_IsSimpleBlock(current)) {
            Block_Recycle(&recycleBlocksTo->allocator, current,
                          currentBlockStart, lineMetas);
        } else if (BlockMeta_IsSuperblockStart(current)) {
            size = BlockMeta_SuperblockSize(current);
            LargeAllocator_Sweep(&recycleBlocksTo->largeAllocator, current,
                                 currentBlockStart);
        } else {
            assert(BlockMeta_IsFree(current));
            BlockAllocator_AddFreeBlocks(&blockAllocator, current, 1);
        }
        assert(size > 0);
        current += size;
        currentBlockStart += WORDS_IN_BLOCK * size;
        lineMetas += LINE_COUNT * size;
    }

    if (Heap_shouldGrow(heap)) {
        double growth;
        if (heap->heapSize < EARLY_GROWTH_THRESHOLD) {
            growth = EARLY_GROWTH_RATE;
        } else {
            growth = GROWTH_RATE;
        }
        uint32_t blocks = heap->blockCount * (growth - 1);
        if (Heap_isGrowingPossible(heap, blocks)) {
            Heap_Grow(heap, blocks);
        } else {
            uint32_t remainingGrowth = heap->maxBlockCount - heap->blockCount;
            if (remainingGrowth > 0) {
                Heap_Grow(heap, remainingGrowth);
            }
        }
    }
    BlockAllocator_SweepDone(&blockAllocator);
    MutatorThreads_foreach(mutatorThreads, node) {
        MutatorThread *thread = node->value;
        if (!Allocator_CanInitCursors(&thread->allocator)) {
            Heap_exitWithOutOfMemory("re-init cursors");
        }
        Allocator_InitCursors(&thread->allocator);
    }
}

void Heap_Grow(Heap *heap, uint32_t incrementInBlocks) {
    BlockAllocator_Acquire(&blockAllocator);
    if (!Heap_isGrowingPossible(heap, incrementInBlocks)) {
        Heap_exitWithOutOfMemory("grow heap");
    }
    size_t incrementInBytes = incrementInBlocks * SPACE_USED_PER_BLOCK;

#ifdef DEBUG_PRINT
    printf("Growing small heap by %zu bytes, to %zu bytes\n", incrementInBytes,
           heap->heapSize + incrementInBytes);
    fflush(stdout);
#endif

    word_t *heapEnd = heap->heapEnd;
    heap->heapEnd = heapEnd + incrementInBlocks * WORDS_IN_BLOCK;
    heap->heapSize += incrementInBytes;
    word_t *blockMetaEnd = heap->blockMetaEnd;
    heap->blockMetaEnd =
        (word_t *)(((BlockMeta *)heap->blockMetaEnd) + incrementInBlocks);
    heap->lineMetaEnd +=
        incrementInBlocks * LINE_COUNT * LINE_METADATA_SIZE / WORD_SIZE;

#ifdef _WIN32
    // Windows does not allow for over-committing, therefore we commit the
    // next chunk of memory when growing the heap. Without this, the process
    // might take over all available memory leading to OutOfMemory errors for
    // other processes. Also when using UNLIMITED heap size it might try to
    // commit more memory than is available.
    if (!memoryCommit(heapEnd, incrementInBytes)) {
        Heap_exitWithOutOfMemory("commit memory");
    };
#endif // WIN32

    BlockAllocator_AddFreeBlocks(&blockAllocator, (BlockMeta *)blockMetaEnd,
                                 incrementInBlocks);

    heap->blockCount += incrementInBlocks;

    // immediately add the block to freelists
    BlockAllocator_SweepDone(&blockAllocator);
    BlockAllocator_Release(&blockAllocator);
}
