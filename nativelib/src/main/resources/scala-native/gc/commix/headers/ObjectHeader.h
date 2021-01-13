#ifndef IMMIX_OBJECTHEADER_H
#define IMMIX_OBJECTHEADER_H

#include <stdint.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdio.h>
#include "../GCTypes.h"
#include "../Constants.h"
#include "../Log.h"
#include "../utils/MathUtils.h"

extern int __object_array_id;
extern int __array_ids_min;
extern int __array_ids_max;

typedef struct {
    struct {
        int32_t id;
        int32_t tid;
        word_t *name;
        word_t *cls;
    } rt;
    int32_t size;
    int32_t idRangeUntil;
    int64_t *refMapStruct;
} Rtti;

typedef word_t *Field_t;

typedef struct {
    Rtti *rtti;
    Field_t fields[0];
} Object;

typedef struct {
    Rtti *rtti;
    int32_t length;
    int32_t stride;
} ArrayHeader;

typedef struct Chunk Chunk;

struct Chunk {
    void *nothing;
    size_t size;
    Chunk *next;
};

static inline bool Object_IsArray(Object *object) {
    int32_t id = object->rtti->rt.id;
    return __array_ids_min <= id && id <= __array_ids_max;
}

static inline size_t Object_Size(Object *object) {
    if (Object_IsArray(object)) {
        ArrayHeader *arrayHeader = (ArrayHeader *)object;
        return MathUtils_RoundToNextMultiple(
            sizeof(ArrayHeader) +
                (size_t)arrayHeader->length * (size_t)arrayHeader->stride,
            ALLOCATION_ALIGNMENT);
    } else {
        return MathUtils_RoundToNextMultiple((size_t)object->rtti->size,
                                             ALLOCATION_ALIGNMENT);
    }
}

#endif // IMMIX_OBJECTHEADER_H
