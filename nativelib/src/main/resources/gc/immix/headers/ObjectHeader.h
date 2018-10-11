#ifndef IMMIX_OBJECTHEADER_H
#define IMMIX_OBJECTHEADER_H

#include <stdint.h>
#include <stdbool.h>
#include <stddef.h>
#include "../GCTypes.h"
#include "../Constants.h"
#include "../Log.h"

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

static inline bool Object_IsStandardObject(ObjectHeader *objectHeader) {
    return objectHeader->type == object_standard;
}
static inline bool Object_IsLargeObject(ObjectHeader *objectHeader) {
    return objectHeader->type == object_large;
}

static inline bool Object_IsArray(Object *object) {
    int32_t id = object->rtti->rt.id;
    for (int i=0; i < __array_type_count; i++) {
        if ((&__array_ids)[i] == id ) {
            return true;
        }
    }
    return false;
}

static inline void Object_SetObjectType(ObjectHeader *objectHeader,
                                        ObjectType objectType) {
    objectHeader->type = objectType;
}

static inline size_t Object_SizeInternal(Object *object) {
    if (Object_IsArray(object)) {
        ArrayHeader *arrayHeader = (ArrayHeader *)&object->rtti;
        return sizeof(ArrayHeader) + (size_t) arrayHeader->length * (size_t) arrayHeader->stride;
    } else {
        return (size_t) object->rtti->size;
    }
}

static inline size_t Object_Size(ObjectHeader *objectHeader) {
    uint32_t size = objectHeader->size;
    assert((Object_IsStandardObject(objectHeader) && size < LARGE_BLOCK_SIZE) ||
           !Object_IsStandardObject(objectHeader));

    return size << WORD_SIZE_BITS;
}

static inline void Object_SetSize(ObjectHeader *objectHeader, size_t size) {
    uint32_t _size = (uint32_t)(size >> WORD_SIZE_BITS);
    assert(!Object_IsStandardObject(objectHeader) ||
           (Object_IsStandardObject(objectHeader) && _size > 0 &&
            _size < LARGE_BLOCK_SIZE));
    objectHeader->size = _size;
}

static inline Object *Object_FromMutatorAddress(word_t *address) {
    return (Object *)(address - WORDS_IN_OBJECT_HEADER);
}

static inline word_t *Object_ToMutatorAddress(Object *object) {
    return (word_t *)&object->rtti;
}

#endif // IMMIX_OBJECTHEADER_H
