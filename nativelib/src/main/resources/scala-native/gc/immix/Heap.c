#if defined(SCALANATIVE_GC_IMMIX)

#include "shared/GCHooks.h"
#include <stdlib.h>
#include <stdio.h>
#include "Heap.h"
#include "immix_commix/WriteBarrier.h"
#include "Block.h"
#include "shared/Log.h"
#include "Allocator.h"
#include "Marker.h"
#include "State.h"
#include "StackTrace.h"
#include "immix_commix/Settings.h"
#include "shared/MemoryInfo.h"
#include "shared/MemoryMap.h"
#include "shared/Time.h"
#include "shared/jmx.h"
#include <time.h>
#include "WeakReferences.h"
#include "immix_commix/References.h"
#include "immix_commix/Synchronizer.h"
#include "Generations.h"

/**
 * Allocates the heap struct and initializes it
 */
void Heap_Init(Heap *heap, size_t minHeapSize, size_t maxHeapSize) {
    heap->maxGcTimeRatio =
        Settings_Ratio("GC_TIME_RATIO", SCALANATIVE_GC_CONFIG_TIME_RATIO);
    heap->maxFreeRatio = Settings_Ratio("GC_MAX_FREE_RATIO",
                                        SCALANATIVE_GC_CONFIG_MAX_FREE_RATIO);
    size_t memoryLimit = Heap_getMemoryLimit();

    if (maxHeapSize < MIN_HEAP_SIZE) {
        GC_LOG_ERROR("GC_MAXIMUM_HEAP_SIZE too small to initialize heap. "
                     "Minimum required: %zum",
                     (size_t)(MIN_HEAP_SIZE / 1024 / 1024));
        exit(1);
    }

    if (minHeapSize > memoryLimit) {
        GC_LOG_ERROR("GC_INITIAL_HEAP_SIZE is too large. "
                     "Maximum possible: %zug",
                     memoryLimit / 1024 / 1024 / 1024);
        exit(1);
    }

    if (maxHeapSize < minHeapSize) {
        GC_LOG_ERROR("GC_MAXIMUM_HEAP_SIZE should be at least "
                     "GC_INITIAL_HEAP_SIZE");
        exit(1);
    }

    if (minHeapSize < MIN_HEAP_SIZE) {
        minHeapSize = MIN_HEAP_SIZE;
    }

    if (maxHeapSize == UNLIMITED_HEAP_SIZE) {
        maxHeapSize = memoryLimit;
    }
    maxHeapSize = Heap_limitMaxHeapSize(maxHeapSize);
    // The limit may be below the initial size that was asked for
    if (minHeapSize > maxHeapSize)
        minHeapSize = maxHeapSize;

    uint32_t maxNumberOfBlocks = maxHeapSize / SPACE_USED_PER_BLOCK;
    assert(maxNumberOfBlocks > 1);
    uint32_t initialBlockCount = minHeapSize / SPACE_USED_PER_BLOCK;
    atomic_init(&heap->collectionGeneration, 0);
    heap->maxHeapSize = maxHeapSize;
    heap->blockCount = initialBlockCount;
    heap->maxBlockCount = maxNumberOfBlocks;
    heap->emergencyBlockStart = NULL;
    heap->emergencyBlockMeta = NULL;
    atomic_init(&heap->emergencyBlockClaimed, false);

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
        Heap_bytemapCoverage(maxHeapSize) / ALLOCATION_ALIGNMENT +
        sizeof(Bytemap);
    Bytemap *bytemap =
        (Bytemap *)Heap_mapAndAlign(bytemapSpaceSize, ALLOCATION_ALIGNMENT);
    heap->bytemap = bytemap;

    // Init heap for small objects
    word_t *heapStart = Heap_mapHeapSpace(maxHeapSize);
    if (!heapStart) {
        GC_LOG_ERROR("Failed to allocate heap space, "
                     "requested size=%.2fMB, available memory=%.2fMB. "
                     "Consider setting GC_MAXIMUM_HEAP_SIZE env variable "
                     "to limit maximal heap size",
                     maxHeapSize / (1024.0 * 1024.0),
                     getFreeMemorySize() / (1024.0 * 1024.0));
        exit(1);
    }
    heap->heapSize = initialBlockCount * SPACE_USED_PER_BLOCK;
    heap->heapStart = heapStart;
    heap->heapEnd = heapStart + initialBlockCount * WORDS_IN_BLOCK;

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
#ifdef SCALANATIVE_GC_WRITE_BARRIER
    WriteBarrier_Init(heapStart, maxHeapSize, bytemap->data);
#endif
    Heap_RefillEmergencyBlock(heap);
#ifdef SCALANATIVE_GC_STICKY
    Generations_Init();
#endif
    char *statsFile = Settings_StatsFileName();
    if (statsFile != NULL) {
        heap->stats = malloc(sizeof(Stats));
        if (!Stats_Init(heap->stats, statsFile)) {
            free(heap->stats);
            heap->stats = NULL;
        }
    }
    mutex_init(&heap->lock);
}

