#ifndef IMMIX_OBJECTHEADER_H
#define IMMIX_OBJECTHEADER_H

#include <stdint.h>
#include <stdbool.h>
#include <stddef.h>
#include "../GCTypes.h"
#include "../Constants.h"
#include "../Log.h"
#include "../utils/MathUtils.h"

extern int __object_array_id;
extern int __array_type_count;
extern int __array_ids; // first element of the array

typedef enum {
    object_standard = 0x1,
    object_large = 0x2,
} ObjectType;

typedef enum {
    object_free = 0x0,
    object_allocated = 0x1,
    object_marked = 0x2,
} ObjectFlag;

typedef struct {
    uint32_t size;
    uint8_t type;
    uint8_t flag;
} ObjectHeader;

typedef struct {
    struct {
        int32_t id;
        int32_t tid;
        word_t *name;
        int8_t kind;
    } rt;
    int64_t size;
    struct {
        int32_t from;
        int32_t to;
    } range;
    struct {
        int32_t dyn_method_count;
        word_t *dyn_method_salt;
        word_t *dyn_method_keys;
        word_t *dyn_methods;
    } dynDispatchTable;
    int64_t *refMapStruct;
} Rtti;

typedef word_t *Field_t;

typedef struct {
    ObjectHeader header;
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
    ObjectHeader header;
    void *nothing;
    size_t size;
    Chunk *next;
};

static inline bool Object_IsArray(Object *object) {
    int32_t id = object->rtti->rt.id;
    for (int i=0; i < __array_type_count; i++) {
        if ((&__array_ids)[i] == id ) {
            return true;
        }
    }
    return false;
}

static inline size_t Object_Size(Object *object) {
    if (object->rtti == NULL) {
        Chunk *chunk = (Chunk *) object;
        return  chunk-> size;
    } else if (Object_IsArray(object)) {
        ArrayHeader *arrayHeader = (ArrayHeader *)&object->rtti;
        return MathUtils_RoundToNextMultiple(sizeof(ArrayHeader) + (size_t) arrayHeader->length * (size_t) arrayHeader->stride, WORD_SIZE);
    } else {
        return MathUtils_RoundToNextMultiple((size_t) object->rtti->size, WORD_SIZE);
    }
}

static inline Object *Object_FromMutatorAddress(word_t *address) {
    return (Object *)(address - WORDS_IN_OBJECT_HEADER);
}

static inline word_t *Object_ToMutatorAddress(Object *object) {
    return (word_t *)&object->rtti;
}

#endif // IMMIX_OBJECTHEADER_H
