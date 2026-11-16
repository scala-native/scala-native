#if defined(SCALANATIVE_GC_IMMIX)

#include <stdio.h>
#include <memory.h>
#include <stdlib.h>
#include "Block.h"
#include "immix_commix/metadata/ObjectMeta.h"
#include "Allocator.h"

INLINE void Block_recycleUnmarkedBlock(Allocator *allocator,
                                       BlockMeta *blockMeta,
                                       word_t *blockStart) {
    memset(blockMeta, 0, sizeof(BlockMeta));
    // does not unmark in LineMetas because those are ignored by the allocator
    BlockAllocator_AddFreeBlocks(allocator->blockAllocator, blockMeta, 1);
    ObjectMeta_ClearBlockAt(Bytemap_Get(allocator->bytemap, blockStart));
}

// Free lines found in partially used blocks by the current sweep.
uint64_t Block_recycledFreeLines = 0;

/**
 * recycles a block and adds it to the allocator
 */
#ifdef SCALANATIVE_GC_STICKY
// A block nothing was allocated into since it was last swept holds only old
// objects, whose metadata a minor collection leaves as it is, and its free
// lines are still linked as that sweep left them.
#ifdef SCALANATIVE_GC_WRITE_BARRIER_VERIFY
// Checks what Block_recycleUntouched assumes: every object is old, and the
// holes linked from the block cover exactly its unmarked lines.
static void Block_verifyUntouched(Allocator *allocator, BlockMeta *blockMeta,
                                  word_t *blockStart, LineMeta *lineMetas) {
    ObjectMeta *meta = Bytemap_Get(allocator->bytemap, blockStart);
    for (size_t i = 0; i < WORDS_IN_BLOCK / ALLOCATION_ALIGNMENT_WORDS; i++) {
        if (!ObjectMeta_IsFree(&meta[i]) &&
            !ObjectMeta_IsOldUnlogged(&meta[i])) {
            fprintf(stderr,
                    "GC sticky collector: skipped block %p holds an object at "
                    "%p with state %d\n",
                    (void *)blockStart,
                    (void *)(blockStart + i * ALLOCATION_ALIGNMENT_WORDS),
                    (int)meta[i]);
            abort();
        }
    }
    bool linked[LINE_COUNT] = {false};
    int holes = 0;
    for (int line = BlockMeta_FirstFreeLine(blockMeta); line != LAST_HOLE;) {
        FreeLineMeta *hole =
            (FreeLineMeta *)Block_GetLineAddress(blockStart, line);
        bool bad = line < 0 || line >= LINE_COUNT || ++holes > LINE_COUNT ||
                   hole->size == 0 || line + hole->size > LINE_COUNT;
        for (int k = line; !bad && k < line + hole->size; k++) {
            bad = linked[k] || Line_IsMarked(&lineMetas[k]);
            linked[k] = true;
        }
        if (bad) {
            fprintf(stderr,
                    "GC sticky collector: skipped block %p has a bad hole at "
                    "line %d\n",
                    (void *)blockStart, line);
            abort();
        }
        line = hole->next;
    }
    for (int k = 0; k < LINE_COUNT; k++) {
        if (!linked[k] && !Line_IsMarked(&lineMetas[k])) {
            fprintf(stderr,
                    "GC sticky collector: skipped block %p does not link its "
                    "free line %d\n",
                    (void *)blockStart, k);
            abort();
        }
    }
}
#endif

static void Block_recycleUntouched(Allocator *allocator, BlockMeta *blockMeta,
                                   word_t *blockStart, LineMeta *lineMetas) {
#ifdef SCALANATIVE_GC_WRITE_BARRIER_VERIFY
    Block_verifyUntouched(allocator, blockMeta, blockStart, lineMetas);
#endif
    if (BlockMeta_FirstFreeLine(blockMeta) == LAST_HOLE)
        return;
    uint32_t freeLines = 0;
    for (int line = 0; line < LINE_COUNT; line++)
        freeLines += !Line_IsMarked(&lineMetas[line]);
    Block_recycledFreeLines += freeLines;
    BlockList_AddLast(&allocator->recycledBlocks, blockMeta);
    atomic_fetch_add_explicit(&allocator->recycledBlockCount, 1,
                              memory_order_relaxed);
}
#endif

void Block_Recycle(Allocator *allocator, BlockMeta *blockMeta,
                   word_t *blockStart, LineMeta *lineMetas, bool full) {
#ifdef SCALANATIVE_GC_STICKY
    if (!full && BlockMeta_IsMarked(blockMeta) &&
        !BlockMeta_IsTouched(blockMeta)) {
        Block_recycleUntouched(allocator, blockMeta, blockStart, lineMetas);
        return;
    }
    BlockMeta_SetTouched(blockMeta, false);
#endif

    // If the block is not marked, it means that it's completely free
    if (!BlockMeta_IsMarked(blockMeta)) {
        Block_recycleUnmarkedBlock(allocator, blockMeta, blockStart);
    } else {
        // If the block is marked, we need to recycle line by line
        assert(BlockMeta_IsMarked(blockMeta));
#ifndef SCALANATIVE_GC_STICKY
        // Sticky Immix keeps block and line marks until the next full
        // collection: they cover the old objects, which minor collections
        // neither trace nor free.
        BlockMeta_Unmark(blockMeta);
#endif
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
#ifndef SCALANATIVE_GC_STICKY
                Line_Unmark(lineMeta);
#endif
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
                    assert(lineIndex >= 0);
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
                Block_recycledFreeLines += size;
            }
        }
        // If there is no recyclable line, the block is unavailable
        if (lastRecyclable != NULL) {
            lastRecyclable->next = LAST_HOLE;
            BlockList_AddLast(&allocator->recycledBlocks, blockMeta);

            assert(BlockMeta_FirstFreeLine(blockMeta) >= 0);
            assert(BlockMeta_FirstFreeLine(blockMeta) < LINE_COUNT);
            atomic_fetch_add_explicit(&allocator->recycledBlockCount, 1,
                                      memory_order_relaxed);
        } else {
            // Block_recycleUntouched relies on this to tell a full block
            // from one whose holes are still linked.
            BlockMeta_SetFirstFreeLine(blockMeta, LAST_HOLE);
        }
    }
}

#endif