bool Heap_BeginEmergencyAllocation(Heap *heap) {
    bool expected = false;
    if (heap->emergencyBlockStart == NULL ||
        !atomic_compare_exchange_strong(&heap->emergencyBlockClaimed, &expected,
                                        true)) {
        return false;
    }

    BlockMeta *blockMeta = (BlockMeta *)heap->emergencyBlockMeta;
    word_t *blockStart = heap->emergencyBlockStart;
    heap->emergencyBlockStart = NULL;
    heap->emergencyBlockMeta = NULL;
    BlockMeta_SetFlag(blockMeta, block_simple);
    BlockMeta_SetFirstFreeLine(blockMeta, LAST_HOLE);
#ifdef SCALANATIVE_GC_STICKY
    // Allocated into, so the next minor collection must sweep it.
    BlockMeta_SetTouched(blockMeta, true);
#endif
    ObjectMeta_ClearBlockAt(Bytemap_Get(heap->bytemap, blockStart));

    Allocator *allocator = &currentMutatorThread->allocator;
    allocator->block = blockMeta;
    allocator->blockStart = blockStart;
    allocator->cursor = blockStart;
    allocator->limit = Block_GetBlockEnd(blockStart);
    atomic_thread_fence(memory_order_release);
    return true;
}

static bool Heap_reserveAllocation(size_t size) {
    if (size >= MIN_BLOCK_SIZE) {
        LargeAllocator *allocator = &currentMutatorThread->largeAllocator;
        return LargeAllocator_ReserveRecycled(allocator, size) ||
               LargeAllocator_Reserve(allocator, size);
    }
    Allocator *allocator = &currentMutatorThread->allocator;
    if (Allocator_Reserve(allocator, size) ||
        Allocator_ReserveRecycled(allocator, size))
        return true;
    // Consume protected whole blocks only after searching recycled runs.
    allocator->canUseReserve = true;
    bool available = Allocator_Reserve(allocator, size);
    allocator->canUseReserve = false;
    return available;
}

/**
 * The GC overhead limit, after the JVM's (UseGCOverheadLimit): an allocation
 * fails when HEAP_OVERHEAD_COLLECTIONS recovery collections in a row each
 * leave less than HEAP_OVERHEAD_FREE_PERCENT of the heap free at its size limit
 * and take more than HEAP_OVERHEAD_GC_PERCENT of the time since the previous
 * collection ended. Low yield alone is not enough: a program can run stably
 * with a nearly full heap, collecting often. Without the limit, the recovery
 * retry succeeds whenever one object fits, and a heap that really is full
 * collects every few allocations without reaching the out-of-memory path.
 */
#define HEAP_OVERHEAD_FREE_PERCENT 2
#define HEAP_OVERHEAD_GC_PERCENT 98
#define HEAP_OVERHEAD_COLLECTIONS 5

