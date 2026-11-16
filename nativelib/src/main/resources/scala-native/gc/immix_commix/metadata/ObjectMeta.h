#ifndef IMMIX_COMMIX_OBJECTMETA_H
#define IMMIX_COMMIX_OBJECTMETA_H

#include <stddef.h>
#include <stdbool.h>
#include "immix_commix/CommonConstants.h"
#include "shared/GCTypes.h"

typedef enum {
    om_free = 0x0,
    om_placeholder = 0x1,
    om_allocated = 0x2,
    om_marked = 0x4,
    // Write barrier states, see immix_commix/WriteBarrier.h. Only the sticky
    // Immix collector (SCALANATIVE_GC_STICKY) and the barrier verifier produce
    // them: an object that survived a collection is old.
    om_old_unlogged = 0x8,
    om_old_logged = 0x9,
} Flag;

typedef ubyte_t ObjectMeta;

static inline bool ObjectMeta_IsFree(ObjectMeta *metadata) {
    return *metadata == om_free;
}

static inline bool ObjectMeta_IsPlaceholder(ObjectMeta *metadata) {
    return *metadata == om_placeholder;
}

static inline bool ObjectMeta_IsAllocated(ObjectMeta *metadata) {
    return *metadata == om_allocated;
}

static inline bool ObjectMeta_IsMarked(ObjectMeta *metadata) {
    return *metadata == om_marked;
}

static inline bool ObjectMeta_IsOldUnlogged(ObjectMeta *metadata) {
    return *metadata == om_old_unlogged;
}

static inline bool ObjectMeta_IsOld(ObjectMeta *metadata) {
    return *metadata == om_old_unlogged || *metadata == om_old_logged;
}

// Survives the current collection: marked in it, or old and so not traced by
// a minor collection.
static inline bool ObjectMeta_IsLive(ObjectMeta *metadata) {
    return ObjectMeta_IsMarked(metadata) || ObjectMeta_IsOld(metadata);
}

static inline void ObjectMeta_SetOldUnlogged(ObjectMeta *metadata) {
    *metadata = om_old_unlogged;
}

static inline void ObjectMeta_SetOldLogged(ObjectMeta *metadata) {
    *metadata = om_old_logged;
}

static inline void ObjectMeta_SetFree(ObjectMeta *metadata) {
    *metadata = om_free;
}

static inline void ObjectMeta_SetPlaceholder(ObjectMeta *metadata) {
    *metadata = om_placeholder;
}

static inline void ObjectMeta_SetAllocated(ObjectMeta *metadata) {
    *metadata = om_allocated;
}

static inline void ObjectMeta_SetMarked(ObjectMeta *metadata) {
    *metadata = om_marked;
}

static inline void ObjectMeta_ClearLineAt(ObjectMeta *cursor) {
    for (size_t i = 0; i < WORDS_IN_LINE / ALLOCATION_ALIGNMENT_WORDS; i++) {
        ObjectMeta_SetFree(&cursor[i]);
    }
}

static inline void ObjectMeta_ClearBlockAt(ObjectMeta *cursor) {
    for (size_t i = 0; i < WORDS_IN_BLOCK / ALLOCATION_ALIGNMENT_WORDS; i++) {
        ObjectMeta_SetFree(&cursor[i]);
    }
}

static inline void ObjectMeta_Sweep(ObjectMeta *cursor) {
#ifdef SCALANATIVE_GC_STICKY
    // Survivors become old, and remembered objects are forgotten: the
    // collection traced what they referenced.
    if (ObjectMeta_IsLive(cursor))
        ObjectMeta_SetOldUnlogged(cursor);
    else
        ObjectMeta_SetFree(cursor);
#else
    if (ObjectMeta_IsMarked(cursor))
        ObjectMeta_SetAllocated(cursor);
    else
        ObjectMeta_SetFree(cursor);
#endif
}

static inline void ObjectMeta_SweepLineAt(ObjectMeta *data) {
    for (size_t i = 0; i < WORDS_IN_LINE / ALLOCATION_ALIGNMENT_WORDS; i++) {
        ObjectMeta_Sweep(&data[i]);
    }
}

#ifdef GC_ASSERTIONS
static inline void ObjectMeta_AssertIsValidAllocation(ObjectMeta *start,
                                                      size_t size) {
    ObjectMeta *limit = start + (size / ALLOCATION_ALIGNMENT);
    for (ObjectMeta *current = start; current < limit; current++) {
        assert(ObjectMeta_IsFree(current) || ObjectMeta_IsPlaceholder(current));
    }
}
#endif

#endif // IMMIX_COMMIX_OBJECTMETA_H