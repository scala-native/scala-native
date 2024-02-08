#ifndef IMMIX_OBJECTHEADER_H
#define IMMIX_OBJECTHEADER_H

#include <stdint.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <wchar.h>
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

typedef struct StringObject StringObject;

typedef struct {
    struct {
        word_t *cls;
#ifdef USES_LOCKWORD
        word_t *lockWord;
#endif
        int32_t id;
        int32_t tid;
        StringObject *name;
    } rt;
    int32_t size;
    int32_t idRangeUntil;
    int32_t *refFieldOffsets; // Array of field offsets (in bytes) from object
                              // start, terminated with -1
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

typedef struct {
    ArrayHeader header;
    uint16_t values[0];
} CharArray;

typedef struct StringObject {
    // ObjectHeader
    Rtti *rtti;
#ifdef USES_LOCKWORD
    word_t *lockWord;
#endif
    // Object fields
    // Best effort, order of fields is not guaranteed
    CharArray *value;
    int32_t offset;
    int32_t count;
    int32_t cached_hash_code;
} StringObject;

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

/* Returns a wide string containg Class.name of given object based on UTF-8
 * java.lang.String value.
 * Caller of this function is responsible for freeing returned pointer. Function
 * can fail if StringObject layout does not match the runtime layout
 */
static inline wchar_t *Object_nameWString(Object *object) {
    // Depending on platform wchar_t might be 2 or 4 bytes
    // Always convert Scala Char to wchar_t
    CharArray *strChars = object->rtti->rt.name->value;
    int nameLength = strChars->header.length;
    wchar_t *buf = calloc(nameLength + 1, sizeof(wchar_t));
    for (int i = 0; i < nameLength; i++) {
        buf[i] = (wchar_t)strChars->values[i];
    }
    buf[nameLength] = 0;
    return buf;
}

#endif // IMMIX_OBJECTHEADER_H