uint64_t Heap_freeLines(void) {
    return (uint64_t)atomic_load(&blockAllocator.freeBlockCount) * LINE_COUNT +
           Block_recycledFreeLines + LargeAllocator_sweptFreeBytes / LINE_SIZE;
}

/** At the size limit with under HEAP_OVERHEAD_FREE_PERCENT free. */
static bool Heap_lowYield(Heap *heap) {
    if (heap->blockCount < heap->maxBlockCount)
        return false;
    uint64_t freeLines = Heap_freeLines();
    uint64_t totalLines = (uint64_t)heap->blockCount * LINE_COUNT;
    return freeLines * 100 < totalLines * HEAP_OVERHEAD_FREE_PERCENT;
}

static bool Heap_overheadLimitReached(Heap *heap, uint64_t start_ns,
                                      bool lowYield) {
    uint64_t now = Time_current_nanos();
    uint64_t gcNs = now - start_ns;
    uint64_t sinceLast =
        heap->lastCollectionEndNs == 0 ? gcNs : now - heap->lastCollectionEndNs;
    bool gcBound = gcNs * 100 >= sinceLast * HEAP_OVERHEAD_GC_PERCENT;
    if (gcBound && lowYield)
        heap->overheadStreak++;
    else
        heap->overheadStreak = 0;
    if (lowYield)
        GC_LOG_DEBUG("Overhead limit: low-yield recovery collection, "
                     "gc-bound: %s, streak: %" PRIu32,
                     gcBound ? "yes" : "no", heap->overheadStreak);
    if (heap->overheadStreak < HEAP_OVERHEAD_COLLECTIONS)
        return false;
    // The error is about to be thrown; a program that catches it starts afresh.
    heap->overheadStreak = 0;
    return true;
}

/**
 * Marks and sweeps. A full collection traces the whole heap; a minor one
 * (Sticky Immix only) traces and frees only young objects. Adds the time
 * spent marking and processing references to the totals, if asked.
 */
static void Heap_traceAndSweep(Heap *heap, Stack *stack, bool full,
                               uint64_t *markNs, uint64_t *nullifyNs) {
    uint64_t startNs = markNs != NULL ? Time_current_nanos() : 0;
#ifdef SCALANATIVE_GC_WRITE_BARRIER_VERIFY
    WriteBarrier_VerifyBeforeCollection();
#endif
#ifdef SCALANATIVE_GC_STICKY
    if (full)
        Generations_PrepareFull(heap);
#endif
    // Mark cycles tell which objects were not reached: only a full collection
    // reaches the old ones, a minor one marks within the current cycle.
    if (full)
        GCHooks_markStarted();
    Marker_MarkRoots(heap, stack, !full);
    if (full)
        GCHooks_markCompleted();
    uint64_t nullifyStartNs = markNs != NULL ? Time_current_nanos() : 0;
    WeakReferences_Nullify();
    if (markNs != NULL) {
        *markNs += nullifyStartNs - startNs;
        *nullifyNs += Time_current_nanos() - nullifyStartNs;
    }
    Heap_Recycle(heap, full);
#ifdef SCALANATIVE_GC_STICKY
    Generations_EndCollection(heap, full);
#endif
#ifdef SCALANATIVE_GC_WRITE_BARRIER_VERIFY
    WriteBarrier_VerifyAfterCollection();
#endif
}

