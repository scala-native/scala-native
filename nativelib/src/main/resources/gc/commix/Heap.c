#include <stdlib.h>
#include <sys/mman.h>
#include <stdio.h>
#include "Heap.h"
#include "Log.h"
#include "Allocator.h"
#include "Marker.h"
#include "State.h"
#include "utils/MathUtils.h"
#include "StackTrace.h"
#include "Settings.h"
#include "Memory.h"
#include "GCThread.h"
#include "Sweeper.h"
#include <memory.h>
#include <time.h>
#include <inttypes.h>
#include <sched.h>

// Allow read and write
#define HEAP_MEM_PROT (PROT_READ | PROT_WRITE)
// Map private anonymous memory, and prevent from reserving swap
#define HEAP_MEM_FLAGS (MAP_NORESERVE | MAP_PRIVATE | MAP_ANONYMOUS)
// Map anonymous memory (not a file)
#define HEAP_MEM_FD -1
#define HEAP_MEM_FD_OFFSET 0

void Heap_exitWithOutOfMemory() {
    printf("Out of heap space\n");
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
void Heap_Init(Heap *heap, size_t minHeapSize, size_t maxHeapSize) {
    size_t memoryLimit = Heap_getMemoryLimit();

    if (maxHeapSize < MIN_HEAP_SIZE) {
        fprintf(stderr,
                "SCALANATIVE_MAX_HEAP_SIZE too small to initialize heap.\n");
        fprintf(stderr, "Minimum required: %zum \n",
                MIN_HEAP_SIZE / 1024 / 1024);
        fflush(stderr);
        exit(1);
    }

    if (minHeapSize > memoryLimit) {
        fprintf(stderr, "SCALANATIVE_MIN_HEAP_SIZE is too large.\n");
        fprintf(stderr, "Maximum possible: %zug \n",
                memoryLimit / 1024 / 1024 / 1024);
        fflush(stderr);
        exit(1);
    }

    if (maxHeapSize < minHeapSize) {
        fprintf(stderr, "SCALANATIVE_MAX_HEAP_SIZE should be at least "
                        "SCALANATIVE_MIN_HEAP_SIZE\n");
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

    word_t *heapStart = Heap_mapAndAlign(maxHeapSize, BLOCK_TOTAL_SIZE);

    BlockAllocator_Init(&blockAllocator, blockMetaStart, initialBlockCount);
    GreyList_Init(&heap->mark.empty);
    GreyList_Init(&heap->mark.full);
    uint32_t greyPacketCount = (uint32_t)(maxHeapSize * GREY_PACKET_RATIO / GREY_PACKET_SIZE);
    heap->mark.total = greyPacketCount;
    word_t* greyPacketsStart = Heap_mapAndAlign(greyPacketCount * sizeof(GreyPacket), WORD_SIZE);
    heap->greyPacketsStart = greyPacketsStart;
    GreyList_PushAll(&heap->mark.empty, greyPacketsStart, (GreyPacket *) greyPacketsStart, greyPacketCount);

    // reserve space for bytemap
    Bytemap *bytemap = (Bytemap *)Heap_mapAndAlign(
        maxHeapSize / ALLOCATION_ALIGNMENT + sizeof(Bytemap),
        ALLOCATION_ALIGNMENT);
    heap->bytemap = bytemap;

    // Init heap for small objects
    heap->heapSize = minHeapSize;
    heap->heapStart = heapStart;
    heap->heapEnd = heapStart + minHeapSize / WORD_SIZE;
    heap->sweep.cursor = initialBlockCount;
    heap->lazySweep.cursorDone = initialBlockCount;
    heap->sweep.limit = initialBlockCount;
    heap->sweep.coalesceDone = initialBlockCount;
    heap->sweep.postSweepDone = true;
    Bytemap_Init(bytemap, heapStart, maxHeapSize);
    Allocator_Init(&allocator, &blockAllocator, bytemap, blockMetaStart,
                   heapStart);

    LargeAllocator_Init(&largeAllocator, &blockAllocator, bytemap,
                        blockMetaStart, heapStart);

    // Init all GCThreads
    sem_init(&heap->gcThreads.start, 0, 0);
    sem_init(&heap->gcThreads.start0, 0, 0);

    // Init stats if enabled.
    // This must done before initializing other threads.
#ifdef ENABLE_GC_STATS
    char *statsFile = Settings_StatsFileName();
    if (statsFile != NULL) {
        heap->stats = malloc(sizeof(Stats));
        Stats_Init(heap->stats, statsFile, MUTATOR_THREAD_ID);
    } else {
#endif
        heap->stats = NULL;
#ifdef ENABLE_GC_STATS
    }
#endif


    int gcThreadCount = Settings_GCThreadCount();
    heap->gcThreads.count = gcThreadCount;
    heap->gcThreads.phase = gc_idle;
    GCThread *gcThreads = (GCThread *)malloc(sizeof(GCThread) * gcThreadCount);
    heap->gcThreads.all = (void *)gcThreads;
    for (int i = 0; i < gcThreadCount; i++) {
        Stats *stats;
#ifdef ENABLE_GC_STATS
        if (statsFile != NULL) {
            int len = strlen(statsFile) + 5;
            char *threadSpecificFile = (char *) malloc(len);
            snprintf(threadSpecificFile, len, "%s.t%d", statsFile, i);
            stats = malloc(sizeof(Stats));
            Stats_Init(stats, threadSpecificFile, (uint8_t)i);
        } else {
#endif
            stats = NULL;
#ifdef ENABLE_GC_STATS
        }
#endif
        GCThread_Init(&gcThreads[i], i, heap, stats);
    }

    heap->mark.lastEnd_ns = scalanative_nano_time();

    pthread_mutex_init(&heap->sweep.growMutex, NULL);
}

word_t *Heap_AllocLarge(Heap *heap, uint32_t size) {

    assert(size % ALLOCATION_ALIGNMENT == 0);
    assert(size >= MIN_BLOCK_SIZE);

    Object *object = LargeAllocator_GetBlock(&largeAllocator, size);
    if (object != NULL) {
done:
        assert(object != NULL);
        assert(Heap_IsWordInHeap(heap, (word_t *)object));
        return (word_t *)object;
}

    object = Sweeper_LazySweepLarge(heap, size);
    if (object != NULL)
        goto done;

    Heap_Collect(heap);

    object = LargeAllocator_GetBlock(&largeAllocator, size);
    if (object != NULL)
        goto done;

    object = Sweeper_LazySweepLarge(heap, size);
    if (object != NULL)
        goto done;

    size_t increment = MathUtils_DivAndRoundUp(size, BLOCK_TOTAL_SIZE);
    uint32_t pow2increment = 1U << MathUtils_Log2Ceil(increment);
    Heap_Grow(heap, pow2increment);

    object = LargeAllocator_GetBlock(&largeAllocator, size);

    goto done;
}

NOINLINE word_t *Heap_allocSmallSlow(Heap *heap, uint32_t size) {
    Object *object = (Object *) Allocator_Alloc(&allocator, size);

    if (object != NULL) {
done:
    assert(Heap_IsWordInHeap(heap, (word_t *)object));
    assert(object != NULL);
    ObjectMeta *objectMeta = Bytemap_Get(allocator.bytemap, (word_t *)object);
#ifdef DEBUG_ASSERT
    ObjectMeta_AssertIsValidAllocation(objectMeta, size);
#endif
    ObjectMeta_SetAllocated(objectMeta);
    return (word_t *)object;
    }
    object = Sweeper_LazySweep(heap, size);

    if (object != NULL)
        goto done;

    Heap_Collect(heap);
    object = (Object *) Allocator_Alloc(&allocator, size);

    if (object != NULL)
        goto done;

    object = Sweeper_LazySweep(heap, size);

    if (object != NULL)
        goto done;

    // A small object can always fit in a single free block
    // because it is no larger than 8K while the block is 32K.
    Heap_Grow(heap, 1);
    object = (Object *) Allocator_Alloc(&allocator, size);

    goto done;
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
#ifdef DEBUG_ASSERT
    ObjectMeta_AssertIsValidAllocation(objectMeta, size);
#endif
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

#ifdef DEBUG_ASSERT
void Heap_clearIsSwept(Heap *heap) {
    BlockMeta *current = (BlockMeta *)heap->blockMetaStart;
    BlockMeta *limit = (BlockMeta *)heap->blockMetaEnd;
    while (current < limit) {
        current->debugFlag = dbg_must_sweep;
        current++;
    }
}

void Heap_assertIsConsistent(Heap *heap) {
    BlockMeta *current = (BlockMeta *)heap->blockMetaStart;
    LineMeta *lineMetas = (LineMeta *)heap->lineMetaStart;
    BlockMeta *limit = (BlockMeta *)heap->blockMetaEnd;
    ObjectMeta *currentBlockStart = Bytemap_Get(heap->bytemap, heap->heapStart);
    while (current < limit) {
        assert(!BlockMeta_IsCoalesceMe(current));
        assert(!BlockMeta_IsSuperblockStartMe(current));
        assert(!BlockMeta_IsSuperblockTail(current));
        assert(!BlockMeta_IsMarked(current));

        int size = 1;
        if (BlockMeta_IsSuperblockStart(current)) {
            size = BlockMeta_SuperblockSize(current);
        }
        BlockMeta *next = current + size;
        LineMeta *nextLineMetas = lineMetas + LINE_COUNT * size;
        ObjectMeta *nextBlockStart =
            currentBlockStart +
            (WORDS_IN_BLOCK / ALLOCATION_ALIGNMENT_WORDS) * size;

        for (LineMeta *line = lineMetas; line < nextLineMetas; line++) {
            assert(!Line_IsMarked(line));
        }
        for (ObjectMeta *object = currentBlockStart; object < nextBlockStart;
             object++) {
            assert(!ObjectMeta_IsMarked(object));
        }

        current = next;
        lineMetas = nextLineMetas;
        currentBlockStart = nextBlockStart;
    }
    assert(current == limit);
}
#endif

void Heap_Collect(Heap *heap) {
#ifdef ENABLE_GC_STATS
    Stats *stats = heap->stats;
    if (stats != NULL) {
        stats->collection_start_ns = scalanative_nano_time();
    }
#else
    Stats *stats = NULL;
#endif
    assert(Sweeper_IsSweepDone(heap));
#ifdef DEBUG_ASSERT
    Heap_clearIsSwept(heap);
    Heap_assertIsConsistent(heap);
#endif
    heap->mark.lastEnd_ns = heap->mark.currentEnd_ns;
    heap->mark.currentStart_ns = scalanative_nano_time();
    Marker_MarkRoots(heap, stats);
    heap->gcThreads.phase = gc_mark;
    // make sure the gc phase is propagated
    atomic_thread_fence(memory_order_release);
    int gcThreadCount = heap->gcThreads.count;
    GCThread_Wake(heap, gcThreadCount);
    while (!Marker_IsMarkDone(heap)) {
        Marker_Mark(heap, stats);
    }
    heap->gcThreads.phase = gc_idle;
    heap->mark.currentEnd_ns = scalanative_nano_time();
#ifdef ENABLE_GC_STATS
    if (stats != NULL) {
        Stats_RecordEvent(stats, event_mark, heap->mark.currentStart_ns,
                          heap->mark.currentEnd_ns);
    }
#endif
    Heap_Recycle(heap);
}

bool Heap_shouldGrow(Heap *heap) {
    uint32_t freeBlockCount = (uint32_t)blockAllocator.freeBlockCount;
    uint32_t blockCount = heap->blockCount;
    uint32_t recycledBlockCount = (uint32_t)allocator.recycledBlockCount;
    uint32_t unavailableBlockCount =
        blockCount - (freeBlockCount + recycledBlockCount);

#ifdef DEBUG_PRINT
    printf("\n\nBlock count: %" PRIu32 "\n", blockCount);
    printf("Unavailable: %" PRIu32 "\n", unavailableBlockCount);
    printf("Free: %" PRIu32 "\n", freeBlockCount);
    printf("Recycled: %" PRIu32 "\n", recycledBlockCount);
    fflush(stdout);
#endif

    uint64_t timeInMark = heap->mark.currentEnd_ns - heap->mark.currentStart_ns;
    uint64_t timeTotal = heap->mark.currentEnd_ns - heap->mark.lastEnd_ns;

    return timeInMark >= GROWTH_MARK_FRACTION * timeTotal || freeBlockCount * 2 < blockCount ||
           4 * unavailableBlockCount > blockCount;
}

void Heap_Recycle(Heap *heap) {
    Allocator_Clear(&allocator);
    LargeAllocator_Clear(&largeAllocator);
    BlockAllocator_Clear(&blockAllocator);

    // all the marking changes should be visible to all threads by now
    atomic_thread_fence(memory_order_seq_cst);

    // before changing the cursor and limit values, makes sure no gc threads are
    // running
    GCThread_JoinAll(heap);

    heap->sweep.cursor = 0;
    uint32_t blockCount = heap->blockCount;
    heap->sweep.limit = blockCount;
    heap->lazySweep.cursorDone = 0;
    // mark as unitialized
    heap->lazySweep.lastActivity = BlockRange_Pack(2, 0);
    heap->lazySweep.lastActivityObserved = BlockRange_Pack(2, 0);
    heap->sweep.coalesceDone = 0;
    heap->sweep.postSweepDone = false;

    heap->gcThreads.phase = gc_sweep;
    // make sure all running parameters are propagated
    atomic_thread_fence(memory_order_release);
    // determine how many threads need to start
    int gcThreadCount = heap->gcThreads.count;
    int numberOfBatches = blockCount / SWEEP_BATCH_SIZE;
    int threadsToStart = numberOfBatches / MIN_SWEEP_BATCHES_PER_THREAD;
    if (threadsToStart <= 0) {
        threadsToStart = 1;
    }
    if (threadsToStart > gcThreadCount){
        threadsToStart = gcThreadCount;
    }
    GCThread_Wake(heap, threadsToStart);
}

void Heap_GrowIfNeeded(Heap *heap) {
    // make all writes to block counts visible
    atomic_thread_fence(memory_order_seq_cst);
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
    if (!Allocator_CanInitCursors(&allocator)) {
        Heap_exitWithOutOfMemory();
    }
}

void Heap_Grow(Heap *heap, uint32_t incrementInBlocks) {
    pthread_mutex_lock(&heap->sweep.growMutex);
    if (!Heap_isGrowingPossible(heap, incrementInBlocks)) {
        Heap_exitWithOutOfMemory();
    }

#ifdef DEBUG_PRINT
    printf("Growing small heap by %zu bytes, to %zu bytes\n",
           incrementInBlocks * SPACE_USED_PER_BLOCK,
           heap->heapSize + incrementInBlocks * SPACE_USED_PER_BLOCK);
    fflush(stdout);
#endif

    word_t *heapEnd = heap->heapEnd;
    heap->heapEnd = heapEnd + incrementInBlocks * BLOCK_TOTAL_SIZE;
    heap->heapSize += incrementInBlocks * SPACE_USED_PER_BLOCK;
    word_t *blockMetaEnd = heap->blockMetaEnd;
    heap->blockMetaEnd =
        (word_t *)(((BlockMeta *)heap->blockMetaEnd) + incrementInBlocks);
    heap->lineMetaEnd +=
        incrementInBlocks * LINE_COUNT * LINE_METADATA_SIZE / WORD_SIZE;

#ifdef DEBUG_ASSERT
    BlockMeta *end = (BlockMeta *)blockMetaEnd;
    for (BlockMeta *block = end; block < end + incrementInBlocks; block++) {
        block->debugFlag = dbg_free;
    }
#endif

    BlockAllocator_AddFreeBlocks(&blockAllocator, (BlockMeta *)blockMetaEnd,
                                 incrementInBlocks);

    heap->blockCount += incrementInBlocks;
    pthread_mutex_unlock(&heap->sweep.growMutex);
}
