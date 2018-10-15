#ifndef IMMIX_BYTEMAP_H
#define IMMIX_BYTEMAP_H

#include <stddef.h>
#include <stdbool.h>
#include <string.h>
#include "../GCTypes.h"
#include "../Constants.h"
#include "../Log.h"

typedef struct {
    word_t *firstAddress;
    size_t size;
    ubyte_t *end;
    ubyte_t data[0];
} Bytemap;

typedef enum {
    bm_free = 0x0,
    bm_placeholder = 0x1,
    bm_allocated = 0x2,
    bm_marked = 0x4,
} Flag;

void Bytemap_Init(Bytemap *bytemap, word_t *firstAddress, size_t size);

static inline size_t Bytemap_index(Bytemap *bytemap, word_t* address) {
    size_t index = (address - bytemap->firstAddress) / ALLOCATION_ALIGNMENT_WORDS;
    assert(address >= bytemap->firstAddress);
    assert(index < bytemap -> size);
    assert(((word_t)address & ALLOCATION_ALIGNMENT_INVERSE_MASK) == (word_t)address);
    return index;
}

static inline ubyte_t * Bytemap_Cursor(Bytemap *bytemap, word_t* address) {
    size_t index = (address - bytemap->firstAddress) / ALLOCATION_ALIGNMENT_WORDS;
    assert(address >= bytemap->firstAddress);
    assert(index < bytemap -> size);
    assert(((word_t)address & ALLOCATION_ALIGNMENT_INVERSE_MASK) == (word_t)address);
    return &bytemap->data[index];
}

static inline bool Bytemap_IsFree(Bytemap *bytemap, word_t* address) {
    return bytemap->data[Bytemap_index(bytemap, address)] == bm_free;
}

static inline bool Bytemap_IsPlaceholder(Bytemap *bytemap, word_t* address) {
    return bytemap->data[Bytemap_index(bytemap, address)] == bm_placeholder;
}

static inline bool Bytemap_IsAllocated(Bytemap *bytemap, word_t* address) {
    return bytemap->data[Bytemap_index(bytemap, address)] == bm_allocated;
}

static inline bool Bytemap_IsMarked(Bytemap *bytemap, word_t* address) {
    return bytemap->data[Bytemap_index(bytemap, address)] == bm_marked;
}


static inline void Bytemap_SetFree(Bytemap *bytemap, word_t* address) {
    bytemap->data[Bytemap_index(bytemap, address)] = bm_free;
}

static inline void Bytemap_SetPlaceholder(Bytemap *bytemap, word_t* address) {
    bytemap->data[Bytemap_index(bytemap, address)] = bm_placeholder;
}

static inline void Bytemap_SetAllocated(Bytemap *bytemap, word_t* address) {
    bytemap->data[Bytemap_index(bytemap, address)] = bm_allocated;
}

static inline void Bytemap_SetMarked(Bytemap *bytemap, word_t* address) {
    bytemap->data[Bytemap_index(bytemap, address)] = bm_marked;
}

static inline ubyte_t* Bytemap_NextLine(ubyte_t* cursor) {
    return cursor + WORDS_IN_LINE / ALLOCATION_ALIGNMENT_WORDS;
}

static inline void Bytemap_ClearLineAt(ubyte_t* cursor) {
    memset(cursor, 0, WORDS_IN_LINE / ALLOCATION_ALIGNMENT_WORDS);
}

static inline void Bytemap_ClearBlock(Bytemap *bytemap, word_t* start) {
    memset(Bytemap_Cursor(bytemap, start), 0, WORDS_IN_BLOCK / ALLOCATION_ALIGNMENT_WORDS);
}

/*
    implements this, just with hardcoded constants:

    size_t startIndex = Bytemap_index(bytemap, start);
    size_t endIndex = startIndex + WORDS_IN_LINE;
    ubyte_t *data = bytemap->data;
    for (size_t i = startIndex; i < endIndex; i++) {
        if (data[i] == bm_marked) {
            data[i] = bm_allocated;
        } else if (data[i] == bm_allocated) {
            data[i] = bm_free;
        }
    }
*/
#define SWEEP_MASK 0x0404040404040404UL
static inline void Bytemap_SweepLineAt(ubyte_t *start) {
    assert(WORDS_IN_LINE / ALLOCATION_ALIGNMENT == 2);
    uint64_t *first = (uint64_t *) start;
    first[0] = (first[0] & SWEEP_MASK) >> 1;
    first[1] = (first[1] & SWEEP_MASK) >> 1;

}

#endif // IMMIX_BYTEMAP_H