static bool Heap_collect(Heap *heap, Stack *stack, size_t allocationSize,
                         uint64_t generation, bool clearSoftReferences) {
    SpillCalleeSavedRegisters();
    // Stopping the other mutators is part of the pause.
    uint64_t request_ns = Time_current_nanos();
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    if (!(allocationSize == 0 ? Synchronizer_acquire()
                              : Synchronizer_acquireForAllocation(
                                    &heap->collectionGeneration, generation)))
        // Another collector may not have started stopping threads yet. Retry
        // allocation; this is not evidence of exhausted post-collection space.
        return true;
#else
    MutatorThread_switchState(currentMutatorThread,
                              GC_MutatorThreadState_Unmanaged);
#endif
    uint64_t start_ns, mark_ns = 0, nullify_ns = 0, end_ns;
    Stats *stats = heap->stats;
    bool full = true;
#ifdef SCALANATIVE_GC_STICKY
    // Soft referents are cleared to avoid an OutOfMemoryError: the old ones
    // are freed only by a full collection.
    full = clearSoftReferences ||
           Generations_ShouldCollectFull(allocationSize == 0);
#endif
    StatsCollectionKind kind =
        full ? STATS_COLLECTION_FULL : STATS_COLLECTION_MINOR;
    GC_LOG_INFO("GC %s collection started", full ? "full" : "minor");
    start_ns = Time_current_nanos();
    References_StartCollection(clearSoftReferences);
    heap->collectionRequestNs = request_ns;
    heap->explicitCollection = allocationSize == 0;
    Heap_traceAndSweep(heap, stack, full, stats != NULL ? &mark_ns : NULL,
                       &nullify_ns);
    bool available = true;
    // An explicit collection is not an allocation that could not proceed: it
    // ends any run of qualifying recovery collections, so that space it frees
    // is never followed by an error counted partly before it.
    if (allocationSize == 0)
        heap->overheadStreak = 0;
    if (allocationSize != 0) {
        // Yield is judged on the heap as the collection left it: reserving
        // below takes a whole superblock out of the free count for a large
        // object, and counting that would make a productive collection look
        // low-yield.
        bool lowYield = full && Heap_lowYield(heap);
        available = Heap_reserveAllocation(allocationSize);
#ifdef SCALANATIVE_GC_STICKY
        if (!available && !full) {
            // Only young objects were freed; old garbage may hold the space.
            // Growing is decided after a full collection.
            GC_LOG_DEBUG("Minor collection did not free %zu bytes, "
                         "collecting the whole heap",
                         allocationSize);
            full = true;
            kind = STATS_COLLECTION_MINOR_THEN_FULL;
            Heap_traceAndSweep(heap, stack, true,
                               stats != NULL ? &mark_ns : NULL, &nullify_ns);
            lowYield = Heap_lowYield(heap);
            available = Heap_reserveAllocation(allocationSize);
        }
#endif
        if (!available) {
            uint32_t blocks =
                (allocationSize + BLOCK_TOTAL_SIZE - 1ULL) / BLOCK_TOTAL_SIZE;
            GC_LOG_DEBUG(
                "Allocation recovery: size=%zu, generation=%" PRIuFAST64,
                allocationSize, atomic_load(&heap->collectionGeneration));
            if (Heap_TryGrow(heap, blocks)) {
                lowYield = Heap_lowYield(heap);
                available = Heap_reserveAllocation(allocationSize);
            }
        }
        // Judged after reservation and growth, so that the time counted as
        // collecting covers the whole recovery. The limit fails the
        // allocation even when one object would fit: near the cap a
        // reservation almost always succeeds, and failing only when it does
        // not would collect every few allocations without end.
        if (Heap_overheadLimitReached(heap, start_ns, lowYield))
            available = false;
    }
    atomic_fetch_add_explicit(&heap->collectionGeneration, 1,
                              memory_order_release);
    end_ns = Time_current_nanos();
    heap->lastCollectionEndNs = end_ns;
    jmx_stats_record_collection(start_ns, end_ns);
    if (stats != NULL) {
        // The phases of a minor collection followed by a full one add up;
        // the rest of the pause counts as sweeping.
        Stats_RecordCollection(stats, request_ns, start_ns, start_ns + mark_ns,
                               start_ns + mark_ns + nullify_ns, end_ns, kind);
    }
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    Synchronizer_release();
#else
    MutatorThread_switchState(currentMutatorThread,
                              GC_MutatorThreadState_Managed);
#endif
    WeakReferences_InvokeGCFinishedCallback();
    GC_LOG_INFO("GC collection finished in %" PRIu64 "ms",
                (end_ns - start_ns) / 1000000);
    return available;
}

