#include "Sweeper.h"
#include "Stats.h"
#include "State.h"

void Sweeper_sweepDone(Heap *heap);

static inline void Sweeper_advanceLazyCursor(Heap *heap) {
    atomic_uint_fast32_t cursor = heap->sweep.cursor;
    atomic_uint_fast32_t sweepLimit = heap->sweep.limit;
    heap->sweep.cursorDone = (cursor <= sweepLimit) ? cursor : sweepLimit ;
}

Object *Sweeper_LazySweep(Heap *heap, uint32_t size) {
    Object *object = (Object *)Allocator_Alloc(&allocator, size);
    if (object != NULL) {
        // advance the cursor so other threads can coalesce
        Sweeper_advanceLazyCursor(heap);
    } else {
        // lazy sweep will happen
        uint64_t start_ns, end_ns;
        Stats *stats = heap->stats;
        if (stats != NULL) {
            start_ns = scalanative_nano_time();
        }
        while (object == NULL && !Sweeper_IsSweepDone(heap)) {
            Sweeper_Sweep(heap, &heap->sweep.cursorDone, LAZY_SWEEP_MIN_BATCH);
            object = (Object *)Allocator_Alloc(&allocator, size);
            if (heap->gcThreadCount == 0) {
                // if there are no threads the mutator must do coalescing on its own
                Sweeper_LazyCoalesce(heap);
            }
        }
        if (stats != NULL) {
            end_ns = scalanative_nano_time();
            Stats_RecordEvent(stats, event_sweep, MUTATOR_THREAD_ID, start_ns, end_ns);
        }
    }
    if (Sweeper_IsSweepDone(heap) && !heap->postSweepDone) {
        Sweeper_sweepDone(heap);
    }
    return object;
}


Object *Sweeper_LazySweepLarge(Heap *heap, uint32_t size) {
    Object *object = LargeAllocator_GetBlock(&largeAllocator, size);
    #ifdef DEBUG_PRINT
        uint32_t increment = (uint32_t) MathUtils_DivAndRoundUp(size, BLOCK_TOTAL_SIZE);
        printf("Sweeper_LazySweepLarge (%" PRIu32 ") => %" PRIu32 "\n", size, increment);
        fflush(stdout);
    #endif
    if (object != NULL) {
        // advance the cursor so other threads can coalesce
        Sweeper_advanceLazyCursor(heap);
    } else {
        // lazy sweep will happen
        uint64_t start_ns, end_ns;
        Stats *stats = heap->stats;
        if (stats != NULL) {
            start_ns = scalanative_nano_time();
        }
        while (object == NULL && !Sweeper_IsSweepDone(heap)) {
            Sweeper_Sweep(heap, &heap->sweep.cursorDone, LAZY_SWEEP_MIN_BATCH);
            object = LargeAllocator_GetBlock(&largeAllocator, size);
            if (heap->gcThreadCount == 0) {
                // if there are no threads the mutator must do coalescing on its own
                Sweeper_LazyCoalesce(heap);
            }
        }
        if (stats != NULL) {
            end_ns = scalanative_nano_time();
            Stats_RecordEvent(stats, event_sweep, MUTATOR_THREAD_ID, start_ns, end_ns);
        }
    }
    if (Sweeper_IsSweepDone(heap) && !heap->postSweepDone) {
        Sweeper_sweepDone(heap);
    }
    return object;
}

