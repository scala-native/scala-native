#ifndef IMMIX_OBJECTMETA_H
#define IMMIX_OBJECTMETA_H

#include <stddef.h>
#include <stdbool.h>

typedef enum {
    bm_free = 0x0,
    bm_placeholder = 0x1,
    bm_allocated = 0x2,
    bm_marked = 0x4,
} Flag;

typedef ubyte_t ObjectMeta;

static inline bool ObjectMeta_IsFree(ObjectMeta *metadata) {
    return *metadata == bm_free;
}

static inline bool ObjectMeta_IsPlaceholder(ObjectMeta *metadata) {
    return *metadata == bm_placeholder;
}

static inline bool ObjectMeta_IsAllocated(ObjectMeta *metadata) {
    return *metadata == bm_allocated;
}

static inline bool ObjectMeta_IsMarked(ObjectMeta *metadata) {
    return *metadata == bm_marked;
}

static inline void ObjectMeta_SetFree(ObjectMeta *metadata) {
    *metadata = bm_free;
}

static inline void ObjectMeta_SetPlaceholder(ObjectMeta *metadata) {
    *metadata = bm_placeholder;
}

static inline void ObjectMeta_SetAllocated(ObjectMeta *metadata) {
    *metadata = bm_allocated;
}

static inline void ObjectMeta_SetMarked(ObjectMeta *metadata) {
    *metadata = bm_marked;
}

#endif // IMMIX_OBJECTMETA_H