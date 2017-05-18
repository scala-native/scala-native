#ifndef IMMIX_OBJECTHEADER_H
#define IMMIX_OBJECTHEADER_H


#include <stdint.h>
#include <stdbool.h>
#include <stddef.h>
#include "../GCTypes.h"
#include "../Constants.h"
#include "../Log.h"

typedef enum {
    object_forward = 0x0,
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
} ObjectHeaderLine;

typedef struct {
    struct {
        int32_t id;
        word_t* name;
        int8_t kind;
    } rt;
    int64_t size;
    struct {
        int32_t from;
        int32_t to;
    } range;
    struct {
        int32_t dyn_method_count;
        word_t* dyn_method_salt;
        word_t* dyn_method_keys;
        word_t* dyn_methods;
    } dynDispatchTable;
    int64_t* refMapStruct;
} Rtti;

typedef word_t* Field_t;

typedef struct {
    ObjectHeaderLine header;
    Rtti* rtti;
    Field_t fields[0];
} ObjectHeader;

static inline bool Object_isMarked(ObjectHeader *objectHeader) {
    return objectHeader->header.flag == object_marked;
}

static inline void Object_markObjectHeader(ObjectHeader *objectHeader) {
    objectHeader->header.flag = object_marked;
}

static inline void Object_setAllocated(ObjectHeader *objectHeader) {
    objectHeader->header.flag = object_allocated;
}

static inline void Object_setFree(ObjectHeader *objectHeader) {
    objectHeader->header.flag = object_free;
}

static inline bool Object_isAllocated(ObjectHeader *objectHeader) {
    return objectHeader->header.flag == object_allocated;
}

static inline bool Object_isStandardObject(ObjectHeader *objectHeader) {
    return objectHeader->header.type == object_standard;
}
static inline bool Object_isLargeObject(ObjectHeader *objectHeader) {
    return objectHeader->header.type == object_large;
}

static inline bool Object_isForwardObject(ObjectHeader *objectHeader) {
    return objectHeader->header.type == object_forward;
}

static inline void Object_setObjectType(ObjectHeader *objectHeader, ObjectType objectType) {
    objectHeader->header.type = objectType;
}

static inline size_t Object_size(ObjectHeader *objectHeader) {
    uint32_t size = objectHeader->header.size;
    assert((Object_isStandardObject(objectHeader) && size < LARGE_BLOCK_SIZE) || !Object_isStandardObject(objectHeader));

    return size << WORD_SIZE_BITS;
}

static inline void Object_setSize(ObjectHeader *objectHeader, size_t size) {
    uint32_t _size = (uint32_t)(size >> WORD_SIZE_BITS);
    assert(!Object_isStandardObject(objectHeader) || (Object_isStandardObject(objectHeader) && _size > 0 && _size < LARGE_BLOCK_SIZE));
    objectHeader->header.size = _size;
}

static inline ObjectHeader* Object_fromMutatorAddress(word_t *address) {
    return (ObjectHeader*) (address - WORDS_IN_OBJECT_HEADER);
}



#endif //IMMIX_OBJECTHEADER_H
