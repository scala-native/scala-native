#if defined(SCALANATIVE_GC_IMMIX) && defined(SCALANATIVE_GC_STICKY)

#include <stdlib.h>
#include "Generations.h"
#include "Block.h"
#include "LargeAllocator.h"
#include "Marker.h"
#include "State.h"
#include "shared/Log.h"
#include "immix_commix/Settings.h"

// A minor collection that leaves less of the heap free than this makes the
// next collection full.
#define GENERATIONS_MIN_FREE_PERCENT 20

typedef struct {
    Object **items;
    size_t size;
    size_t capacity;
} ObjectList;

static void ObjectList_Push(ObjectList *list, Object *object) {
    if (list->size == list->capacity) {
        size_t capacity = list->capacity == 0 ? 64 : list->capacity * 2;
        Object **items = realloc(list->items, capacity * sizeof(Object *));
        if (items == NULL)
            Heap_exitWithOutOfMemory("for the old blob arrays");
        list->items = items;
        list->capacity = capacity;
    }
    list->items[list->size++] = object;
}

// Blob arrays are scanned conservatively and their contents are written
// without the barrier (continuation stack fragments), so every minor
// collection rescans the old ones. Those found young in a collection join
// them once it ends.
static ObjectList oldBlobArrays = {};
static ObjectList youngBlobArrays = {};

// Remembered objects of threads that exited since the last collection.
static RememberedSet orphanRemembered = {};
static mutex_t orphanRememberedLock;

// Old objects are freed only by full collections, and young survivors pin the
// lines they are scattered over, so the occupied part of the heap grows with
// every minor collection. The next collection is full once it is this many
// times what the last full collection left occupied.
// Below this many occupied lines (16 MB) growth does not matter.
#define GENERATIONS_MIN_FULL_LINES (16 * 1024 * 1024 / LINE_SIZE)
static double oldGrowth = 0.0;
static uint64_t usedLinesAfterFull = 0;

static bool nextCollectionFull = false;
// Set once by GC_STICKY_ALWAYS_FULL, for testing the full collection path.
static int alwaysFull = -1;

void Generations_Init(void) {
    mutex_init(&orphanRememberedLock);
    char *growth = getenv("GC_OLD_GENERATION_GROWTH");
    oldGrowth = growth != NULL ? atof(growth) : 0.0;
    if (!(oldGrowth > 1.0))
        oldGrowth = SCALANATIVE_GC_CONFIG_OLD_GENERATION_GROWTH;
}

bool Generations_ShouldCollectFull(Heap *heap, bool explicitCollection) {
    if (alwaysFull < 0)
        alwaysFull = getenv("GC_STICKY_ALWAYS_FULL") != NULL;
    return explicitCollection || nextCollectionFull || alwaysFull;
}

static void Generations_makeYoung(ObjectMeta *meta) {
    if (ObjectMeta_IsOld(meta))
        ObjectMeta_SetAllocated(meta);
}

void Generations_PrepareFull(Heap *heap) {
    BlockMeta *current = (BlockMeta *)heap->blockMetaStart;
    BlockMeta *end = (BlockMeta *)heap->blockMetaEnd;
    word_t *blockStart = heap->heapStart;
    LineMeta *lineMetas = (LineMeta *)heap->lineMetaStart;
    Bytemap *bytemap = heap->bytemap;
    while (current < end) {
        uint32_t size = 1;
        if (BlockMeta_IsSimpleBlock(current)) {
            // Old objects lie only in marked lines of marked blocks.
            if (BlockMeta_IsMarked(current)) {
                BlockMeta_Unmark(current);
                word_t *lineStart = blockStart;
                ObjectMeta *meta = Bytemap_Get(bytemap, lineStart);
                for (int line = 0; line < LINE_COUNT; line++) {
                    if (Line_IsMarked(&lineMetas[line])) {
                        Line_Unmark(&lineMetas[line]);
                        for (size_t i = 0;
                             i < WORDS_IN_LINE / ALLOCATION_ALIGNMENT_WORDS;
                             i++)
                            Generations_makeYoung(&meta[i]);
                    }
                    meta = Bytemap_NextLine(meta);
                }
            }
        } else if (BlockMeta_IsSuperblockStart(current)) {
            // Large objects start at the superblock or, when smaller than a
            // block, anywhere in its last block (see LargeAllocator_Sweep).
            size = BlockMeta_SuperblockSize(current);
            word_t *blockEnd = blockStart + WORDS_IN_BLOCK * size;
            Generations_makeYoung(Bytemap_Get(bytemap, blockStart));
            word_t *object =
                blockEnd - WORDS_IN_BLOCK + MIN_BLOCK_SIZE / WORD_SIZE;
            for (; object < blockEnd; object += MIN_BLOCK_SIZE / WORD_SIZE)
                Generations_makeYoung(Bytemap_Get(bytemap, object));
        }
        current += size;
        blockStart += WORDS_IN_BLOCK * size;
        lineMetas += LINE_COUNT * size;
    }

    MutatorThreads_foreach(mutatorThreads, node) {
        RememberedSet_Clear(&node->value->rememberedSet);
    }
    RememberedSet_Clear(&orphanRemembered);
    WriteBarrier_ClearCards(
        (size_t)((ubyte_t *)heap->heapEnd - (ubyte_t *)heap->heapStart));
    oldBlobArrays.size = 0;
}

