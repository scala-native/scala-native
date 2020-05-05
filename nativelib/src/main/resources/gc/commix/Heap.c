#include <stdlib.h>
#include <sys/mman.h>
#include <stdio.h>
#include "Heap.h"
#include "Log.h"
#include "Allocator.h"
#include "LargeAllocator.h"
#include "Marker.h"
#include "State.h"
#include "utils/MathUtils.h"
#include "StackTrace.h"
#include "Settings.h"
#include "Memory.h"
#include "GCThread.h"
#include "Sweeper.h"
#include "Phase.h"
#include <memory.h>
#include <time.h>
#include <inttypes.h>

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

#ifdef ENABLE_GC_STATS
INLINE Stats *Heap_createMutatorStats(void) {
    char *statsFile = Settings_StatsFileName();
    if (statsFile != NULL) {
        Stats *stats = malloc(sizeof(Stats));
        Stats_Init(stats, statsFile, MUTATOR_THREAD_ID);
        return stats;
    } else {
        return NULL;
    }
}

INLINE Stats *Heap_createStatsForThread(int id) {
    char *statsFile = Settings_StatsFileName();
    if (statsFile != NULL) {
        int len = strlen(statsFile) + 5;
        char *threadSpecificFile = (char *)malloc(len);
        snprintf(threadSpecificFile, len, "%s.t%d", statsFile, id);
        Stats *stats = malloc(sizeof(Stats));
        Stats_Init(stats, threadSpecificFile, (uint8_t)id);
        return stats;
    } else {
        return NULL;
    }
}

#endif

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
    heap->maxMarkTimeRatio = Settings_MaxMarkTimeRatio();
    heap->minFreeRatio = Settings_MinFreeRatio();

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
    uint32_t greyPacketCount =
        (uint32_t)(maxHeapSize * GREY_PACKET_RATIO / GREY_PACKET_SIZE);
    heap->mark.total = greyPacketCount;
    word_t *greyPacketsStart =
        Heap_mapAndAlign(greyPacketCount * sizeof(GreyPacket), WORD_SIZE);
    heap->greyPacketsStart = greyPacketsStart;
    GreyList_PushAll(&heap->mark.empty, greyPacketsStart,
                     (GreyPacket *)greyPacketsStart, greyPacketCount);

    // reserve space for bytemap
    Bytemap *bytemap = (Bytemap *)Heap_mapAndAlign(
        maxHeapSize / ALLOCATION_ALIGNMENT + sizeof(Bytemap),
        ALLOCATION_ALIGNMENT);
    heap->bytemap = bytemap;

    // Init heap for small objects
    heap->heapSize = minHeapSize;
    heap->heapStart = heapStart;
    heap->heapEnd = heapStart + minHeapSize / WORD_SIZE;

    Phase_Init(heap, initialBlockCount);

    Bytemap_Init(bytemap, heapStart, maxHeapSize);
    Allocator_Init(&allocator, &blockAllocator, bytemap, blockMetaStart,
                   heapStart);

    LargeAllocator_Init(&largeAllocator, &blockAllocator, bytemap,
                        blockMetaStart, heapStart);

    // Init all GCThreads
    // Init stats if enabled.
    // This must done before initializing other threads.
    heap->stats = Stats_OrNull(Heap_createMutatorStats());

    int gcThreadCount = Settings_GCThreadCount();
    heap->gcThreads.count = gcThreadCount;
    Phase_Set(heap, gc_idle);
    GCThread *gcThreads = (GCThread *)malloc(sizeof(GCThread) * gcThreadCount);
    heap->gcThreads.all = (void *)gcThreads;
    for (int i = 0; i < gcThreadCount; i++) {
        Stats *stats = Stats_OrNull(Heap_createStatsForThread(i));
        GCThread_Init(&gcThreads[i], i, heap, stats);
    }

    heap->mark.lastEnd_ns = scalanative_nano_time();

    pthread_mutex_init(&heap->sweep.growMutex, NULL);
}

void Heap_Collect(Heap *heap) {
    Stats *stats = Stats_OrNull(heap->stats);
    Stats_CollectionStarted(stats);
    assert(Sweeper_IsSweepDone(heap));
#ifdef DEBUG_ASSERT
    Sweeper_ClearIsSwept(heap);
    Sweeper_AssertIsConsistent(heap);
#endif
    Phase_StartMark(heap);
    Marker_MarkRoots(heap, stats);
    Marker_MarkUntilDone(heap, stats);
    Phase_MarkDone(heap);
    Stats_RecordEvent(stats, event_mark, heap->mark.currentStart_ns,
                      heap->mark.currentEnd_ns);
    Phase_StartSweep(heap);
}

bool Heap_shouldGrow(Heap *heap) {
    uint32_t freeBlockCount = (uint32_t)blockAllocator.freeBlockCount;
    uint32_t blockCount = heap->blockCount;
    uint32_t recycledBlockCount = (uint32_t)allocator.recycledBlockCount;
    uint32_t unavailableBlockCount =
        blockCount - (freeBlockCount + recycledBlockCount);

#ifdef DEBUG_PRINT
    printf("\n\n Max mark time ratio: %lf \n", heap->maxMarkTimeRatio);
    printf("Min free ratio: %lf \n", heap->minFreeRatio);
    printf("Block count: %" PRIu32 "\n", blockCount);
    printf("Unavailable: %" PRIu32 "\n", unavailableBlockCount);
    printf("Free: %" PRIu32 "\n", freeBlockCount);
    printf("Recycled: %" PRIu32 "\n", recycledBlockCount);
    fflush(stdout);
#endif

    uint64_t timeInMark = heap->mark.currentEnd_ns - heap->mark.currentStart_ns;
    uint64_t timeTotal = heap->mark.currentEnd_ns - heap->mark.lastEnd_ns;

    return timeInMark >= heap->maxMarkTimeRatio * timeTotal ||
           freeBlockCount < heap->minFreeRatio * blockCount ||
           unavailableBlockCount > blockCount * MAX_UNAVAILABLE_RATIO;
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
    heap->heapEnd = heapEnd + incrementInBlocks * WORDS_IN_BLOCK;
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
