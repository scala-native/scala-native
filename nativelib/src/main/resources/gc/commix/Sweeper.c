#include "Sweeper.h"
#include "Stats.h"
#include "State.h"
#include "GCThread.h"
#include "GCTypes.h"
#include <sched.h>

// Sweeper implements concurrent sweeping by coordinating lazy sweeper on the
// mutator thread with one or more concurrent sweepers on GC threads.
//
// After the mark is done the concurrent sweepers are started.
// Each takes batch of SWEEP_BATCH_SIZE blocks using the `heap->sweep.cursor`.
// If the mutator thread fails to allocate if will sweep a batch of
// LAZY_SWEEP_MIN_BATCH blocks. This will speed up sweeping when allocation
// outpaces sweeping.
//
// Sweeper calls sweepSimpleBlock and sweepSuperblock they update the
// corresponding internal structures that relate to partially free blocks
//(freeLists in LargeAllocator).
// If there is a superblock that crosses the batch boundary,
// it is handled in the batch where it starts.
// Sweeper finds free superblocks (i.e. range of free blocks) within its batch.
//
// If the Sweeper would immediately return the free superblocks to
// BlockAllocator  then we couldn't allocate anything bigger than a batch.
// Therefore the free blocks  at the beginning and the end of the batch are
// marked as `block_coalesce_me`.  There will be coalesced into bigger blocks by
// `Sweeper_LazyCoalesce`.  Other free superblocks CAN be immediately returned
// to BlockAllocator because  their size is already fixed by other non-free
// blocks around them.
//
// Coalescing could be done in single pass over the heap once all the batches
// are swept. However, then large areas of free blocks wouldn't be available
// for allocation. Instead coalescing is done incrementally - until we reach a
// batch that has not been swept. Coalescing progress is tracked by
//`heap->sweep.coalesceDone` - coalescing was done up to this point.
// Each sweeper has cursorDone (even the lazy sweeper) to track how far have we
// swept.
//
// Coalescing is done incrementally - `Sweeper_LazyCoalesce` is called on the
// 0-th  GC thread after each batch is swept.
//
// When the coalescing reaches the end of heap `Sweeper_SweepDone` is called on
// the  0-th gc thread. Then the sweeping is done
//
// EXAMPLE:
//        SWEEP_BATCH_SIZE=3, there are 9 blocks in total and 2 threads:
//        master (thread 0) and thread 1
//        ? - unswept block F-free U-unavailable, C-coalesce_me, [] - superblock
//
//        ???|???|???
//        thread 0 starts sweeping the first batch and thread 1 the second one
//        U??|F??|???
//        UFF|F??|???
//        U[CC]|F??|???
//        Thread 0 is done with its block and attempts to coalesce.
//        The second batch is not done, so it can only coalesce up to item 4.
//        U[FF]|FF?|???
//        It starts creating a superblock, then starts sweeping batch 3.
//        U[FF]|FF?|UF?
//        U[FF]|FFU|UF?
//        U[FF]|FFU|U[F]U
//        In Batch 3 the free block in the middle gets immediately returned
//        to BlockAllocator. Thread 0 is done, tries to coalesce, but
//        there is nothing to do because batch 2 is not done yet. It is the
//        master
//         thread so it will check again.
//        U[FF]|[CC]U|U[F]U
//        Batch 2 is done, thread 2 has no more batches, it stops.
//        Thread 0 notices batch
//        U[FFFF]U|U[F]U
//        The free superblock of size 4 gets returned to BlockAllocator.
//        U[FFFF]U|U[F]U
//        U[FFFF]UU[F]U
//        All is coalesced! Sweeping is done.
//
//        Note that besides coalescing `Sweeper_LazyCoalesce` also
//        finishes the sweeping of superblocks in some cases.
//        See also `block_superblock_start_me` and `Sweeper_sweepSuperblock`.

