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
//(recycledBlocks in Allocator and freeLists in LargeAllocator).
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
                                  word_t *blockStart, LineMeta *lineMetas,
                                  SweepResult *result) {

    // If the block is not marked, it means that it's completely free
    assert(blockMeta->debugFlag == dbg_must_sweep);
    if (!BlockMeta_IsMarked(blockMeta)) {
        memset(blockMeta, 0, sizeof(BlockMeta));
        // does not unmark in LineMetas because those are ignored by the
        // allocator
        ObjectMeta_ClearBlockAt(Bytemap_Get(allocator->bytemap, blockStart));
#ifdef DEBUG_ASSERT
        blockMeta->debugFlag = dbg_free;
#endif
        return 1;
    } else {
        // If the block is marked, we need to recycle line by line
        assert(BlockMeta_IsMarked(blockMeta));
        BlockMeta_Unmark(blockMeta);
        Bytemap *bytemap = allocator->bytemap;

        // start at line zero, keep separate pointers into all affected data
        // structures
        int lineIndex = 0;
        LineMeta *lineMeta = lineMetas;
        word_t *lineStart = blockStart;
        ObjectMeta *bytemapCursor = Bytemap_Get(bytemap, lineStart);

        FreeLineMeta *lastRecyclable = NULL;
        while (lineIndex < LINE_COUNT) {
            // If the line is marked, we need to unmark all objects in the line
            if (Line_IsMarked(lineMeta)) {
                // Unmark line
                Line_Unmark(lineMeta);
                ObjectMeta_SweepLineAt(bytemapCursor);

                // next line
                lineIndex++;
                lineMeta++;
                lineStart += WORDS_IN_LINE;
                bytemapCursor = Bytemap_NextLine(bytemapCursor);
            } else {
                // If the line is not marked, we need to merge all continuous
                // unmarked lines.

                // If it's the first free line, update the block header to point
                // to it.
                if (lastRecyclable == NULL) {
                    BlockMeta_SetFirstFreeLine(blockMeta, lineIndex);
                } else {
                    // Update the last recyclable line to point to the current
                    // one
                    lastRecyclable->next = lineIndex;
                }
                ObjectMeta_ClearLineAt(bytemapCursor);
                lastRecyclable = (FreeLineMeta *)lineStart;

                // next line
                lineIndex++;
                lineMeta++;
                lineStart += WORDS_IN_LINE;
                bytemapCursor = Bytemap_NextLine(bytemapCursor);

                uint8_t size = 1;
                while (lineIndex < LINE_COUNT && !Line_IsMarked(lineMeta)) {
                    ObjectMeta_ClearLineAt(bytemapCursor);
                    size++;

                    // next line
                    lineIndex++;
                    lineMeta++;
                    lineStart += WORDS_IN_LINE;
                    bytemapCursor = Bytemap_NextLine(bytemapCursor);
                }
                lastRecyclable->size = size;
            }
        }
        // If there is no recyclable line, the block is unavailable
        if (lastRecyclable != NULL) {
            lastRecyclable->next = LAST_HOLE;

            assert(BlockMeta_FirstFreeLine(blockMeta) >= 0);
            assert(BlockMeta_FirstFreeLine(blockMeta) < LINE_COUNT);
            // allocator->recycledBlockCount++;
            atomic_fetch_add_explicit(&allocator->recycledBlockCount, 1,
                                      memory_order_relaxed);

#ifdef DEBUG_ASSERT
            blockMeta->debugFlag = dbg_partial_free;
#endif
            // the allocator thread must see the sweeping changes in recycled
            // blocks
            atomic_thread_fence(memory_order_release);
            LocalBlockList_Push(&result->recycledBlocks,
                                allocator->blockMetaStart, blockMeta);
#ifdef DEBUG_PRINT
            printf(
                "sweepSimpleBlock %p %" PRIu32 " => RECYCLED\n", blockMeta,
                BlockMeta_GetBlockIndex(allocator->blockMetaStart, blockMeta));
            fflush(stdout);
#endif
        } else {
#ifdef DEBUG_ASSERT
            atomic_thread_fence(memory_order_release);
            blockMeta->debugFlag = dbg_not_free;
#endif
        }
        return 0;
    }
}