// Traces the dirty cards of a large array the barrier remembered by card.
static void Generations_markCards(Heap *heap, Stack *stack,
                                  ArrayHeader *array) {
    uint8_t *cards = WriteBarrier_cards;
    word_t **elements = (word_t **)(array + 1);
    word_t **end = elements + array->length;
    cards[WriteBarrier_CardIndex(array)] &= ~WRITE_BARRIER_CARD_QUEUED;
    size_t last = WriteBarrier_CardIndex(end - 1);
    for (size_t card = WriteBarrier_CardIndex(elements); card <= last; card++) {
        if (!(cards[card] & WRITE_BARRIER_CARD_DIRTY))
            continue;
        cards[card] &= ~WRITE_BARRIER_CARD_DIRTY;
        word_t **from = (word_t **)WriteBarrier_CardStart(card);
        word_t **to = (word_t **)WriteBarrier_CardStart(card + 1);
        Marker_MarkSlots(heap, stack, from < elements ? elements : from,
                         to > end ? end : to);
    }
}

static void Generations_markRemembered(Heap *heap, Stack *stack,
                                       RememberedSet *set) {
    for (size_t i = 0; i < set->size; i++) {
        word_t entry = (word_t)set->items[i];
        if (entry & WRITE_BARRIER_CARDED_TAG) {
            Generations_markCards(
                heap, stack,
                (ArrayHeader *)(entry & ~WRITE_BARRIER_CARDED_TAG));
            continue;
        }
        Object *object = (Object *)entry;
        // The sweep skips blocks nothing was allocated into, so it may not
        // see this object: forget it here, so that the barrier remembers it
        // again on its next store of a young reference.
        ObjectMeta *meta = Bytemap_Get(heap->bytemap, (word_t *)object);
        if (ObjectMeta_IsOld(meta))
            ObjectMeta_SetOldUnlogged(meta);
        Marker_MarkRemembered(heap, stack, object);
    }
    RememberedSet_Clear(set);
}

void Generations_MarkMinorRoots(Heap *heap, Stack *stack) {
    size_t remembered = orphanRemembered.size;
    Generations_markRemembered(heap, stack, &orphanRemembered);
    MutatorThreads_foreach(mutatorThreads, node) {
        remembered += node->value->rememberedSet.size;
        Generations_markRemembered(heap, stack, &node->value->rememberedSet);
    }
    for (size_t i = 0; i < oldBlobArrays.size; i++)
        Marker_MarkRemembered(heap, stack, oldBlobArrays.items[i]);
    GC_LOG_DEBUG("Minor collection roots: %zu remembered objects, %zu old "
                 "blob arrays",
                 remembered, oldBlobArrays.size);
}

void Generations_NoteUnbarriered(Object *object) {
    ObjectList_Push(&youngBlobArrays, object);
}

void Generations_EndCollection(Heap *heap, bool full) {
    for (size_t i = 0; i < youngBlobArrays.size; i++)
        ObjectList_Push(&oldBlobArrays, youngBlobArrays.items[i]);
    youngBlobArrays.size = 0;

    uint64_t freeLines =
        (uint64_t)atomic_load(&blockAllocator.freeBlockCount) * LINE_COUNT +
        Block_recycledFreeLines + LargeAllocator_sweptFreeBytes / LINE_SIZE;
    uint64_t totalLines = (uint64_t)heap->blockCount * LINE_COUNT;
    uint64_t usedLines = totalLines - freeLines;
    if (full)
        usedLinesAfterFull = usedLines;
    uint64_t oldLimit = (uint64_t)(usedLinesAfterFull * oldGrowth);
    if (oldLimit < GENERATIONS_MIN_FULL_LINES)
        oldLimit = GENERATIONS_MIN_FULL_LINES;
    // After a full collection, the next one is minor: it may be the heap is
    // just full of live data, which a full collection cannot help with either.
    nextCollectionFull =
        !full && (freeLines * 100 < totalLines * GENERATIONS_MIN_FREE_PERCENT ||
                  usedLines > oldLimit);
    uint32_t recycledBlocks = 0;
    MutatorThreads_foreach(mutatorThreads, node) {
        recycledBlocks += node->value->allocator.recycledBlockCount;
    }
    GC_LOG_DEBUG("%s collection left %" PRIu64 " of %" PRIu64
                 " lines free (%u free blocks, %u recycled blocks with %" PRIu64
                 " free lines, %" PRIu64 " large bytes), next collection %s",
                 full ? "Full" : "Minor", freeLines, totalLines,
                 (uint32_t)atomic_load(&blockAllocator.freeBlockCount),
                 recycledBlocks, Block_recycledFreeLines,
                 LargeAllocator_sweptFreeBytes,
                 nextCollectionFull ? "full" : "minor");
}

void Generations_AdoptRememberedSet(RememberedSet *set) {
    if (set->size == 0)
        return;
    // The exiting thread is still managed, so no collection reads the set
    // meanwhile; the lock orders threads exiting together.
    mutex_lock(&orphanRememberedLock);
    for (size_t i = 0; i < set->size; i++)
        RememberedSet_Push(&orphanRemembered, set->items[i]);
    mutex_unlock(&orphanRememberedLock);
    RememberedSet_Clear(set);
}

#endif