uint32_t Sweeper_sweepSimpleBlock(Allocator *allocator, BlockMeta *blockMeta,
                                  word_t *blockStart, SweepResult *result, bool collectingOld) {
    assert(blockMeta->debugFlag == dbg_must_sweep);
    // If the block is not marked, it means that it's completely free
    if (!BlockMeta_IsMarked(blockMeta)) {

#ifdef DEBUG_ASSERT
    ObjectMeta *currentBlockStart = Bytemap_Get(heap.bytemap, blockStart);
    ObjectMeta *nextBlockStart = currentBlockStart + (WORDS_IN_BLOCK / ALLOCATION_ALIGNMENT_WORDS);

    for (ObjectMeta *object = currentBlockStart; object < nextBlockStart; object++) {
        if (collectingOld) {
            assert(!ObjectMeta_IsAllocated(object));
        } else {
            assert(!(ObjectMeta_IsMarked(object) || ObjectMeta_IsMarkedRem(object)));
        }
    }

#endif
        memset(blockMeta, 0, sizeof(BlockMeta));
        ObjectMeta_ClearBlockAt(Bytemap_Get(allocator->bytemap, blockStart));
#ifdef DEBUG_ASSERT
        blockMeta->debugFlag = dbg_free;
#endif
        return 1;
    } else {
        Bytemap *bytemap = allocator->bytemap;

        if (!collectingOld) {
            assert(!BlockMeta_IsOld(blockMeta));
            BlockMeta_IncrementAge(blockMeta);
            if (!BlockMeta_IsOld(blockMeta)) {
#ifdef DEBUG_PRINT
                printf("Sweeper_sweepSimpleBlock %p %" PRIu32 " has age %d/%d\n", blockMeta, BlockMeta_GetBlockIndex(allocator->blockMetaStart, blockMeta),
                        BlockMeta_GetAge(blockMeta), MAX_AGE_YOUNG_BLOCK);
                fflush(stdout);
#endif
                atomic_fetch_add_explicit(&allocator->blockAllocator->youngBlockCount, 1, memory_order_relaxed);
            } else {
                atomic_fetch_add_explicit(&allocator->blockAllocator->oldBlockCount, 1, memory_order_relaxed);
#ifdef DEBUG_PRINT
                printf("Sweeper_sweepSimpleBlock promoting block %p %" PRIu32 " to old generation\n", blockMeta, BlockMeta_GetBlockIndex(allocator->blockMetaStart, blockMeta));
                fflush(stdout);
#endif
            }
        } else {
            atomic_fetch_add_explicit(&allocator->blockAllocator->oldBlockCount, 1, memory_order_relaxed);
        }
        BlockMeta_Unmark(blockMeta);
        ObjectMeta *bytemapCursor = Bytemap_Get(bytemap, (word_t *)blockStart);
        ObjectMeta *lastCursor = bytemapCursor + (WORDS_IN_LINE/ALLOCATION_ALIGNMENT_WORDS)*LINE_COUNT;
        if (collectingOld) {
            while (bytemapCursor < lastCursor) {
                ObjectMeta_SweepOldLineAt(bytemapCursor);
                bytemapCursor = Bytemap_NextLine(bytemapCursor);
            }
        } else if (BlockMeta_IsOld(blockMeta)) {
            while (bytemapCursor < lastCursor) {
                ObjectMeta_SweepNewOldLineAt(bytemapCursor);
                bytemapCursor = Bytemap_NextLine(bytemapCursor);
            }
        } else {
            while (bytemapCursor < lastCursor) {
                ObjectMeta_SweepLineAt(bytemapCursor);
                bytemapCursor = Bytemap_NextLine(bytemapCursor);
            }
        }
#ifdef DEBUG_ASSERT
        atomic_thread_fence(memory_order_release);
        blockMeta->debugFlag = dbg_not_free;
#endif
        return 0;
    }
}

