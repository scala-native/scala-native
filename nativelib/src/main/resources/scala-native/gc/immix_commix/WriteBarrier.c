#if (defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX)) &&       \
    defined(SCALANATIVE_GC_WRITE_BARRIER)

#include <stdio.h>
#include <stdlib.h>
#include <wchar.h>
#include <string.h>
#include "shared/Log.h"
#include "immix_commix/WriteBarrier.h"
#include "immix_commix/CollectorState.h"
#include "immix_commix/Object.h"

word_t *scalanative_GC_barrier_heap_start;
size_t scalanative_GC_barrier_heap_size;
ObjectMeta *scalanative_GC_barrier_bytemap;

#ifdef SCALANATIVE_GC_STICKY
uint8_t *WriteBarrier_cards;
#endif

void WriteBarrier_Init(word_t *heapStart, size_t heapSize, ObjectMeta *data) {
    scalanative_GC_barrier_heap_start = heapStart;
    scalanative_GC_barrier_heap_size = heapSize;
    scalanative_GC_barrier_bytemap = data;
#ifdef SCALANATIVE_GC_STICKY
    WriteBarrier_cards = calloc((heapSize >> WRITE_BARRIER_CARD_SHIFT) + 1, 1);
    if (WriteBarrier_cards == NULL)
        Heap_exitWithOutOfMemory("for the write barrier card table");
#endif
}

#ifdef SCALANATIVE_GC_STICKY
void WriteBarrier_ClearCards(size_t heapBytes) {
    memset(WriteBarrier_cards, 0, (heapBytes >> WRITE_BARRIER_CARD_SHIFT) + 1);
}

// Such arrays are large objects, which start at MIN_BLOCK_SIZE boundaries, so
// no other object shares their cards.
static_assert(WRITE_BARRIER_CARD_ARRAY_MIN_LENGTH * sizeof(word_t) >=
                  LARGE_BLOCK_SIZE,
              "card marked arrays must be large objects");
static_assert(MIN_BLOCK_SIZE % (1 << WRITE_BARRIER_CARD_SHIFT) == 0,
              "large objects must start at card boundaries");

static inline bool WriteBarrier_isCardedArray(Object *object) {
    return Object_IsArray(object) &&
           Rtti_ArrayScan(object->rtti) == RTTI_ARRAY_SCAN_REFERENCES &&
           ((ArrayHeader *)object)->length >=
               WRITE_BARRIER_CARD_ARRAY_MIN_LENGTH;
}
#endif

void RememberedSet_Push(RememberedSet *set, word_t *object) {
    if (set->size == set->capacity) {
        size_t capacity = set->capacity == 0 ? 256 : set->capacity * 2;
        word_t **items = realloc(set->items, capacity * sizeof(word_t *));
        if (items == NULL)
            Heap_exitWithOutOfMemory("for the remembered set");
        set->items = items;
        set->capacity = capacity;
    }
    set->items[set->size++] = object;
}

void RememberedSet_Clear(RememberedSet *set) { set->size = 0; }

// Remembers a heap object that is old and not remembered yet. Racing stores
// from two threads may both remember the object; the collector tolerates
// duplicates.
static inline void WriteBarrier_remember(word_t *object, ObjectMeta *meta) {
    if (!ObjectMeta_IsOldUnlogged(meta))
        return;
    ObjectMeta_SetOldLogged(meta);
    RememberedSet_Push(&currentMutatorThread->rememberedSet, object);
}

// Called by generated code for an old object that is not remembered yet.
WRITE_BARRIER_SLOW_PATH void scalanative_GC_write_barrier_slow(word_t *object) {
#ifdef SCALANATIVE_GC_BARRIER_WINDOW_BITS
    // The fast path also sends objects outside the heap here.
    if (!Heap_IsWordInHeap(&heap, object))
        return;
#endif
    WriteBarrier_remember(object, Bytemap_Get(heap.bytemap, object));
}

// Called by generated code for an old object that is not remembered yet,
// when the address of the written slot is known.
WRITE_BARRIER_SLOW_PATH void scalanative_GC_write_barrier_elem(word_t *object,
                                                               word_t *slot) {
#ifdef SCALANATIVE_GC_BARRIER_WINDOW_BITS
    if (!Heap_IsWordInHeap(&heap, object))
        return;
#endif
    ObjectMeta *meta = Bytemap_Get(heap.bytemap, object);
#ifdef SCALANATIVE_GC_STICKY
    if (ObjectMeta_IsOldUnlogged(meta) &&
        WriteBarrier_isCardedArray((Object *)object)) {
        uint8_t *card = &WriteBarrier_cards[WriteBarrier_CardIndex(slot)];
        if (__atomic_load_n(card, __ATOMIC_RELAXED) & WRITE_BARRIER_CARD_DIRTY)
            return;
        __atomic_fetch_or(card, WRITE_BARRIER_CARD_DIRTY, __ATOMIC_RELAXED);
        uint8_t *header = &WriteBarrier_cards[WriteBarrier_CardIndex(object)];
        if (!(__atomic_fetch_or(header, WRITE_BARRIER_CARD_QUEUED,
                                __ATOMIC_RELAXED) &
              WRITE_BARRIER_CARD_QUEUED))
            RememberedSet_Push(
                &currentMutatorThread->rememberedSet,
                (word_t *)((word_t)object | WRITE_BARRIER_CARDED_TAG));
        return;
    }
#endif
    WriteBarrier_remember(object, meta);
}

