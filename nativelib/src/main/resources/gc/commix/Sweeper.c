#include "Sweeper.h"
#include "Stats.h"
#include "State.h"
#include "GCThread.h"
#include "GCTypes.h"
#include <sched.h>

/*

Sweeper implements concurrent sweeping by coordinating lazy sweeper on the
mutator thread with one or more concurrent sweepers on GC threads.

After the mark is done the concurrent sweepers are started.
Each takes batch of SWEEP_BATCH_SIZE blocks using the `heap->sweep.cursor`.
If the mutator thread fails to allocate if will sweep a batch of
LAZY_SWEEP_MIN_BATCH blocks. This will speed up sweeping when allocation outpaces
sweeping.

Sweeper calls Allocator_Sweep and LargeAllocator_Sweep they update their
internal structures that relate to partially free blocks
(recycledBlocks in Allocator and freeLists in LargeAllocator).
If there is a superblock that crosses the batch boundary,
it is handled in the batch where it starts.
Sweeper finds free superblocks (i.e. range of free blocks) within its batch.

If the Sweeper would immediately return the free superblocks to BlockAllocator
then we couldn't allocate anything bigger than a batch. Therefore the free blocks
at the beginning and the end of the batch are marked as `block_coalesce_me`.
There will be coalesced into bigger blocks by `Sweeper_LazyCoalesce`.
Other free superblocks CAN be immediately returned to BlockAllocator because
their size is already fixed by other non-free blocks around them.

Coalescing could be done in single pass over the heap once all the batches
are swept. However, then large areas of free blocks wouldn't be available
for allocation. Instead coalescing is done incrementally - until we reach a
batch that has not been swept. Coalescing progress is tracked by
`heap->sweep.coalesceDone` - coalescing was done up to this point.
Each sweeper has cursorDone (even the lazy sweeper) to track how far have we swept.

Coalescing is done incrementally - `Sweeper_LazyCoalesce` is called on the 0-th
GC thread after each batch is swept.

When the coalescing reaches the end of heap `Sweeper_SweepDone` is called on the
0-th gc thread. Then the sweeping is done

EXAMPLE:
SWEEP_BATCH_SIZE=3, there are 9 blocks in total and 2 threads
? - unswept block F-free U-unavailable, C-coalesce_me, [] - superblock

???|???|???
thread 1 starts sweeping the first batch and thread 2 the second one
U??|F??|???
UFF|F??|???
U[CC]|F??|???
Thread 1 is done with its block and attempts to coalesce.
The second batch is not done, so it can only coalesce up to item 4.
U[FF]|FF?|???
It starts creating a superblock, then starts sweeping batch 3.
U[FF]|FF?|UF?
U[FF]|FFU|UF?
U[FF]|FFU|U[F]U
In Batch 3 the free block in the middle gets immediately returned to BlockAllocator.
Thread 1 is done, tries to coalesce, but there is nothing to do because batch 2 is not done yet.
U[FF]|[CC]U|U[F]U
Batch 2 is done, thread 2 tries to coalesce. It can coalesce the remaining 2 batches.
U[FFFF]U|U[F]U
The free superblock of size 4 gets returned to BlockAllocator.
U[FFFF]U|U[F]U
U[FFFF]UU[F]U
All is coalesced! Sweeping is done.

Note that besides coalescing `Sweeper_LazyCoalesce` also finishes the sweeping of superblocks in some cases.
See also `block_superblock_start_me` and `LargeAllocator_Sweep`.
*/

void Sweep_applyResult(SweepResult *result, Allocator *allocator, BlockAllocator *blockAllocator) {
    {
        BlockMeta *first = result->recycledBlocks.first;
        if (first != NULL) {
            BlockList_PushAll(&allocator->recycledBlocks, allocator->blockMetaStart, first, result->recycledBlocks.last);
        }
    }

    for (int i = 0; i < SUPERBLOCK_LOCAL_LIST_SIZE; i++) {
        LocalBlockList item = result->freeSuperblocks[i];
        BlockMeta *first = item.first;
        if (first != NULL) {
            BlockList_PushAll(&blockAllocator->freeSuperblocks[i], allocator->blockMetaStart, first, item.last);
        }
    }
    SweepResult_clear(result);
}

