#ifndef IMMIX_OBJECTMETA_H
#define IMMIX_OBJECTMETA_H

#include <stddef.h>
#include <stdbool.h>
#include "immix_commix/CommonConstants.h"
#include "shared/GCTypes.h"

typedef enum {
    om_free = 0x0,
    om_placeholder = 0x1,
    om_allocated = 0x2,
    om_marked = 0x4,
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
    if (ObjectMeta_IsMarked(cursor))
        ObjectMeta_SetAllocated(cursor);
    else
        ObjectMeta_SetFree(cursor);
}

static inline void ObjectMeta_SweepLineAt(ObjectMeta *data) {
    for (size_t i = 0; i < WORDS_IN_LINE / ALLOCATION_ALIGNMENT_WORDS; i++) {
        ObjectMeta_Sweep(&data[i]);
    }
}

#endif // IMMIX_OBJECTMETA_H