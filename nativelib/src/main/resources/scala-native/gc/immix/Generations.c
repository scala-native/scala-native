#if defined(SCALANATIVE_GC_IMMIX) && defined(SCALANATIVE_GC_STICKY)

#include <stdlib.h>
#include <string.h>
#include "Generations.h"
#include "BlockAllocator.h"
#include "immix_commix/Object.h"
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

// Evacuation (see Generations_Evacuate): the young objects the current minor
// collection marked, and the old objects and card ranges it traced, whose
// references the evacuation updates.
static ObjectList survivors = {};
static ObjectList tracedOld = {};
static ObjectList tracedCardRanges = {}; // pairs of slot addresses
#ifdef SCALANATIVE_GC_WRITE_BARRIER_VERIFY
// The evacuated originals, poisoned once references are updated, so that a
// reference the evacuation missed fails fast.
static ObjectList evacuatedFrom = {};
#define GENERATIONS_POISON 0xAB
#endif

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
        from = from < elements ? elements : from;
        to = to > end ? end : to;
        Marker_MarkSlots(heap, stack, from, to);
        if (Marker_evacuating) {
            ObjectList_Push(&tracedCardRanges, (Object *)from);
            ObjectList_Push(&tracedCardRanges, (Object *)to);
        }
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
        if (Marker_evacuating)
            ObjectList_Push(&tracedOld, object);
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

void Generations_NoteSurvivor(Object *object) {
    ObjectList_Push(&survivors, object);
}

// Copying can be disabled in the build (GarbageCollectorConfig.evacuation) or
// with GC_EVACUATION=0: a program that keeps addresses of heap objects in
// native memory needs that.
bool Generations_ShouldEvacuate(bool full) {
    static int enabled = -1;
    if (enabled < 0) {
        enabled = SCALANATIVE_GC_CONFIG_EVACUATION;
        const char *env = getenv("GC_EVACUATION");
        if (env != NULL) {
            if (env[0] == '0' || env[0] == 'f' || env[0] == 'F')
                enabled = 0;
            else if (env[0] == '1' || env[0] == 't' || env[0] == 'T')
                enabled = 1;
        }
    }
    return !full && enabled;
}

// Free blocks the evacuation copies survivors into, bump allocated. In
// practice these are the copy reserve, held back from the mutators.
typedef struct {
    word_t *cursor;
    word_t *limit;
} CopySpace;

// The copy reserve covers the survivors of the last minor collection with a
// margin, between these bounds.
#define GENERATIONS_MIN_COPY_RESERVE 4
#define GENERATIONS_MAX_COPY_RESERVE_DIVISOR 8
#define GENERATIONS_COPY_RESERVE_DECAY_DIVISOR 8

static word_t *Generations_copySpaceAlloc(Heap *heap, CopySpace *space,
                                          size_t size) {
    if (space->cursor == NULL ||
        (ubyte_t *)space->cursor + size > (ubyte_t *)space->limit) {
        BlockMeta *block = BlockAllocator_GetFreeBlockForSmallAllocation(
            &blockAllocator, true);
        if (block == NULL)
            return NULL;
        // Swept by this collection like a block a mutator allocated into.
        BlockMeta_SetFirstFreeLine(block, LAST_HOLE);
        BlockMeta_SetTouched(block, true);
        word_t *blockStart = BlockMeta_GetBlockStart(heap->blockMetaStart,
                                                     heap->heapStart, block);
        LineMeta *lines = Heap_LineMetaForWord(heap, blockStart);
        memset(lines, 0, LINE_COUNT * sizeof(LineMeta));
        space->cursor = blockStart;
        space->limit = Block_GetBlockEnd(blockStart);
    }
    word_t *start = space->cursor;
    space->cursor = (word_t *)((ubyte_t *)start + size);
    return start;
}

static inline void Generations_fixSlot(Heap *heap, word_t **slot) {
    word_t *target = *slot;
    if (Heap_IsWordInHeap(heap, target) &&
        ObjectMeta_IsForwarded(Bytemap_Get(heap->bytemap, target)))
        *slot = *(word_t **)target;
}

static void Generations_fixSlots(Heap *heap, word_t **from, word_t **to) {
    for (word_t **slot = from; slot < to; slot++)
        Generations_fixSlot(heap, slot);
}

// Updates the references of `object` to evacuated objects. Blob arrays and
// boxed pointers hold raw addresses, whose targets were pinned.
static void Generations_fixObject(Heap *heap, Object *object) {
#ifdef USES_LOCKWORD
    word_t lock = (word_t)object->lockWord;
    if (Field_isInflatedLock((Field_t)lock)) {
        word_t *monitor = Field_allignedLockRef((Field_t)lock);
        if (Heap_IsWordInHeap(heap, monitor) &&
            ObjectMeta_IsForwarded(Bytemap_Get(heap->bytemap, monitor)))
            object->lockWord = (word_t *)((word_t) * (word_t **)monitor |
                                          (lock & MONITOR_INFLATION_MARK_MASK));
    }
#endif
    if (Object_IsArray(object)) {
        if (Rtti_ArrayScan(object->rtti) == RTTI_ARRAY_SCAN_REFERENCES) {
            ArrayHeader *array = (ArrayHeader *)object;
            word_t **elements = (word_t **)(array + 1);
            Generations_fixSlots(heap, elements, elements + array->length);
        }
    } else {
        // The referent is not among the reference fields; it survived, or
        // the slot was cleared already.
        if (Object_IsWeakReference(object))
            Generations_fixSlot(heap,
                                (word_t **)WeakReference_ReferentField(object));
        int32_t *offsets = object->rtti->refFieldOffsets;
        for (int i = 0; offsets[i] != -1; i++)
            Generations_fixSlot(heap,
                                (word_t **)((int8_t *)object + offsets[i]));
    }
}