uint32_t Sweeper_sweepSuperblock(LargeAllocator *allocator, BlockMeta *blockMeta,
                                 word_t *blockStart, BlockMeta *batchLimit, bool collectingOld) {
    // Objects that are larger than a block
    // are always allocated at the beginning the smallest possible superblock.
    // Any gaps at the end can be filled with large objects, that are smaller
    // than a block. This means that objects can ONLY start at the beginning at
    // the first block or anywhere at the last block, except the beginning.
    // Therefore we only need to look at a few locations.
    uint32_t superblockSize = BlockMeta_SuperblockSize(blockMeta);
    word_t *blockEnd = blockStart + WORDS_IN_BLOCK * superblockSize;

#ifdef DEBUG_ASSERT
    for (BlockMeta *block = blockMeta; block < blockMeta + superblockSize;
         block++) {
        assert(block->debugFlag == dbg_must_sweep);
    }

    if (collectingOld) {
        assert(BlockMeta_IsOld(blockMeta));
    } else {
        assert(!BlockMeta_IsOld(blockMeta));
    }
#endif
    ObjectMeta *firstObject = Bytemap_Get(allocator->bytemap, blockStart);
    // It is ok to have free objects at the begining of the block since
    // we do not release chunk by chunk but block by block
    //assert(!ObjectMeta_IsFree(firstObject));
    BlockMeta *lastBlock = blockMeta + superblockSize - 1;

    if (!collectingOld) {
        BlockMeta_IncrementAge(blockMeta);
        // If the super block is larger than 1 block, then all blocks except the last
        // one contains the first object. Thus we only need to mark the last block
        if (superblockSize > 1) {
            BlockMeta_IncrementAge(lastBlock);
        }
#ifdef DEBUG_PRINT
                printf("Sweeper_sweepSuper promoting block (%p,%p) (%" PRIu32 ",%" PRIu32") to old generation\n", blockMeta, lastBlock, BlockMeta_GetBlockIndex(allocator->blockMetaStart, blockMeta), BlockMeta_GetBlockIndex(allocator->blockMetaStart, lastBlock));
                fflush(stdout);
#endif
        assert(BlockMeta_GetAge(blockMeta) == BlockMeta_GetAge(lastBlock));
    }
    BlockMeta_UnmarkSuperblock(blockMeta);

    int freeCount = 0;
    bool firstObjectAlive = ObjectMeta_IsAliveSweep(firstObject, collectingOld);

    if (superblockSize > 1 && !firstObjectAlive) {
        // release free superblock starting from the first object
        freeCount = superblockSize - 1;
#ifdef DEBUG_ASSERT
        for (BlockMeta *block = blockMeta; block < blockMeta + freeCount;
             block++) {
            block->debugFlag = dbg_free;
        }
#endif
    } else {
#ifdef DEBUG_ASSERT
        for (BlockMeta *block = blockMeta;
             block < blockMeta + superblockSize - 1; block++) {
            block->debugFlag = dbg_not_free;
        }
#endif
    }

    word_t *lastBlockStart = blockEnd - WORDS_IN_BLOCK;

    if (collectingOld) {
        ObjectMeta_SweepOld(firstObject);
    } else if (BlockMeta_IsOld(blockMeta)) {
        ObjectMeta_SweepNewOld(firstObject);
    } else {
        ObjectMeta_Sweep(firstObject);
    }

    word_t *current = lastBlockStart + (MIN_BLOCK_SIZE / WORD_SIZE);
    ObjectMeta *currentMeta = Bytemap_Get(allocator->bytemap, current);
    // The first object must be in the last block
    bool lastblockContainsLiveObjects = firstObjectAlive;

    if (collectingOld) {
        while(current < blockEnd) {
            lastblockContainsLiveObjects |= ((*currentMeta & 0x2) == 0x2);
            ObjectMeta_SweepOld(currentMeta);
            current += MIN_BLOCK_SIZE / WORD_SIZE;
            currentMeta += MIN_BLOCK_SIZE / ALLOCATION_ALIGNMENT;
        }
    } else if (BlockMeta_IsOld(lastBlock)) {
        while(current < blockEnd) {
            lastblockContainsLiveObjects |= ((*currentMeta & 0x4) == 0x4);
            ObjectMeta_SweepNewOld(currentMeta);
            current += MIN_BLOCK_SIZE / WORD_SIZE;
            currentMeta += MIN_BLOCK_SIZE / ALLOCATION_ALIGNMENT;
        }
    } else {
        assert(!collectingOld && !BlockMeta_IsOld(lastBlock));
        while(current < blockEnd) {
            lastblockContainsLiveObjects |= ((*currentMeta & 0x4) == 0x4);
            ObjectMeta_Sweep(currentMeta);
            current += MIN_BLOCK_SIZE / WORD_SIZE;
            currentMeta += MIN_BLOCK_SIZE / ALLOCATION_ALIGNMENT;
        }
    }

    if (!lastblockContainsLiveObjects) {
        // The whole super block is free
#ifdef DEBUG_ASSERT
        lastBlock->debugFlag = dbg_free;
#endif
        freeCount += 1;
    } else {
        // The last block contains some live objects
#ifdef DEBUG_ASSERT
        lastBlock->debugFlag = dbg_not_free;
#endif
        if (freeCount > 0) {
            assert(!firstObjectAlive);
            // The last block is its own superblock
            if (BlockMeta_IsOld(lastBlock)) {
                atomic_fetch_add_explicit(&blockAllocator.oldBlockCount, 1, memory_order_relaxed);
            } else  {
                atomic_fetch_add_explicit(&blockAllocator.youngBlockCount, 1, memory_order_relaxed);
            }
            if (lastBlock < batchLimit) {
                // The block is within current btch, just create the superblock
                // yourself
                BlockMeta_SetFlagAndSuperblockSize(lastBlock, block_superblock_start, 1);
            } else {
                // if we cross the current batch, then it is not to mark a
                // block_superblock_tail to block_superblock_start. The other
                // sweeper threads could be in the middle of skipping
                // block_superblock_tail s. Then creating the superblock will
                // be done by Heap_lazyCoalesce
                BlockMeta_SetFlag(lastBlock, block_superblock_start_me);
            }
        } else {
            assert(firstObjectAlive);
            // The whole superblock is still alive
            if (BlockMeta_IsOld(lastBlock)) {
                atomic_fetch_add_explicit(&blockAllocator.oldBlockCount, superblockSize, memory_order_relaxed);
            } else  {
                atomic_fetch_add_explicit(&blockAllocator.youngBlockCount, superblockSize, memory_order_relaxed);
            }
        }
    }

    assert((blockMeta->block.simple.flags != block_superblock_start_marked) && 
            (lastBlock->block.simple.flags != block_superblock_tail_marked));

#ifdef DEBUG_PRINT
    printf("sweepSuperblock %p %" PRIu32 " => FREE %" PRIu32 "/ %" PRIu32
           "\n",
           blockMeta,
           BlockMeta_GetBlockIndex(allocator->blockMetaStart, blockMeta),
           freeCount, superblockSize);
    fflush(stdout);
#endif
    return freeCount;
}


