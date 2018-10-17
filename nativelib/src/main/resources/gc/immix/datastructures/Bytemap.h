#ifndef IMMIX_BYTEMAP_H
#define IMMIX_BYTEMAP_H

#include <stddef.h>
#include <stdbool.h>
#include <string.h>
#include "../GCTypes.h"
#include "../Constants.h"
#include "../Log.h"
#include "../metadata/ObjectMeta.h"

typedef struct {
    word_t *firstAddress;
    size_t size;
    ObjectMeta *end;
    ObjectMeta data[0];
} Bytemap;

void Bytemap_Init(Bytemap *bytemap, word_t *firstAddress, size_t size);

static inline size_t Bytemap_index(Bytemap *bytemap, word_t *address) {
    size_t index =
        (address - bytemap->firstAddress) / ALLOCATION_ALIGNMENT_WORDS;
    assert(address >= bytemap->firstAddress);
    assert(index < bytemap->size);
    assert(((word_t)address & ALLOCATION_ALIGNMENT_INVERSE_MASK) ==
           (word_t)address);
    return index;
}

static inline ObjectMeta *Bytemap_Cursor(Bytemap *bytemap, word_t *address) {
    size_t index =
        (address - bytemap->firstAddress) / ALLOCATION_ALIGNMENT_WORDS;
    assert(address >= bytemap->firstAddress);
    assert(index < bytemap->size);
    assert(((word_t)address & ALLOCATION_ALIGNMENT_INVERSE_MASK) ==
           (word_t)address);
    return &bytemap->data[index];
}

static inline ObjectMeta *Bytemap_PreviousWord(ObjectMeta *cursor) {
    return cursor - 1;
}

static inline ObjectMeta *Bytemap_NextWord(ObjectMeta *cursor) {
    return cursor + 1;
}

static inline ObjectMeta *Bytemap_NextLine(ObjectMeta *cursor) {
    return cursor + WORDS_IN_LINE / ALLOCATION_ALIGNMENT_WORDS;
}

static inline void Bytemap_ClearLineAt(ObjectMeta *cursor) {
    memset(cursor, 0, WORDS_IN_LINE / ALLOCATION_ALIGNMENT_WORDS);
}

static inline void Bytemap_ClearBlock(Bytemap *bytemap, word_t *start) {
    memset(Bytemap_Cursor(bytemap, start), 0,
           WORDS_IN_BLOCK / ALLOCATION_ALIGNMENT_WORDS);
}


#define SWEEP_MASK 0x0404040404040404UL
static inline void Bytemap_SweepLineAt(ObjectMeta *start) {
//    implements this, just with hardcoded constants:
//
//    size_t startIndex = Bytemap_index(bytemap, start);
//    size_t endIndex = startIndex + WORDS_IN_LINE / ALLOCATION_ALIGNMENT_WORDS;
//    ubyte_t *data = bytemap->data;
//    for (size_t i = startIndex; i < endIndex; i++) {
//        if (data[i] == bm_marked) {
//            data[i] = bm_allocated;
//        } else if (data[i] == bm_allocated) {
//            data[i] = bm_free;
//        }
//    }
    assert(WORDS_IN_LINE / ALLOCATION_ALIGNMENT_WORDS / 8 == 2);
    uint64_t *first = (uint64_t *)start;
    first[0] = (first[0] & SWEEP_MASK) >> 1;
    first[1] = (first[1] & SWEEP_MASK) >> 1;
}

#endif // IMMIX_BYTEMAP_H