void Heap_Collect(Heap *heap, Stack *stack, bool clearSoftReferences) {
    Heap_collect(heap, stack, 0, 0, clearSoftReferences);
}

bool Heap_RecoverAllocation(Heap *heap, size_t size, uint64_t generation) {
    if (atomic_load_explicit(&heap->collectionGeneration,
                             memory_order_acquire) != generation)
        return true;
    if (Heap_collect(heap, &stack, size, generation, false))
        return true;
    if (!References_SoftFound())
        return false;
    // Before an OutOfMemoryError, collect once more with soft referents cleared
    uint64_t next =
        atomic_load_explicit(&heap->collectionGeneration, memory_order_acquire);
    return Heap_collect(heap, &stack, size, next, true);
}

// Collections took too much of the time since the previous one ended:
// growing makes them rarer. A heap that stays mostly free grows for this only
// while small, or a program keeping many small objects alive would grow it
// without bound (#2436). System.gc() does not count (#5052).
static bool Heap_gcTooFrequent(Heap *heap, uint64_t freeLines) {
    uint64_t now = Time_current_nanos();
    uint64_t totalLines = (uint64_t)heap->blockCount * LINE_COUNT;
    return !heap->explicitCollection && heap->lastCollectionEndNs != 0 &&
           now - heap->collectionRequestNs >=
               heap->maxGcTimeRatio * (now - heap->lastCollectionEndNs) &&
           (heap->heapSize < EARLY_GROWTH_THRESHOLD ||
            freeLines < heap->maxFreeRatio * totalLines);
}

bool Heap_shouldGrow(Heap *heap) {
    uint32_t freeBlockCount = (uint32_t)blockAllocator.freeBlockCount;
    uint32_t blockCount = heap->blockCount;
    uint32_t recycledBlockCount = 0;
    MutatorThreads_foreach(mutatorThreads, node) {
        recycledBlockCount += node->value->allocator.recycledBlockCount;
    }
    uint32_t unavailableBlockCount =
        blockCount - (freeBlockCount + recycledBlockCount);

    GC_LOG_DEBUG("Block count: %u, Unavailable: %u, Free: %u, Recycled: %u",
                 blockCount, unavailableBlockCount, freeBlockCount,
                 recycledBlockCount);
    uint64_t usedLines = (uint64_t)(blockCount - freeBlockCount) * LINE_COUNT -
                         Block_recycledFreeLines;
    GC_LOG_DEBUG("Used lines: %" PRIu64 " of %" PRIu64 " (%" PRIu64 " MB)",
                 usedLines, (uint64_t)blockCount * LINE_COUNT,
                 (usedLines * LINE_SIZE) >> 20);

    bool gcTooFrequent =
        Heap_gcTooFrequent(heap, (uint64_t)freeBlockCount * LINE_COUNT);

    return gcTooFrequent || freeBlockCount * 2 < blockCount ||
           (4 * unavailableBlockCount > blockCount &&
            20 * freeBlockCount < 13 * blockCount);
}

#ifdef SCALANATIVE_MULTITHREADING_ENABLED
/** Whether `thread` took any space since the last collection. */
static bool Heap_allocatedSinceCollection(MutatorThread *thread) {
    return thread->allocator.blocksTaken != 0 ||
           thread->largeAllocator.blocksTaken != 0;
}

/**
 * The threads that receive partly free blocks in this collection: the
 * collecting thread, then every thread that took or asked for space since the
 * last one. A thread that did neither (parked, sleeping, waiting for work)
 * would hold its share until the next collection, out of reach of the threads
 * that allocate, so it is left out. Thread state cannot tell the two apart:
 * every stopped thread is Unmanaged by now. Built once per collection, so
 * handing out a block stays O(1) however many threads are idle. Kept between
 * collections and grown as needed; if it cannot grow, every block goes to the
 * collecting thread.
 */
static MutatorThread **recycleTargets = NULL;
static size_t recycleTargetsCapacity = 0;