uint32_t Sweeper_sweepSuperblock(LargeAllocator *allocator,
                                 BlockMeta *blockMeta, word_t *blockStart,
                                 BlockMeta *batchLimit) {
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
#endif

    ObjectMeta *firstObject = Bytemap_Get(allocator->bytemap, blockStart);
    assert(!ObjectMeta_IsFree(firstObject));
    BlockMeta *lastBlock = blockMeta + superblockSize - 1;
    int freeCount = 0;
    if (superblockSize > 1 && !ObjectMeta_IsMarked(firstObject)) {
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
    word_t *chunkStart = NULL;

    // the tail end of the first object
    if (!ObjectMeta_IsMarked(firstObject)) {
        chunkStart = lastBlockStart;
    }
    ObjectMeta_Sweep(firstObject);

    word_t *current = lastBlockStart + (MIN_BLOCK_SIZE / WORD_SIZE);
    ObjectMeta *currentMeta = Bytemap_Get(allocator->bytemap, current);
    while (current < blockEnd) {
        if (chunkStart == NULL) {
            // if (ObjectMeta_IsAllocated(currentMeta)||
            // ObjectMeta_IsPlaceholder(currentMeta)) {
            if (*currentMeta & 0x3) {
                chunkStart = current;
            }
        } else {
            if (ObjectMeta_IsMarked(currentMeta)) {
                size_t currentSize = (current - chunkStart) * WORD_SIZE;
                LargeAllocator_AddChunk(allocator, (Chunk *)chunkStart,
                                        currentSize);
                chunkStart = NULL;
            }
        }
        ObjectMeta_Sweep(currentMeta);

        current += MIN_BLOCK_SIZE / WORD_SIZE;
        currentMeta += MIN_BLOCK_SIZE / ALLOCATION_ALIGNMENT;
    }
    if (chunkStart == lastBlockStart) {
        // free chunk covers the entire last block, released it
        freeCount += 1;
#ifdef DEBUG_ASSERT
        lastBlock->debugFlag = dbg_free;
#endif
    } else {
#ifdef DEBUG_ASSERT
        lastBlock->debugFlag = dbg_not_free;
#endif
        if (ObjectMeta_IsFree(firstObject)) {
            // the first object was free
            // the end of first object becomes a placeholder
            ObjectMeta_SetPlaceholder(
                Bytemap_Get(allocator->bytemap, lastBlockStart));
        }
        if (freeCount > 0) {
            // the last block is its own superblock
            if (lastBlock < batchLimit) {
                // The block is within current batch, just create the superblock
                // yourself
                BlockMeta_SetFlagAndSuperblockSize(lastBlock,
                                                   block_superblock_start, 1);
            } else {
                // If we cross the current batch, then it is not to mark a
                // block_superblock_tail to block_superblock_start. The other
                // sweeper threads could be in the middle of skipping
                // block_superblock_tail s. Then creating the superblock will
                // be done by Heap_lazyCoalesce
                BlockMeta_SetFlag(lastBlock, block_superblock_start_me);
            }
        }
        // handle the last chunk if any
        if (chunkStart != NULL) {
            size_t currentSize = (current - chunkStart) * WORD_SIZE;
            LargeAllocator_AddChunk(allocator, (Chunk *)chunkStart,
                                    currentSize);
        }
    }
#ifdef DEBUG_PRINT
    printf("sweepSuperblock %p %" PRIu32 " => FREE %" PRIu32 "/ %" PRIu32 "\n",
           blockMeta,
           BlockMeta_GetBlockIndex(allocator->blockMetaStart, blockMeta),
           freeCount, superblockSize);
    fflush(stdout);
#endif
    return freeCount;
}

void Sweep_applyResult(SweepResult *result, Allocator *allocator,
                       BlockAllocator *blockAllocator) {
    {
        BlockMeta *first = result->recycledBlocks.first;
        if (first != NULL) {
            BlockList_PushAll(&allocator->recycledBlocks,
                              allocator->blockMetaStart, first,
                              result->recycledBlocks.last);
        }
    }

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
    LineMeta *lineMetas = Line_getFromBlockIndex(heap->lineMetaStart, startIdx);
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
            freeCount =
                Sweeper_sweepSimpleBlock(&allocator, current, currentBlockStart,
                                         lineMetas, &sweepResult);
#ifdef DEBUG_PRINT
            printf("Sweeper_Sweep SimpleBlock %p %" PRIu32 "\n", current,
                   BlockMeta_GetBlockIndex(heap->blockMetaStart, current));
            fflush(stdout);
#endif
        } else if (BlockMeta_IsSuperblockStart(current)) {
            size = BlockMeta_SuperblockSize(current);
            assert(size > 0);
            freeCount = Sweeper_sweepSuperblock(&largeAllocator, current,
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
        lineMetas += LINE_COUNT * size;
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