void Sweeper_Sweep(Heap *heap, atomic_uint_fast32_t *cursorDone, uint32_t maxCount) {
    uint32_t cursor = heap->sweep.cursor;
    uint32_t sweepLimit = heap->sweep.limit;
    // protect against sweep.cursor overflow
    uint32_t startIdx = sweepLimit;
    if (cursor < sweepLimit) {
        startIdx = (uint32_t) atomic_fetch_add(&heap->sweep.cursor, maxCount);
    }
    uint32_t limitIdx = startIdx + maxCount;
    assert(*cursorDone <= startIdx);
    if (limitIdx > sweepLimit) {
        limitIdx = sweepLimit;
    }

    BlockMeta *lastFreeBlockStart = NULL;

    BlockMeta *first = BlockMeta_GetFromIndex(heap->blockMetaStart, startIdx);
    BlockMeta *limit = BlockMeta_GetFromIndex(heap->blockMetaStart, limitIdx);

    #ifdef DEBUG_PRINT
        printf("Sweeper_Sweep(%p %" PRIu32 ",%p %" PRIu32 "\n",
               first, startIdx, limit, limitIdx);
        fflush(stdout);
    #endif

    // skip superblock_middle these are handled by the previous batch
    // (BlockMeta_IsSuperblockStartMe(first) || BlockMeta_IsSuperblockMiddle(first)) && first < limit
    while (((first->block.simple.flags & 0x3) == 0x3) && first < limit) {
        #ifdef DEBUG_PRINT
            printf("Sweeper_Sweep SuperblockMiddle %p %" PRIu32 "\n",
                   first, BlockMeta_GetBlockIndex(heap->blockMetaStart, first));
            fflush(stdout);
        #endif
        startIdx += 1;
        first += 1;
    }

    BlockMeta *current = first;
    word_t *currentBlockStart = Block_GetStartFromIndex(heap->heapStart, startIdx);
    LineMeta *lineMetas = Line_getFromBlockIndex(heap->lineMetaStart, startIdx);
    while (current < limit) {
        int size = 1;
        uint32_t freeCount = 0;
        assert(!BlockMeta_IsCoalesceMe(current));
        assert(!BlockMeta_IsSuperblockMiddle(current));
        assert(!BlockMeta_IsSuperblockStartMe(current));
        if (BlockMeta_IsSimpleBlock(current)) {
            freeCount = Allocator_Sweep(&allocator, current, currentBlockStart, lineMetas);
            #ifdef DEBUG_PRINT
                printf("Sweeper_Sweep SimpleBlock %p %" PRIu32 "\n",
                       current, BlockMeta_GetBlockIndex(heap->blockMetaStart, current));
                fflush(stdout);
            #endif
        } else if (BlockMeta_IsSuperblockStart(current)) {
            size = BlockMeta_SuperblockSize(current);
            assert(size > 0);
            freeCount = LargeAllocator_Sweep(&largeAllocator, current, currentBlockStart, limit);
            #ifdef DEBUG_PRINT
                printf("Sweeper_Sweep Superblock(%" PRIu32 ") %p %" PRIu32 "\n",
                       size, current, BlockMeta_GetBlockIndex(heap->blockMetaStart, current));
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
                printf("Sweeper_Sweep FreeBlock %p %" PRIu32 "\n",
                       current, BlockMeta_GetBlockIndex(heap->blockMetaStart, current));
                fflush(stdout);
            #endif
        }
        // ignore superblock middle blocks, that superblock will be swept by someone else
        assert(size > 0);
        assert(freeCount <= size);
        if (lastFreeBlockStart == NULL) {
            if (freeCount > 0) {
                lastFreeBlockStart = current;
            }
        }
        if (lastFreeBlockStart != NULL && freeCount < size) {
            BlockMeta *freeLimit = current + freeCount;
            uint32_t totalSize = (uint32_t) (freeLimit - lastFreeBlockStart);
            if (lastFreeBlockStart == first || freeLimit >= limit) {
                // Free blocks in the start or the end
                // There may be some free blocks before this batch that needs to be coalesced with this block.
                BlockMeta_SetFlag(lastFreeBlockStart, block_coalesce_me);
                BlockMeta_SetSuperblockSize(lastFreeBlockStart, totalSize);
            } else {
                // Free blocks in the middle
                assert(totalSize > 0);
                BlockAllocator_AddFreeSuperblock(&blockAllocator, lastFreeBlockStart, totalSize);
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
        uint32_t totalSize = (uint32_t) (doneUntil - lastFreeBlockStart);
        assert(totalSize > 0);
        // There may be some free blocks after this batch that needs to be coalesced with this block.
        BlockMeta_SetFlag(lastFreeBlockStart, block_coalesce_me);
        BlockMeta_SetSuperblockSize(lastFreeBlockStart, totalSize);
    }

    // coalescing might be done by another thread
    // block_coalesce_me marks should be visible
    atomic_thread_fence(memory_order_seq_cst);

    *cursorDone = limitIdx;
}

uint_fast32_t Sweeper_minSweepCursor(Heap *heap) {
    uint_fast32_t min = heap->sweep.cursorDone;
    int gcThreadCount = heap->gcThreadCount;
    for (int i = 0; i < gcThreadCount; i++) {
        uint_fast32_t cursorDone = gcThreads[i].sweep.cursorDone;
        if (gcThreads[i].active && cursorDone < min) {
            min = cursorDone;
        }
    }
    return min;
}

void Sweeper_LazyCoalesce(Heap *heap) {
    // the previous coalesce is done and there is work
    BlockRangeVal coalesce = heap->coalesce;
    uint_fast32_t startIdx = BlockRange_Limit(coalesce);
    uint_fast32_t coalesceDoneIdx = BlockRange_First(coalesce);
    uint_fast32_t limitIdx = Sweeper_minSweepCursor(heap);
    assert(coalesceDoneIdx <= startIdx);
    BlockRangeVal newValue = BlockRange_Pack(coalesceDoneIdx, limitIdx);
    while (startIdx == coalesceDoneIdx && startIdx < limitIdx) {
        if (!atomic_compare_exchange_strong(&heap->coalesce, &coalesce, newValue)) {
            // coalesce is updated by atomic_compare_exchange_strong
            startIdx = BlockRange_Limit(coalesce);
            coalesceDoneIdx = BlockRange_First(coalesce);
            limitIdx = Sweeper_minSweepCursor(heap);
            newValue = BlockRange_Pack(coalesceDoneIdx, limitIdx);
            assert(coalesceDoneIdx <= startIdx);
            continue;
        }

        BlockMeta *lastFreeBlockStart = NULL;
        BlockMeta *lastCoalesceMe = NULL;
        BlockMeta *first = BlockMeta_GetFromIndex(heap->blockMetaStart, (uint32_t) startIdx);
        BlockMeta *current = first;
        BlockMeta *limit = BlockMeta_GetFromIndex(heap->blockMetaStart, (uint32_t) limitIdx);

        while (current < limit) {
            // updates lastFreeBlockStart and adds blocks
            if (lastFreeBlockStart == NULL) {
                if (BlockMeta_IsCoalesceMe(current)) {
                    lastFreeBlockStart = current;
                }
            } else {
                if (!BlockMeta_IsCoalesceMe(current)) {
                    BlockMeta *freeLimit = current;
                    uint32_t totalSize = (uint32_t) (freeLimit - lastFreeBlockStart);
                    BlockAllocator_AddFreeBlocks(&blockAllocator, lastFreeBlockStart, totalSize);
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
                // finish the LargeAllocator_Sweep in the case when the last block is not free
                BlockMeta_SetFlag(current, block_superblock_start);
                BlockMeta_SetSuperblockSize(current, 1);
            }

            current += size;
        }
        if (lastFreeBlockStart != NULL) {
            assert(current <= (BlockMeta *) heap->blockMetaEnd);
            assert(current >= limit);
            if (current == limit) {
                uint32_t totalSize = (uint32_t) (limit - lastFreeBlockStart);
                BlockAllocator_AddFreeBlocks(&blockAllocator, lastFreeBlockStart, totalSize);
            } else {
                // the last superblock crossed the limit
                // other sweepers still need to sweep it
                // add the part that is fully swept
                uint32_t totalSize = (uint32_t) (lastCoalesceMe - lastFreeBlockStart);
                assert(lastFreeBlockStart + totalSize <= limit);
                if (totalSize > 0) {
                    BlockAllocator_AddFreeBlocks(&blockAllocator, lastFreeBlockStart, totalSize);
                }
                // try to advance the sweep cursor past the superblock
                uint_fast32_t advanceTo = BlockMeta_GetBlockIndex(heap->blockMetaStart, current);
                uint_fast32_t sweepCursor = heap->sweep.cursor;
                while (sweepCursor < advanceTo) {
                    atomic_compare_exchange_strong(&heap->sweep.cursor, &sweepCursor, advanceTo);
                    // sweepCursor is updated by atomic_compare_exchange_strong
                }
                // retreat the coalesce cursor
                uint_fast32_t retreatTo = BlockMeta_GetBlockIndex(heap->blockMetaStart, lastCoalesceMe);
                heap->coalesce = BlockRange_Pack(retreatTo, retreatTo);
                // do no more to avoid infinite loops
                return;
            }
        }

        heap->coalesce = BlockRange_Pack(limitIdx, limitIdx);
    }
}

void Sweeper_sweepDone(Heap *heap) {
    Heap_GrowIfNeeded(heap);
    heap->postSweepDone = true;
    Stats *stats = heap->stats;
    if (stats != NULL) {
        uint64_t end_ns = scalanative_nano_time();
        Stats_RecordEvent(stats, event_collection, MUTATOR_THREAD_ID, stats->collection_start_ns, end_ns);
    }
}