static size_t Heap_collectRecycleTargets(void) {
    size_t threads = 0;
    MutatorThreads_foreach(mutatorThreads, node) { threads++; }
    if (threads + 1 > recycleTargetsCapacity) {
        MutatorThread **grown =
            realloc(recycleTargets, (threads + 1) * sizeof(MutatorThread *));
        if (grown == NULL) {
            if (recycleTargets == NULL)
                return 0;
            recycleTargets[0] = currentMutatorThread;
            return 1;
        }
        recycleTargets = grown;
        recycleTargetsCapacity = threads + 1;
    }
    size_t count = 0;
    recycleTargets[count++] = currentMutatorThread;
    MutatorThreads_foreach(mutatorThreads, node) {
        MutatorThread *thread = node->value;
        if (thread != currentMutatorThread &&
            Heap_allocatedSinceCollection(thread))
            recycleTargets[count++] = thread;
    }
    return count;
}
#endif

void Heap_Recycle(Heap *heap, bool full) {
    Block_recycledFreeLines = 0;
    LargeAllocator_sweptFreeBytes = 0;
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

#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    size_t targetCount = Heap_collectRecycleTargets();
    size_t targetIndex = 0;
#define NextMutatorThread()                                                    \
    (targetCount == 0 ? currentMutatorThread                                   \
                      : recycleTargets[targetIndex++ % targetCount])
#else
    MutatorThread *mainThread = currentMutatorThread;
#define NextMutatorThread() mainThread
#endif

    while ((word_t *)current < end) {
        int size = 1;

        assert(!BlockMeta_IsSuperblockMiddle(current));
        if (BlockMeta_IsReserved(current)) {
            // Keep the empty reserve out of the allocator while it is held.
            BlockAllocator_SweepDone(&blockAllocator);
        } else if (BlockMeta_IsSimpleBlock(current)) {
            MutatorThread *recycleBlocksTo = NextMutatorThread();
            Block_Recycle(&recycleBlocksTo->allocator, current,
                          currentBlockStart, lineMetas, full);
        } else if (BlockMeta_IsSuperblockStart(current)) {
            size = BlockMeta_SuperblockSize(current);
            MutatorThread *recycleBlocksTo = NextMutatorThread();
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

    // A new cycle: who allocates in it decides the next distribution.
    MutatorThreads_foreach(mutatorThreads, node) {
        node->value->allocator.blocksTaken = 0;
        node->value->largeAllocator.blocksTaken = 0;
    }

#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    atomic_thread_fence(memory_order_seq_cst);
#endif
    // Old garbage survives a minor collection, so after one the heap grows
    // only when collections take too much of the time, judged by all the
    // free lines: free blocks are scarce between full collections.
    bool grow = full ? Heap_shouldGrow(heap)
                     : Heap_gcTooFrequent(heap, Heap_freeLines());
    if (grow) {
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
    Heap_RefillEmergencyBlock(heap);
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    atomic_thread_fence(memory_order_seq_cst);
#endif
}

bool Heap_TryGrow(Heap *heap, uint32_t incrementInBlocks) {
    BlockAllocator_Acquire(&blockAllocator);
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    atomic_thread_fence(memory_order_seq_cst);
#endif
    if (!Heap_isGrowingPossible(heap, incrementInBlocks)) {
        BlockAllocator_Release(&blockAllocator);
        return false;
    }
    size_t incrementInBytes = incrementInBlocks * SPACE_USED_PER_BLOCK;

    GC_LOG_INFO("Growing heap by %zu bytes, to %zu bytes", incrementInBytes,
                heap->heapSize + incrementInBytes);

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
        Heap_exitWithOutOfMemory("grow heap, commit memmory");
    };
#endif // WIN32

    BlockAllocator_AddFreeBlocks(&blockAllocator, (BlockMeta *)blockMetaEnd,
                                 incrementInBlocks);

    heap->blockCount += incrementInBlocks;

    // immediately add the block to freelists
    BlockAllocator_SweepDone(&blockAllocator);
    BlockAllocator_Release(&blockAllocator);
    return true;
}

#endif