static void Generations_sizeCopyReserve(size_t survivorBytes) {
    uint64_t blocks = ((uint64_t)survivorBytes * 5 / 4 + BLOCK_TOTAL_SIZE - 1) /
                          BLOCK_TOTAL_SIZE +
                      1;
    // A collection starts when the mutators run out of free blocks, so the
    // reserve is all the evacuation can copy into. Survivor volumes swing
    // from one collection to the next: shrink the reserve slowly, or a small
    // collection leaves the next large one without room.
    uint64_t decayed =
        BlockAllocator_copyReserve -
        BlockAllocator_copyReserve / GENERATIONS_COPY_RESERVE_DECAY_DIVISOR;
    if (blocks < decayed)
        blocks = decayed;
    uint64_t max = heap.blockCount / GENERATIONS_MAX_COPY_RESERVE_DIVISOR;
    if (blocks > max)
        blocks = max;
    if (blocks < GENERATIONS_MIN_COPY_RESERVE)
        blocks = GENERATIONS_MIN_COPY_RESERVE;
    BlockAllocator_copyReserve = (uint32_t)blocks;
}

void Generations_Evacuate(Heap *heap) {
    Bytemap *bytemap = heap->bytemap;
    CopySpace space = {NULL, NULL};
    size_t copied = 0, copiedBytes = 0, kept = 0, keptBytes = 0;
    bool spaceLeft = true;
    for (size_t i = 0; i < survivors.size; i++) {
        Object *object = survivors.items[i];
        ObjectMeta *meta = Bytemap_Get(bytemap, (word_t *)object);
        BlockMeta *block = Block_GetBlockMeta(
            heap->blockMetaStart, heap->heapStart, (word_t *)object);
        word_t *copy = NULL;
        size_t size = 0;
        if (spaceLeft && !ObjectMeta_IsMarkedPinned(meta) &&
            !BlockMeta_ContainsLargeObjects(block)) {
            size = Object_Size(object);
            copy = Generations_copySpaceAlloc(heap, &space, size);
            spaceLeft = copy != NULL;
        }
        if (copy == NULL) {
            Object_MarkLines(heap, object);
            kept++;
            if (!BlockMeta_ContainsLargeObjects(block))
                keptBytes += Object_Size(object);
            continue;
        }
        memcpy(copy, object, size);
        ObjectMeta_SetMarked(Bytemap_Get(bytemap, copy));
        Object_MarkLines(heap, (Object *)copy);
        *(word_t **)object = copy;
        ObjectMeta_SetForwarded(meta);
#ifdef SCALANATIVE_GC_WRITE_BARRIER_VERIFY
        ObjectList_Push(&evacuatedFrom, object);
#endif
        survivors.items[i] = (Object *)copy;
        copied++;
        copiedBytes += size;
    }

    if (copied > 0) {
        for (size_t i = 0; i < survivors.size; i++)
            Generations_fixObject(heap, survivors.items[i]);
        for (size_t i = 0; i < tracedOld.size; i++)
            Generations_fixObject(heap, tracedOld.items[i]);
        for (size_t i = 0; i + 1 < tracedCardRanges.size; i += 2)
            Generations_fixSlots(heap, (word_t **)tracedCardRanges.items[i],
                                 (word_t **)tracedCardRanges.items[i + 1]);
    }
#ifdef SCALANATIVE_GC_WRITE_BARRIER_VERIFY
    for (size_t i = 0; i < evacuatedFrom.size; i++) {
        Object *original = evacuatedFrom.items[i];
        Object *copy = *(Object **)original;
        memset(original, GENERATIONS_POISON, Object_Size(copy));
    }
    evacuatedFrom.size = 0;
#endif
    Generations_sizeCopyReserve(copiedBytes + keptBytes);
    GC_LOG_DEBUG("Evacuation copied %zu objects (%zu bytes), kept %zu in "
                 "place (%zu bytes)%s, copy reserve now %u blocks",
                 copied, copiedBytes, kept, keptBytes,
                 spaceLeft ? "" : ", ran out of free blocks",
                 BlockAllocator_copyReserve);
    survivors.size = 0;
    tracedOld.size = 0;
    tracedCardRanges.size = 0;
}

void Generations_NoteUnbarriered(Object *object) {
    ObjectList_Push(&youngBlobArrays, object);
}

void Generations_EndCollection(Heap *heap, bool full) {
    // Before the first minor collection: an eighth of the heap.
    if (BlockAllocator_copyReserve == 0 && Generations_ShouldEvacuate(false))
        Generations_sizeCopyReserve((size_t)heap->blockCount *
                                    BLOCK_TOTAL_SIZE /
                                    GENERATIONS_MAX_COPY_RESERVE_DIVISOR);
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
