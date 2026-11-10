#ifndef IMMIX_OBJECTHEADER_H
#define IMMIX_OBJECTHEADER_H

#include <stdint.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <wchar.h>
#include "immix_commix/CommonConstants.h"
#include "immix_commix/utils/MathUtils.h"
#include "shared/GCTypes.h"
#include "limits.h"
#include "assert.h"

extern const int __weak_ref_field_offset;

/* Bits of BaseRtti.flags, keep in sync with
 * codegen.RuntimeTypeInformation.Flags */
#define RTTI_FLAG_IS_INTERFACE (1 << 0)
#define RTTI_FLAG_IS_PRIMITIVE (1 << 1)
#define RTTI_FLAG_IS_ARRAY (1 << 2)
/* Arrays only, bits 3-4: how the GC scans the elements */
#define RTTI_ARRAY_SCAN_MASK (3 << 3)
#define RTTI_ARRAY_SCAN_NONE (0 << 3)         // primitive values
#define RTTI_ARRAY_SCAN_REFERENCES (1 << 3)   // references
#define RTTI_ARRAY_SCAN_CONSERVATIVE (2 << 3) // raw bytes, up to a limit
/* Bits 5-6: subclasses of java.lang.ref.Reference */
#define RTTI_REFERENCE_KIND_MASK (3 << 5)
#define RTTI_REFERENCE_KIND_WEAK (1 << 5)
#define RTTI_REFERENCE_KIND_SOFT (2 << 5)
#define RTTI_REFERENCE_KIND_PHANTOM (3 << 5)
/* Traits only: implementors hold the trait in its home itable slot (bit 7),
 * low 24 bits of the itable key (bits 8-31) */
#define RTTI_FLAG_IS_AT_HOME_SLOT (1 << 7)
#define RTTI_TRAIT_KEY_SHIFT 8
/* Arrays only, bits 8-15: number of dimensions */
#define RTTI_ARRAY_DIMENSIONS_SHIFT 8

/* Length of the lineage stored inline in class RTTI, keep in sync with
 * codegen.CommonMemoryLayouts.LineageSize */
#define SCALANATIVE_RTTI_LINEAGE_SIZE 8

#ifdef SCALANATIVE_MULTITHREADING_ENABLED
#define USES_LOCKWORD 1

// Inflation mark and object monitor are complementary
#define MONITOR_INFLATION_MARK_MASK ((word_t)1)
#define MONITOR_OBJECT_MASK (~MONITOR_INFLATION_MARK_MASK)

#endif

typedef struct StringObject StringObject;

struct BaseRtti;

/* Slot of an open-addressing table of implemented traits, see codegen.ITable.
 * A NULL trait marks an empty slot. */
typedef struct ITableEntry {
    struct BaseRtti *trait;
    void **vtable;
} ITableEntry;

typedef struct Rtti {
    // Fields shared by all classes
    struct BaseRtti {
        word_t *cls;
#ifdef USES_LOCKWORD
        word_t *lockWord;
#endif
        int32_t flags; // RTTI_FLAG_*
        int32_t interfacesCount;
        struct BaseRtti **interfaces; // all implemented traits
        StringObject *name;
    } rt;
    // Fields defined only for instantiable types (classes)
    int32_t size;
    int32_t superclassCount; // 0 for java.lang.Object and primitives
    union {
        // Classes: offsets in bytes, from the object start, of reference
        // fields, -1, then of raw pointer fields scanned conservatively, -1
        int32_t *refFieldOffsets;
        // Arrays: RTTI of the element type
        struct Rtti *componentType;
    };
    int32_t itableMask;  // itable size - 1, size is a power of two
    int32_t itableShift; // lookups start at (key >> shift) & mask
    ITableEntry *itable; // ITableEntry[itableMask + 1]
    struct Rtti *superclass;
    // Superclasses from the root, then the class itself, then NULLs:
    // lineage[superclassCount] is the class itself
    struct Rtti *lineage[SCALANATIVE_RTTI_LINEAGE_SIZE];
    void *dynmap;
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

static inline bool Rtti_HasFlag(const Rtti *rtti, int32_t flag) {
    return (rtti->rt.flags & flag) != 0;
}

static inline bool Object_IsArray(const Object *object) {
    return Rtti_HasFlag(object->rtti, RTTI_FLAG_IS_ARRAY);
}

static inline int32_t Rtti_ArrayScan(const Rtti *rtti) {
    return rtti->rt.flags & RTTI_ARRAY_SCAN_MASK;
}

static inline size_t Array_Stride(const ArrayHeader *header) {
    // clang would optimize it to llvm.max(stride, 1)
    // negative stride is used only for blob array
    int32_t stride = header->stride;
    return (stride > 0) ? (size_t)stride : 1;
}

static inline size_t BlobArray_ScannableLimit(const ArrayHeader *header) {
    assert(Rtti_ArrayScan(header->rtti) == RTTI_ARRAY_SCAN_CONSERVATIVE);
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
    // Only weak references clear their referent, soft and phantom references
    // keep the referent like any other field
    return (object->rtti->rt.flags & RTTI_REFERENCE_KIND_MASK) ==
           RTTI_REFERENCE_KIND_WEAK;
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