void Sweep_applyResult(SweepResult *result, Allocator *allocator,
                       BlockAllocator *blockAllocator) {

    for (int i = 0; i < SUPERBLOCK_LOCAL_LIST_SIZE; i++) {
        LocalBlockList item = result->freeSuperblocks[i];
        BlockMeta *first = item.first;
        if (first != NULL) {
            BlockList_PushAll(&blockAllocator->freeSuperblocks[i],
                              allocator->blockMetaStart, first, item.last);
        }
    }
    SweepResult_clear(result);
}

void Sweeper_Sweep(Heap *heap, Stats *stats, atomic_uint_fast32_t *cursorDone,
                   uint32_t maxCount, bool collectingOld) {
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
    Stats_RecordEventSync(stats, event_sync, start_ns, presync_end_ns);

    uint32_t limitIdx = startIdx + maxCount;
    assert(*cursorDone <= startIdx);
    if (limitIdx > sweepLimit) {
        limitIdx = sweepLimit;
    }

    BlockMeta *lastFreeBlockStart = NULL;

    BlockMeta *first = BlockMeta_GetFromIndex(heap->blockMetaStart, startIdx);
    BlockMeta *limit = BlockMeta_GetFromIndex(heap->blockMetaStart, limitIdx);

    BlockMeta *reserveFirst = (BlockMeta *)blockAllocator.reservedSuperblock;
    BlockMeta *reserveLimit = reserveFirst + SWEEP_RESERVE_BLOCKS;

    // reserved block are at the start

#ifdef DEBUG_PRINT
    printf("Sweeper_Sweep(%p %" PRIu32 ",%p %" PRIu32 "\n", first, startIdx,
           limit, limitIdx);
    fflush(stdout);
#endif

    // skip superblock_middle these are handled by the previous batch
    // (BlockMeta_IsSuperblockStartMe(first) ||
    // BlockMeta_IsSuperblockTail(first) || BlockMeta_IsCoalesceMe(first)) &&
    // first < limit 0xb, 0x3, 0x13).contains(flags)
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
    while (current < limit) {
        int size = 1;
        uint32_t freeCount = 0;
        assert(!BlockMeta_IsCoalesceMe(current));

        assert(current >= reserveFirst && current < reserveLimit ||
               !BlockMeta_IsSuperblockTail(current));
        assert(!BlockMeta_IsSuperblockStartMe(current));
        if (current >= reserveFirst && current < reserveLimit) {
            // skip reserved block
            assert(reserveFirst != NULL);
            // size = 1, freeCount = 0
        } else if (BlockMeta_IsSimpleBlock(current)) {
            if (BlockMeta_ShouldBeSwept(current, collectingOld)) {
                freeCount = Sweeper_sweepSimpleBlock(&allocator, current, currentBlockStart,
                                                 &sweepResult, collectingOld);
#ifdef DEBUG_PRINT
            printf("Sweeper_Sweep SimpleBlock %p %" PRIu32 "\n", current,
                   BlockMeta_GetBlockIndex(heap->blockMetaStart, current));
            fflush(stdout);
#endif
            }
            assert(!BlockMeta_IsMarked(current));
        } else if (BlockMeta_IsSuperblockStart(current)) {
            size = BlockMeta_SuperblockSize(current);
            assert(size > 0);
            if (BlockMeta_ShouldBeSwept(current, collectingOld)) {
                freeCount = Sweeper_sweepSuperblock(&largeAllocator, current,
                                                    currentBlockStart, limit, collectingOld);
#ifdef DEBUG_PRINT
                printf("Sweeper_Sweep Superblock(%" PRIu32 ") %p %" PRIu32 "\n",
                       size, current,
                       BlockMeta_GetBlockIndex(heap->blockMetaStart, current));
                fflush(stdout);
#endif
            }
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
                BlockMeta_SetFlagAndSuperblockSize(
                    lastFreeBlockStart, block_coalesce_me, totalSize);
            } else {
                // Free blocks in the middle
                assert(totalSize > 0);
                if (totalSize >= SUPERBLOCK_LOCAL_LIST_MAX) {
                    BlockAllocator_AddFreeSuperblock(
                        &blockAllocator, lastFreeBlockStart, totalSize);
                } else {
                    BlockAllocator_AddFreeSuperblockLocal(
                        &blockAllocator, sweepResult.freeSuperblocks,
                        lastFreeBlockStart, totalSize);
                }
            }
            lastFreeBlockStart = NULL;
        }

        current += size;
        currentBlockStart += WORDS_IN_BLOCK * size;
    }
    BlockMeta *doneUntil = current;
    if (lastFreeBlockStart != NULL) {
        // Free blocks in the end or the entire batch is free
        uint32_t totalSize = (uint32_t)(doneUntil - lastFreeBlockStart);
        assert(totalSize > 0);
        // There may be some free blocks after this batch that needs to be
        // coalesced with this block.
        BlockMeta_SetFlagAndSuperblockSize(lastFreeBlockStart,
                                           block_coalesce_me, totalSize);
    }

    Stats_RecordTimeSync(stats, postsync_start_ns);

    Sweep_applyResult(&sweepResult, &allocator, &blockAllocator);
    // coalescing might be done by another thread
    // block_coalesce_me marks should be visible
    atomic_thread_fence(memory_order_release);
    atomic_store_explicit(cursorDone, limitIdx, memory_order_release);

    Stats_RecordTimeBatch(stats, end_ns);
    Stats_RecordEventSync(stats, event_sync, postsync_start_ns, end_ns);
    Stats_RecordEventBatches(stats, event_sweep_batch, start_ns, end_ns);
}

