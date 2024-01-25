#ifndef IMMIX_OBJECTHEADER_H
#define IMMIX_OBJECTHEADER_H

#include <stdint.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdio.h>
#include "immix_commix/CommonConstants.h"
#include "immix_commix/Log.h"
#include "immix_commix/utils/MathUtils.h"
#include "shared/GCTypes.h"
#include "limits.h"

extern const int __object_array_id;
extern const int __blob_array_id;
extern const int __weak_ref_ids_min;
extern const int __weak_ref_ids_max;
extern const int __weak_ref_field_offset;
extern const int __array_ids_min;
extern const int __array_ids_max;
extern const int __boxed_ptr_id;

#ifdef SCALANATIVE_MULTITHREADING_ENABLED
#define USES_LOCKWORD 1

// Inflation mark and object monitor are complementary
#define MONITOR_INFLATION_MARK_MASK ((word_t)1)
#define MONITOR_OBJECT_MASK (~MONITOR_INFLATION_MARK_MASK)

#endif

typedef struct {
    struct {
        word_t *cls;
#ifdef USES_LOCKWORD
        word_t *lockWord;
#endif
        int32_t id;
        int32_t tid;
        word_t *name;
    } rt;
    int32_t size;
    int32_t idRangeUntil;
    int64_t *refMapStruct;
} Rtti;

typedef word_t *Field_t;

typedef struct {
    Rtti *rtti;
#ifdef USES_LOCKWORD
    word_t *lockWord;
#endif
    Field_t fields[0];
} Object;

typedef struct {
    Rtti *rtti;
#ifdef USES_LOCKWORD
    word_t *lockWord;
#endif
    int32_t length;
    int32_t stride;
} ArrayHeader;

typedef struct Chunk Chunk;

struct Chunk {
    void *nothing;
    size_t size;
    Chunk *next;
};

static inline bool Object_IsArray(const Object *object) {
    int32_t id = object->rtti->rt.id;
    return __array_ids_min <= id && id <= __array_ids_max;
}

static inline size_t Array_Stride(const ArrayHeader *header) {
    // clang would optimize it to llvm.max(stride, 1)
    // negative stride is used only for blob array
    int32_t stride = header->stride;
    return (stride > 0) ? (size_t)stride : 1;
}

static inline size_t BlobArray_ScannableLimit(const ArrayHeader *header) {
    assert(header->rtti->rt.id == __blob_array_id);
    size_t length = (size_t)header->length;
    size_t limit = (size_t)-header->stride; // limit is stored as negative
    return (limit < length) ? limit : length;
}

static inline size_t Object_Size(const Object *object) {
    if (Object_IsArray(object)) {
        ArrayHeader *arrayHeader = (ArrayHeader *)object;
        size_t size = sizeof(ArrayHeader) +
                      (size_t)arrayHeader->length * Array_Stride(arrayHeader);
        return MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);
    } else {
        return MathUtils_RoundToNextMultiple((size_t)object->rtti->size,
                                             ALLOCATION_ALIGNMENT);
    }
}

static inline bool Object_IsWeakReference(const Object *object) {
    int32_t id = object->rtti->rt.id;
    return __weak_ref_ids_min <= id && id <= __weak_ref_ids_max;
}

static inline bool Object_IsReferantOfWeakReference(const Object *object,
                                                    int fieldOffset) {
    return Object_IsWeakReference(object) &&
           fieldOffset == __weak_ref_field_offset;
}

#ifdef USES_LOCKWORD
static inline bool Field_isInflatedLock(const Field_t field) {
    return (word_t)field & MONITOR_INFLATION_MARK_MASK;
}

static inline Field_t Field_allignedLockRef(const Field_t field) {
    return (Field_t)((word_t)field & MONITOR_OBJECT_MASK);
}
#endif

#endif // IMMIX_OBJECTHEADER_H
