#ifndef IMMIX_OBJECTMETA_H
#define IMMIX_OBJECTMETA_H

#include <stddef.h>
#include <string.h>
#include <stdbool.h>
#include <stdio.h>

#define OLD_MASK 0x80

// Only OLD objects are remembered. This means that om_allocated_remembered represent
// old objects. This flag is used during the marking phase of an old collection if 
// the object is remembered.
typedef enum {
    om_free = 0x0,
    om_placeholder = 0x1,
    om_allocated = 0x2,
    om_marked = 0x4,
    om_allocated_rem = 0xA, // om_allocated | 0x8
    om_marked_rem = 0xC, // om_marked | 0x8
} Flag;

typedef ubyte_t ObjectMeta;

static inline ubyte_t ObjectMeta_getFlag(ObjectMeta *metadata) {
    return *metadata & ~OLD_MASK;
}

static inline void ObjectMeta_SetOldBit(ObjectMeta *metadata) {
    *metadata |= OLD_MASK;
}

static inline bool ObjectMeta_IsFree(ObjectMeta *metadata) {
    // 0x8 = free garbage from old collection. See `ObjectMeta_SweepOld`
    ubyte_t flag = ObjectMeta_getFlag(metadata);
    return flag == om_free || flag == 0x8 || flag == 0x80;
}

static inline bool ObjectMeta_IsPlaceholder(ObjectMeta *metadata) {
    return ObjectMeta_getFlag(metadata) == om_placeholder;
}

static inline bool ObjectMeta_IsAllocated(ObjectMeta *metadata) {
    return ObjectMeta_getFlag(metadata) == om_allocated;
}

static inline bool ObjectMeta_IsAllocatedRem(ObjectMeta *metadata) {
    return ObjectMeta_getFlag(metadata) == om_allocated_rem;
}

static inline bool ObjectMeta_IsMarked(ObjectMeta *metadata) {
    return ObjectMeta_getFlag(metadata) == om_marked;
}

static inline bool ObjectMeta_IsMarkedRem(ObjectMeta *metadata) {
    return ObjectMeta_getFlag(metadata) == om_marked_rem;
}

static inline bool ObjectMeta_IsOld(ObjectMeta *metadata) {
    // (om_marked || om_marked_rem) || om_allocated_rem
    ubyte_t data = ObjectMeta_getFlag(metadata);
    return ((data & 0x4) == 0x4) || data == om_allocated_rem;
}

static inline bool ObjectMeta_IsAlive(ObjectMeta *metadata, bool oldObject) {
    ubyte_t data = ObjectMeta_getFlag(metadata);
    if (oldObject) {
        return data == om_marked || data == om_marked_rem;
        //return  (data & 0x4) == 0x4;
    } else {
        return data == om_allocated;
    }
}

static inline bool ObjectMeta_IsAliveSweep(ObjectMeta *metadata, bool collectingOld) {
    if (collectingOld) {
        return ObjectMeta_IsAllocated(metadata) || ObjectMeta_IsAllocatedRem(metadata);
    } else {
        return ObjectMeta_IsMarked(metadata) || ObjectMeta_IsMarkedRem(metadata);
    }
}

static inline void ObjectMeta_SetFree(ObjectMeta *metadata) {
    *metadata = om_free;
}

static inline void ObjectMeta_SetPlaceholder(ObjectMeta *metadata) {
    *metadata = om_placeholder;
}

static inline void ObjectMeta_SetFlag(ObjectMeta *metadata, ubyte_t flag) {
    *metadata = flag;
}

static inline void ObjectMeta_SetAllocated(ObjectMeta *metadata) {
    *metadata = (*metadata & OLD_MASK) | om_allocated;
}

static inline void ObjectMeta_SetMarked(ObjectMeta *metadata) {
    *metadata = (*metadata & OLD_MASK) | om_marked;
}

static inline void ObjectMeta_SetMarkedRem(ObjectMeta *metadata) {
    *metadata = om_marked_rem | 0x80;
}

static inline void ObjectMeta_SetAllocatedRem(ObjectMeta *metadata) {
    *metadata = om_allocated_rem | 0x80;
}

static inline void ObjectMeta_ClearLineAt(ObjectMeta *cursor) {
    memset(cursor, 0, WORDS_IN_LINE / ALLOCATION_ALIGNMENT_WORDS);
}

static inline void ObjectMeta_ClearBlockAt(ObjectMeta *cursor) {
    memset(cursor, 0, WORDS_IN_BLOCK / ALLOCATION_ALIGNMENT_WORDS);
}

#define SWEEP_MASK 0x0404040404040404UL
static inline void ObjectMeta_SweepLineAt(ObjectMeta *start) {
    //    implements this, just with hardcoded constants:
    //
    //    size_t startIndex = Bytemap_index(bytemap, start);
    //    size_t endIndex = startIndex + WORDS_IN_LINE /
    //    ALLOCATION_ALIGNMENT_WORDS; ObjectMeta *data = bytemap->data;
    //
    //    for (size_t i = startIndex; i < endIndex; i++) {
    //        if (data[i] == om_marked) {
    //            data[i] = om_allocated;
    //        } else {
    //            data[i] = om_free;
    //        }
    //    }
    assert(WORDS_IN_LINE / ALLOCATION_ALIGNMENT_WORDS / 8 == 2);
    uint64_t *first = (uint64_t *)start;
    first[0] = (first[0] & SWEEP_MASK) >> 1;
    first[1] = (first[1] & SWEEP_MASK) >> 1;
}