uint_fast32_t Sweeper_minSweepCursor(Heap *heap) {
    BlockRangeVal lastActivity = heap->lazySweep.lastActivity;
    uint_fast32_t min;
    if (BlockRange_First(lastActivity) == 1 ||
        lastActivity != heap->lazySweep.lastActivityObserved) {
        // the mutator thread is doing some lazy sweeping
        min = heap->lazySweep.cursorDone;
    } else {
        // the mutator thread has not done any lazy sweeping since last time, it
        // can be ignored
        min = heap->sweep.limit;
    }

    int gcThreadCount = heap->gcThreads.count;
    GCThread *gcThreads = (GCThread *)heap->gcThreads.all;
    for (int i = 0; i < gcThreadCount; i++) {
        uint_fast32_t cursorDone = atomic_load_explicit(
            &gcThreads[i].sweep.cursorDone, memory_order_acquire);
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
                // finish the supeblock sweep in the case when the last
                // block is not free
                BlockMeta_SetFlagAndSuperblockSize(current,
                                                   block_superblock_start, 1);
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
                BlockMeta_SetFlagAndSuperblockSize(limit, block_coalesce_me,
                                                   remainingSize);
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
        Stats_RecordEventBatches(stats, event_coalesce_batch, start_ns, end_ns);
    }
}