void Sweeper_Sweep(Heap *heap, Stats *stats, atomic_uint_fast32_t *cursorDone,
                   uint32_t maxCount) {
    Stats_RecordTimeBatch(stats, start_ns);
    SweepResult sweepResult;
    SweepResult_Init(&sweepResult);
    uint32_t cursor = heap->sweep.cursor;
    uint32_t sweepLimit = heap->sweep.limit;
    // protect against sweep.cursor overflow
    uint32_t startIdx = sweepLimit;
    if (cursor < sweepLimit) {
        startIdx = (uint32_t)atomic_fetch_add(&heap->sweep.cursor, maxCount);
    }
    Stats_RecordTimeSync(stats, presync_end_ns);
#ifdef ENABLE_GC_STATS_SYNC
    if (stats != NULL) {
        Stats_RecordEvent(stats, event_sync, start_ns, presync_end_ns);
    }
#endif

    uint32_t limitIdx = startIdx + maxCount;
    assert(*cursorDone <= startIdx);
    if (limitIdx > sweepLimit) {
        limitIdx = sweepLimit;
    }

    BlockMeta *lastFreeBlockStart = NULL;

    BlockMeta *first = BlockMeta_GetFromIndex(heap->blockMetaStart, startIdx);
    BlockMeta *limit = BlockMeta_GetFromIndex(heap->blockMetaStart, limitIdx);

    BlockMeta *reserveFirst = (BlockMeta *) blockAllocator.reservedSuperblock;
    BlockMeta *reserveLimit = reserveFirst + SWEEP_RESERVE_BLOCKS;

    // reserved block are at the start

#ifdef DEBUG_PRINT
    printf("Sweeper_Sweep(%p %" PRIu32 ",%p %" PRIu32 "\n", first, startIdx,
           limit, limitIdx);
    fflush(stdout);
#endif

    // skip superblock_middle these are handled by the previous batch
    // (BlockMeta_IsSuperblockStartMe(first) ||
    // BlockMeta_IsSuperblockTail(first) || BlockMeta_IsCoalesceMe(first)) && first < limit
    // 0xb, 0x3, 0x13).contains(flags)
    while (((first->block.simple.flags & 0x3) == 0x3) && first < limit) {
#ifdef DEBUG_PRINT
        printf("Sweeper_Sweep SuperblockTail %p %" PRIu32 "\n", first,
               BlockMeta_GetBlockIndex(heap->blockMetaStart, first));
        fflush(stdout);
#endif
        startIdx += 1;
        first += 1;
    }

    BlockMeta *current = first;
    word_t *currentBlockStart =
        Block_GetStartFromIndex(heap->heapStart, startIdx);
    LineMeta *lineMetas = Line_getFromBlockIndex(heap->lineMetaStart, startIdx);
    while (current < limit) {
        int size = 1;
        uint32_t freeCount = 0;
        assert(!BlockMeta_IsCoalesceMe(current));
        assert(current >= reserveFirst && current < reserveLimit
            || !BlockMeta_IsSuperblockTail(current));
        assert(!BlockMeta_IsSuperblockStartMe(current));
        if (current >= reserveFirst && current < reserveLimit) {
            // skip reserved block
            assert(reserveFirst != NULL);
            // size = 1, freeCount = 0
        } else if (BlockMeta_IsSimpleBlock(current)) {
            freeCount = Allocator_Sweep(&allocator, current, currentBlockStart,
                                        lineMetas, &sweepResult);
#ifdef DEBUG_PRINT
            printf("Sweeper_Sweep SimpleBlock %p %" PRIu32 "\n", current,
                   BlockMeta_GetBlockIndex(heap->blockMetaStart, current));
            fflush(stdout);
#endif
        } else if (BlockMeta_IsSuperblockStart(current)) {
            size = BlockMeta_SuperblockSize(current);
            assert(size > 0);
            freeCount = LargeAllocator_Sweep(&largeAllocator, current,
                                             currentBlockStart, limit);
#ifdef DEBUG_PRINT
            printf("Sweeper_Sweep Superblock(%" PRIu32 ") %p %" PRIu32 "\n",
                   size, current,
                   BlockMeta_GetBlockIndex(heap->blockMetaStart, current));
            fflush(stdout);
#endif
        } else {
            assert(BlockMeta_IsFree(current));
            freeCount = 1;
            assert(current->debugFlag == dbg_must_sweep);
#ifdef DEBUG_ASSERT
            current->debugFlag = dbg_free;
#endif
#ifdef DEBUG_PRINT
            printf("Sweeper_Sweep FreeBlock %p %" PRIu32 "\n", current,
                   BlockMeta_GetBlockIndex(heap->blockMetaStart, current));
            fflush(stdout);
#endif
        }
        // ignore superblock middle blocks, that superblock will be swept by
        // someone else
        assert(size > 0);
        assert(freeCount <= size);
        if (lastFreeBlockStart == NULL) {
            if (freeCount > 0) {
                lastFreeBlockStart = current;
            }
        }
        if (lastFreeBlockStart != NULL && freeCount < size) {
            BlockMeta *freeLimit = current + freeCount;
            uint32_t totalSize = (uint32_t)(freeLimit - lastFreeBlockStart);
            if (lastFreeBlockStart == first || freeLimit >= limit) {
                // Free blocks in the start or the end
                // There may be some free blocks before this batch that needs to
                // be coalesced with this block.
                BlockMeta_SetFlagAndSuperblockSize(lastFreeBlockStart, block_coalesce_me, totalSize);
            } else {
                // Free blocks in the middle
                assert(totalSize > 0);
                if (totalSize >= SUPERBLOCK_LOCAL_LIST_MAX) {
                    BlockAllocator_AddFreeSuperblock(&blockAllocator,
                                                     lastFreeBlockStart, totalSize);
                } else {
                    BlockAllocator_AddFreeSuperblockLocal(&blockAllocator, sweepResult.freeSuperblocks,
                                                          lastFreeBlockStart, totalSize);
                }
            }
            lastFreeBlockStart = NULL;
        }

        current += size;
        currentBlockStart += WORDS_IN_BLOCK * size;
        lineMetas += LINE_COUNT * size;
    }
    BlockMeta *doneUntil = current;
    if (lastFreeBlockStart != NULL) {
        // Free blocks in the end or the entire batch is free
        uint32_t totalSize = (uint32_t)(doneUntil - lastFreeBlockStart);
        assert(totalSize > 0);
        // There may be some free blocks after this batch that needs to be
        // coalesced with this block.
        BlockMeta_SetFlagAndSuperblockSize(lastFreeBlockStart, block_coalesce_me, totalSize);
    }

    Stats_RecordTimeSync(stats, postsync_start_ns);

    Sweep_applyResult(&sweepResult, &allocator, &blockAllocator);
    // coalescing might be done by another thread
    // block_coalesce_me marks should be visible
    atomic_thread_fence(memory_order_release);
    atomic_store_explicit(cursorDone, limitIdx, memory_order_release);


    Stats_RecordTimeSync(stats, end_ns);

#ifdef ENABLE_GC_STATS_SYNC
    if (stats != NULL) {
        Stats_RecordEvent(stats, event_sync, postsync_start_ns, end_ns);
    }
#endif
#ifdef ENABLE_GC_STATS_BATCHES
    if (stats != NULL) {
        Stats_RecordEvent(stats, event_sweep_batch, start_ns, end_ns);
    }
#endif
}