// Finds the last object that starts at or before `slot`, a heap address, or
// NULL if there is none in its block. Only the first allocation granule of an
// object has a non-free ObjectMeta byte, and a large object starts at a block
// boundary.
static word_t *WriteBarrier_objectBefore(word_t *slot) {
    BlockMeta *blockMeta =
        Block_GetBlockMeta(heap.blockMetaStart, heap.heapStart, slot);
    int stride;
    word_t *blockStart;
    word_t *current;
    if (BlockMeta_ContainsLargeObjects(blockMeta)) {
        stride = MIN_BLOCK_SIZE / ALLOCATION_ALIGNMENT;
        BlockMeta *superblockStart =
            BlockMeta_GetSuperblockStart(heap.blockMetaStart, blockMeta);
        blockStart = BlockMeta_GetBlockStart(heap.blockMetaStart,
                                             heap.heapStart, superblockStart);
        current = (word_t *)((word_t)slot & LARGE_BLOCK_MASK);
    } else {
        stride = 1;
        blockStart = Block_GetBlockStartForWord(slot);
        current = (word_t *)((word_t)slot & ALLOCATION_ALIGNMENT_INVERSE_MASK);
    }
    ObjectMeta *meta = Bytemap_Get(heap.bytemap, current);
    while (ObjectMeta_IsFree(meta)) {
        current -= ALLOCATION_ALIGNMENT_WORDS * stride;
        if (current < blockStart)
            return NULL;
        meta -= stride;
    }
    return current;
}

// Called by generated code before a reference is stored through a raw
// pointer into the heap range, when the containing object is not known:
// atomic operations on fields and array elements, and stores through Ptr.
WRITE_BARRIER_SLOW_PATH void scalanative_GC_write_barrier_slot(word_t *slot) {
    word_t *object = WriteBarrier_objectBefore(slot);
    if (object == NULL)
        return;
    // Most stores are into objects that need no remembering: check that
    // before the size, which tells whether the object covers the slot.
    ObjectMeta *meta = Bytemap_Get(heap.bytemap, object);
    if (ObjectMeta_IsOldUnlogged(meta) &&
        (ubyte_t *)slot < (ubyte_t *)object + Object_Size((Object *)object))
        WriteBarrier_remember(object, meta);
}

#ifdef SCALANATIVE_GC_WRITE_BARRIER_VERIFY

// Objects allocated since the previous collection keep om_allocated, all
// others are old.
static bool WriteBarrier_isYoung(word_t *field) {
    return Heap_IsWordInHeap(&heap, field) && Bytemap_isPtrAligned(field) &&
           ObjectMeta_IsAllocated(Bytemap_Get(heap.bytemap, field));
}

// Counts the references of `object` that match, and gives the offset of the
// first one. Blob arrays and boxed pointers hold raw addresses, which need
// not be object starts, and are not covered. Elements of a large array on a
// dirty card are left out if asked: the collector traces those.
static size_t WriteBarrier_countReferences(Object *object, size_t *firstOffset,
                                           bool (*matches)(word_t *),
                                           bool skipDirtyCards) {
    size_t found = 0;
    *firstOffset = 0;
#ifdef USES_LOCKWORD
    Field_t lock = object->lockWord;
    if (Field_isInflatedLock(lock) && matches(Field_allignedLockRef(lock))) {
        *firstOffset = (int8_t *)&object->lockWord - (int8_t *)object;
        found++;
    }
#endif
    if (Object_IsArray(object)) {
        if (Rtti_ArrayScan(object->rtti) == RTTI_ARRAY_SCAN_REFERENCES) {
            ArrayHeader *array = (ArrayHeader *)object;
            word_t **elements = (word_t **)(array + 1);
            for (size_t i = 0; i < array->length; i++) {
#ifdef SCALANATIVE_GC_STICKY
                if (skipDirtyCards && WriteBarrier_isCardedArray(object) &&
                    (WriteBarrier_cards[WriteBarrier_CardIndex(&elements[i])] &
                     WRITE_BARRIER_CARD_DIRTY))
                    continue;
#endif
                if (matches(elements[i]) && found++ == 0)
                    *firstOffset = (int8_t *)&elements[i] - (int8_t *)object;
            }
        }
    } else {
        int32_t *offsets = object->rtti->refFieldOffsets;
        for (int i = 0; offsets[i] != LAST_FIELD_OFFSET; i++) {
            size_t offset = (size_t)offsets[i];
            Field_t field = *(Field_t *)((int8_t *)object + offset);
            if (matches(field) && found++ == 0)
                *firstOffset = offset;
        }
    }
    return found;
}

