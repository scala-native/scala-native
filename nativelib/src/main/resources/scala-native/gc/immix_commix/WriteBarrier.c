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

// Called by generated code for an old object that is not remembered yet.
// Racing stores from two threads may both remember the object; the
// collector tolerates duplicates.
WRITE_BARRIER_SLOW_PATH void scalanative_GC_write_barrier_slow(word_t *object) {
#ifdef SCALANATIVE_GC_BARRIER_WINDOW_BITS
    // The fast path also sends objects outside the heap here.
    if ((size_t)((word_t)object - (word_t)scalanative_GC_barrier_heap_start) >=
        scalanative_GC_barrier_heap_size)
        return;
#endif
    ObjectMeta *meta = Bytemap_Get(heap.bytemap, object);
    if (!ObjectMeta_IsOldUnlogged(meta))
        return;
    ObjectMeta_SetOldLogged(meta);
    RememberedSet_Push(&currentMutatorThread->rememberedSet, object);
}

// Called by generated code for an old object that is not remembered yet,
// when the address of the written slot is known.
WRITE_BARRIER_SLOW_PATH void scalanative_GC_write_barrier_elem(word_t *object,
                                                               word_t *slot) {
#ifdef SCALANATIVE_GC_STICKY
#ifdef SCALANATIVE_GC_BARRIER_WINDOW_BITS
    if ((size_t)((word_t)object - (word_t)scalanative_GC_barrier_heap_start) >=
        scalanative_GC_barrier_heap_size)
        return;
#endif
    if (ObjectMeta_IsOldUnlogged(Bytemap_Get(heap.bytemap, object)) &&
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
    scalanative_GC_write_barrier_slow(object);
}

// Finds the object containing `slot`, a heap address, or NULL if no object
// covers it. Only the first allocation granule of an object has a non-free
// ObjectMeta byte, and a large object starts at a block boundary.
static word_t *WriteBarrier_containingObject(word_t *slot) {
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
    if (ObjectMeta_IsPlaceholder(meta) ||
        (ubyte_t *)slot >= (ubyte_t *)current + Object_Size((Object *)current))
        return NULL;
    return current;
}

// Called by generated code before a reference is stored through a raw
// pointer into the heap range, when the containing object is not known:
// atomic operations on fields and array elements, and stores through Ptr.
WRITE_BARRIER_SLOW_PATH void scalanative_GC_write_barrier_slot(word_t *slot) {
    word_t *object = WriteBarrier_containingObject(slot);
    if (object != NULL)
        scalanative_GC_write_barrier_slow(object);
}

#ifdef SCALANATIVE_GC_STICKY
// Called by generated code (GC.pin) before the address of an object is used
// as a value, such as its identity hash, when its ObjectMeta byte may be
// om_allocated: a young object is pinned so that its address never changes.
// Old objects never move.
void scalanative_GC_pin_slow(word_t *object) {
    if (!Heap_IsWordInHeap(&heap, object))
        return;
    ObjectMeta *meta = Bytemap_Get(heap.bytemap, object);
    if (*meta == om_allocated)
        *meta = om_allocated_pinned;
}
#endif

#ifdef SCALANATIVE_GC_WRITE_BARRIER_VERIFY

// Objects allocated since the previous collection keep om_allocated, all
// others are old.
static bool WriteBarrier_isYoung(word_t *field) {
    return Heap_IsWordInHeap(&heap, field) && Bytemap_isPtrAligned(field) &&
           ObjectMeta_IsAllocated(Bytemap_Get(heap.bytemap, field));
}

// Counts references from `object` to young objects. Conservatively scanned
// blob arrays and boxed pointers are not covered by the barrier yet.
static size_t WriteBarrier_youngReferences(Object *object,
                                           size_t *firstOffset) {
    size_t found = 0;
    *firstOffset = 0;
#ifdef USES_LOCKWORD
    Field_t lock = object->lockWord;
    if (Field_isInflatedLock(lock) &&
        WriteBarrier_isYoung(Field_allignedLockRef(lock))) {
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
                if (WriteBarrier_isCardedArray(object) &&
                    (WriteBarrier_cards[WriteBarrier_CardIndex(&elements[i])] &
                     WRITE_BARRIER_CARD_DIRTY))
                    continue;
#endif
                if (WriteBarrier_isYoung(elements[i]) && found++ == 0)
                    *firstOffset = (int8_t *)&elements[i] - (int8_t *)object;
            }
        }
    } else {
        int32_t *offsets = object->rtti->refFieldOffsets;
        for (int i = 0; offsets[i] != -1; i++) {
            size_t offset = (size_t)offsets[i];
            Field_t field = *(Field_t *)((int8_t *)object + offset);
            if (WriteBarrier_isYoung(field) && found++ == 0)
                *firstOffset = offset;
        }
    }
    return found;
}