uint_fast32_t Sweeper_minSweepCursor(Heap *heap) {
    BlockRangeVal lastActivity = heap->lazySweep.lastActivity;
    uint_fast32_t min;
    if (BlockRange_First(lastActivity) == 1 || lastActivity != heap->lazySweep.lastActivityObserved) {
        // the mutator thread is doing some lazy sweeping
        min = heap->lazySweep.cursorDone;
    } else {
        // the mutator thread has not done any lazy sweeping since last time, it can be ignored
        min = heap->sweep.limit;
    }

    int gcThreadCount = heap->gcThreads.count;
    GCThread *gcThreads = (GCThread *) heap->gcThreads.all;
    for (int i = 0; i < gcThreadCount; i++) {
        uint_fast32_t cursorDone = atomic_load_explicit(&gcThreads[i].sweep.cursorDone, memory_order_acquire);
        if (gcThreads[i].active && cursorDone < min) {
            min = cursorDone;
        }
    }
    heap->lazySweep.lastActivityObserved = lastActivity;
    return min;
}

void Sweeper_LazyCoalesce(Heap *heap, Stats *stats) {
    // the previous coalesce is done and there is work
    uint_fast32_t startIdx = heap->sweep.coalesceDone;
    uint_fast32_t limitIdx = Sweeper_minSweepCursor(heap);
    if (startIdx < limitIdx) {
        // need to get all the coalesce_me information from Sweeper_Sweep
        Stats_RecordTimeBatch(stats, start_ns);
        atomic_thread_fence(memory_order_acquire);

        BlockMeta *lastFreeBlockStart = NULL;
        BlockMeta *lastCoalesceMe = NULL;
        BlockMeta *first =
            BlockMeta_GetFromIndex(heap->blockMetaStart, (uint32_t)startIdx);
        BlockMeta *current = first;
        BlockMeta *limit =
            BlockMeta_GetFromIndex(heap->blockMetaStart, (uint32_t)limitIdx);

        while (current < limit) {
            // updates lastFreeBlockStart and adds blocks
            if (lastFreeBlockStart == NULL) {
                if (BlockMeta_IsCoalesceMe(current)) {
                    lastFreeBlockStart = current;
                }
            } else {
                if (!BlockMeta_IsCoalesceMe(current)) {
                    BlockMeta *freeLimit = current;
                    uint32_t totalSize =
                        (uint32_t)(freeLimit - lastFreeBlockStart);
                    BlockAllocator_AddFreeBlocks(&blockAllocator,
                                                 lastFreeBlockStart, totalSize);
                    lastFreeBlockStart = NULL;
                }
            }

            // controls movement forward
            int size = 1;
            if (BlockMeta_IsCoalesceMe(current)) {
                lastCoalesceMe = current;
                size = BlockMeta_SuperblockSize(current);
            } else if (BlockMeta_IsSuperblockStart(current)) {
                size = BlockMeta_SuperblockSize(current);
            } else if (BlockMeta_IsSuperblockStartMe(current)) {
                // finish the LargeAllocator_Sweep in the case when the last
                // block is not free
                BlockMeta_SetFlagAndSuperblockSize(current, block_superblock_start, 1);
            }

            current += size;
        }
        if (lastFreeBlockStart != NULL) {
            assert(current <= (BlockMeta *)heap->blockMetaEnd);
            assert(current >= limit);
            if (current == limit) {
                uint32_t totalSize = (uint32_t)(limit - lastFreeBlockStart);
                BlockAllocator_AddFreeBlocks(&blockAllocator,
                                             lastFreeBlockStart, totalSize);
            } else {
                // the last superblock crossed the limit
                // other sweepers still need to sweep it
                // add the part that is fully swept
                uint32_t totalSize = (uint32_t)(limit - lastFreeBlockStart);
                uint32_t remainingSize = (uint32_t)(current - limit);
                assert(remainingSize > 0);
                // mark the block in the next batch with the remaining size
                BlockMeta_SetFlagAndSuperblockSize(limit, block_coalesce_me, remainingSize);
                // other threads need to see this
                atomic_thread_fence(memory_order_seq_cst);
                if (totalSize > 0) {
                    BlockAllocator_AddFreeBlocks(&blockAllocator,
                                                 lastFreeBlockStart, totalSize);
                }
                // try to advance the sweep cursor past the superblock
                uint_fast32_t advanceTo =
                    BlockMeta_GetBlockIndex(heap->blockMetaStart, current);
                uint_fast32_t sweepCursor = heap->sweep.cursor;
                while (sweepCursor < advanceTo) {
                    atomic_compare_exchange_strong(&heap->sweep.cursor,
                                                   &sweepCursor, advanceTo);
                    // sweepCursor is updated by atomic_compare_exchange_strong
                }
            }
        }

        heap->sweep.coalesceDone = limitIdx;
        Stats_RecordTimeBatch(stats, end_ns);
#ifdef ENABLE_GC_STATS_BATCHES
        if (stats != NULL) {
            Stats_RecordEvent(stats, event_coalesce_batch, start_ns, end_ns);
        }
#endif
    }
}