void Sweeper_LazySweepUntilDone(Heap *heap) {
    Stats_DefineOrNothing(stats, heap->stats);
    Stats_RecordTime(stats, start_ns);
    heap->lazySweep.lastActivity = BlockRange_Pack(1, heap->sweep.cursor);
    while (heap->sweep.cursor < heap->sweep.limit) {
        Sweeper_Sweep(heap, heap->stats, &heap->lazySweep.cursorDone,
                      LAZY_SWEEP_MIN_BATCH, heap->lazySweep.nextSweepOld);
    }
    heap->lazySweep.lastActivity = BlockRange_Pack(0, heap->sweep.cursor);
    while (!Sweeper_IsSweepDone(heap)) {
        sched_yield();
    }
    Stats_RecordTime(stats, end_ns);
    Stats_RecordEvent(stats, event_sweep, start_ns, end_ns);
}

#ifdef DEBUG_ASSERT
void Sweeper_ClearIsSwept(Heap *heap) {
    BlockMeta *current = (BlockMeta *)heap->blockMetaStart;
    BlockMeta *limit = (BlockMeta *)heap->blockMetaEnd;
    while (current < limit) {
        BlockMeta *reserveFirst =
            (BlockMeta *)blockAllocator.reservedSuperblock;
        BlockMeta *reserveLimit = reserveFirst + SWEEP_RESERVE_BLOCKS;
        if (current < reserveFirst || current >= reserveLimit) {
            assert(reserveFirst != NULL);
            current->debugFlag = dbg_must_sweep;
        }
        current++;
    }
}

void Sweeper_AssertIsConsistent(Heap *heap) {
    BlockMeta *current = (BlockMeta *)heap->blockMetaStart;
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
        ObjectMeta *nextBlockStart =
            currentBlockStart +
            (WORDS_IN_BLOCK / ALLOCATION_ALIGNMENT_WORDS) * size;

        for (ObjectMeta *object = currentBlockStart; object < nextBlockStart;
             object++) {
            if (!BlockMeta_IsOld(current)) {
                assert(!ObjectMeta_IsOld(object));
            } else {
                assert(!ObjectMeta_IsAllocated(object));
            }

        }
        current = next;
        currentBlockStart = nextBlockStart;
    }
    assert(current == limit);
}
#endif