void WriteBarrier_VerifyBeforeCollection(void) {
    ObjectMeta *start = Bytemap_Get(heap.bytemap, heap.heapStart);
    ObjectMeta *end =
        start + (heap.heapEnd - heap.heapStart) / ALLOCATION_ALIGNMENT_WORDS;
    size_t remembered = 0, pinned = 0, missedObjects = 0, missedReferences = 0;
    word_t *current = heap.heapStart;
    for (ObjectMeta *meta = start; meta < end;
         meta++, current += ALLOCATION_ALIGNMENT_WORDS) {
        if (*meta == om_allocated_pinned) {
            pinned++;
        } else if (*meta == om_old_logged) {
            remembered++;
        } else if (*meta == om_old_unlogged) {
            Object *object = (Object *)current;
            size_t offset;
            size_t missed = WriteBarrier_youngReferences(object, &offset);
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
        if (*meta == om_old_logged || *meta == om_old_unlogged)
            ObjectMeta_SetAllocated(meta);
    }
    MutatorThreads_foreach(mutatorThreads, node) {
        RememberedSet_Clear(&node->value->rememberedSet);
    }
#endif
    GC_LOG_DEBUG("Write barrier verified, %zu objects remembered, %zu young "
                 "objects pinned",
                 remembered, pinned);
}

#ifdef SCALANATIVE_GC_STICKY
// A reference from a survivor to an object the collection freed.
static bool WriteBarrier_isDangling(word_t *field) {
    return Heap_IsWordInHeap(&heap, field) && Bytemap_isPtrAligned(field) &&
           !ObjectMeta_IsOld(Bytemap_Get(heap.bytemap, field));
}

// Counts references from `object` to objects that are not live. Blob arrays
// and boxed pointers hold raw addresses, which need not be object starts.
static size_t WriteBarrier_danglingReferences(Object *object,
                                              size_t *firstOffset) {
    size_t found = 0;
    *firstOffset = 0;
#ifdef USES_LOCKWORD
    Field_t lock = object->lockWord;
    if (Field_isInflatedLock(lock) &&
        WriteBarrier_isDangling(Field_allignedLockRef(lock))) {
        *firstOffset = (int8_t *)&object->lockWord - (int8_t *)object;
        found++;
    }
#endif
    if (Object_IsArray(object)) {
        if (Rtti_ArrayScan(object->rtti) == RTTI_ARRAY_SCAN_REFERENCES) {
            ArrayHeader *array = (ArrayHeader *)object;
            word_t **elements = (word_t **)(array + 1);
            for (size_t i = 0; i < array->length; i++) {
                if (WriteBarrier_isDangling(elements[i]) && found++ == 0)
                    *firstOffset = (int8_t *)&elements[i] - (int8_t *)object;
            }
        }
    } else {
        int32_t *offsets = object->rtti->refFieldOffsets;
        for (int i = 0; offsets[i] != -1; i++) {
            size_t offset = (size_t)offsets[i];
            Field_t field = *(Field_t *)((int8_t *)object + offset);
            if (WriteBarrier_isDangling(field) && found++ == 0)
                *firstOffset = offset;
        }
    }
    return found;
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
        size_t bad = WriteBarrier_danglingReferences(object, &offset);
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
            *meta = om_old_unlogged;
    }
}
#endif

#endif // SCALANATIVE_GC_WRITE_BARRIER_VERIFY

#endif