void WriteBarrier_VerifyBeforeCollection(void) {
    ObjectMeta *start = Bytemap_Get(heap.bytemap, heap.heapStart);
    ObjectMeta *end =
        start + (heap.heapEnd - heap.heapStart) / ALLOCATION_ALIGNMENT_WORDS;
    size_t remembered = 0, missedObjects = 0, missedReferences = 0;
    word_t *current = heap.heapStart;
    for (ObjectMeta *meta = start; meta < end;
         meta++, current += ALLOCATION_ALIGNMENT_WORDS) {
        if (ObjectMeta_IsOldLogged(meta)) {
            remembered++;
        } else if (ObjectMeta_IsOldUnlogged(meta)) {
            Object *object = (Object *)current;
            size_t offset;
            size_t missed = WriteBarrier_countReferences(
                object, &offset, WriteBarrier_isYoung, true);
            if (missed > 0) {
                if (missedObjects < 16) {
                    wchar_t *name = Object_nameWString(object);
                    fprintf(stderr,
                            "GC write barrier: old %ls at %p references %zu "
                            "young objects, first at offset %zu, but was not "
                            "remembered\n",
                            name, (void *)object, missed, offset);
                    free(name);
                }
                missedObjects++;
                missedReferences += missed;
            }
        }
    }
    if (missedObjects > 0) {
        fprintf(stderr,
                "GC write barrier: %zu old objects with %zu references to "
                "young objects were not remembered\n",
                missedObjects, missedReferences);
        abort();
    }
#ifndef SCALANATIVE_GC_STICKY
    // Only after all objects are checked: the collector itself does not know
    // old objects.
    for (ObjectMeta *meta = start; meta < end; meta++) {
        if (ObjectMeta_IsOld(meta))
            ObjectMeta_SetAllocated(meta);
    }
    MutatorThreads_foreach(mutatorThreads, node) {
        RememberedSet_Clear(&node->value->rememberedSet);
    }
#endif
    GC_LOG_DEBUG("Write barrier verified, %zu objects remembered", remembered);
}

#ifdef SCALANATIVE_GC_STICKY
// A reference from a survivor to an object the collection freed.
static bool WriteBarrier_isDangling(word_t *field) {
    return Heap_IsWordInHeap(&heap, field) && Bytemap_isPtrAligned(field) &&
           !ObjectMeta_IsOld(Bytemap_Get(heap.bytemap, field));
}

// Every survivor is old after a sticky collection; none may reference an
// object it freed.
void WriteBarrier_VerifyAfterCollection(void) {
    ObjectMeta *start = Bytemap_Get(heap.bytemap, heap.heapStart);
    ObjectMeta *end =
        start + (heap.heapEnd - heap.heapStart) / ALLOCATION_ALIGNMENT_WORDS;
    size_t survivors = 0, badObjects = 0, badReferences = 0;
    word_t *current = heap.heapStart;
    for (ObjectMeta *meta = start; meta < end;
         meta++, current += ALLOCATION_ALIGNMENT_WORDS) {
        if (ObjectMeta_IsAllocated(meta) || ObjectMeta_IsMarked(meta)) {
            fprintf(stderr,
                    "GC sticky collector: object at %p has state %d after the "
                    "sweep\n",
                    (void *)current, (int)*meta);
            abort();
        }
        if (!ObjectMeta_IsOld(meta))
            continue;
        survivors++;
        Object *object = (Object *)current;
        size_t offset;
        size_t bad = WriteBarrier_countReferences(
            object, &offset, WriteBarrier_isDangling, false);
        if (bad > 0) {
            if (badObjects < 16) {
                wchar_t *name = Object_nameWString(object);
                fprintf(stderr,
                        "GC sticky collector: %ls at %p references %zu freed "
                        "objects, first at offset %zu\n",
                        name, (void *)object, bad, offset);
                free(name);
            }
            badObjects++;
            badReferences += bad;
        }
    }
    if (badObjects > 0) {
        fprintf(stderr,
                "GC sticky collector: %zu survivors with %zu references to "
                "freed objects\n",
                badObjects, badReferences);
        abort();
    }
    GC_LOG_DEBUG("Sticky collection verified, %zu survivors", survivors);
}
#else
// Ages the survivors, so that the next verification sees which objects were
// allocated in between.
void WriteBarrier_VerifyAfterCollection(void) {
    ObjectMeta *meta = Bytemap_Get(heap.bytemap, heap.heapStart);
    ObjectMeta *end =
        meta + (heap.heapEnd - heap.heapStart) / ALLOCATION_ALIGNMENT_WORDS;
    for (; meta < end; meta++) {
        if (ObjectMeta_IsAllocated(meta))
            ObjectMeta_SetOldUnlogged(meta);
    }
}
#endif

#endif // SCALANATIVE_GC_WRITE_BARRIER_VERIFY

#endif