#define OLD_BYTE_MASK 0x8080808080808080UL
#define SWEEP_MASK_NEW_OLD 0x0C0C0C0C0C0C0C0CUL
static inline void ObjectMeta_SweepNewOldLineAt(ObjectMeta *start) {
    //
    //  for (size_t i = startIndex; i < endIndex; i++) {
    //      if (data[i] == om_marked == om_marked_rem) {
    //          // do nothing. Old object are marked
    //      } else {
    //          data[i] = omd_free;
    //      }
    assert(WORDS_IN_LINE / ALLOCATION_ALIGNMENT_WORDS / 8 == 2);
    uint64_t *first = (uint64_t *)start;
    first[0] = (first[0] & SWEEP_MASK_NEW_OLD) | OLD_BYTE_MASK;
    first[1] = (first[1] & SWEEP_MASK_NEW_OLD) | OLD_BYTE_MASK;
}

#define SWEEP_MASK_OLD 0x0A0A0A0A0A0A0A0AUL
#define SWEEP_MASK_OLD_ADD 0x0202020202020202UL
static inline void ObjectMeta_SweepOldLineAt(ObjectMeta *start) {
    //
    //  for (size_t i = startIndex; i < endIndex; i++) {
    //      if (data[i] == om_allocated) {
    //          data[i] = om_marked;
    //      } else if (data[i] == om_allocated_rem) {
    //          data[i] = om_marked_rem;
    //      } else {
    //          data[i] = om_free;
    //      }
    //
    //  For mask explanation, see below `ObjectMeta_SweepOld`
    assert(WORDS_IN_LINE / ALLOCATION_ALIGNMENT_WORDS / 8 == 2);
    uint64_t *first = (uint64_t *)start;
    first[0] = (((first[0] & SWEEP_MASK_OLD) + SWEEP_MASK_OLD_ADD) & SWEEP_MASK_NEW_OLD) | OLD_BYTE_MASK;
    first[1] = (((first[1] & SWEEP_MASK_OLD) + SWEEP_MASK_OLD_ADD) & SWEEP_MASK_NEW_OLD) | OLD_BYTE_MASK;
}


static inline void ObjectMeta_Sweep(ObjectMeta *cursor) {
    //    implements this, just with hardcoded constants:
    //
    //    if (ObjectMeta_IsMarked(cursor)) {
    //        ObjectMeta_SetAllocated(cursor);
    //    } else {
    //        ObjectMeta_SetFree(cursor);
    //    }
    *cursor = (*cursor & 0x04) >> 1;
}

static inline void ObjectMeta_SweepNewOld(ObjectMeta *cursor) {
    //    implements this, just with hardcoded constants:
    //
    //    if (*cursor == om_marked || *cursor == om_marked_remembered) {
    //        // do nothing, old object are marked
    //    } else {
    //        ObjectMeta_SetFree(cursor);
    //    }
    assert(*cursor != om_allocated_rem);
    *cursor = (*cursor & 0xC) | OLD_MASK;
}

static inline void ObjectMeta_SweepOld(ObjectMeta *cursor) {
    //    implements this, just with hardcoded constants:
    //
    //    if (*cursor == om_allocated) {
    //      ObjectMeta_SetMarked(cursor);
    //    } else if (*cursor == om_allocated_remembered) {
    //      ObjectMeta_SetAllocatedRem(cursor);
    //    } else {
    //        ObjectMeta_SetFree(cursor);
    //    }
    //  We want to apply the following transformation
    //      - om_free           0000 -> 0000        om_free
    //      - om_allocated      0010 -> 0100        om_marked
    //      - om_marked         0100 -> 0000        om_free
    //      - om_allocated_rem  1010 -> 1100        om_marked_rem
    //      - om_marked_rem     1100 -> 0000        om_free
    //
    //  However, currently this code does the transformation
    //  1100 -> 1000 instead of the one wanted above. Since the
    //  code 1000 (0x10) does not match any code, it should be ok.
    //
    //  Explanation :
    //      - & 0xA keep only the fourth and second bits of the byte, virtually
    //        freeing the om_marked and om_marked_rem
    //      - + 0x2 activate the third bit if the second is active, and deactive the
    //        second bit. This "unfree" the om_marked and om_marked_rem objects by making
    //        them om_allocated and om_allocated_rem
    //      - & 0xC re-free them by keeping only the third and fourth bit. Making the om_allocated
    //        free and om_allocated_rem 0x10 which does not correspond to any code.
    *cursor = (((*cursor & 0xA) + 0x2) & 0xC) | OLD_MASK;
}

#ifdef DEBUG_ASSERT
static inline void ObjectMeta_AssertIsValidAllocation(ObjectMeta *start,
                                                      size_t size) {
    ObjectMeta *limit = start + (size / ALLOCATION_ALIGNMENT);
    for (ObjectMeta *current = start; current < limit; current++) {
        assert(ObjectMeta_IsFree(current) || ObjectMeta_IsPlaceholder(current));
    }
}
#endif

#endif // IMMIX_OBJECTMETA_H
