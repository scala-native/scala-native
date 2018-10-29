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

static inline ObjectMeta *Bytemap_Get(Bytemap *bytemap, word_t *address) {
    size_t index =
        (address - bytemap->firstAddress) / ALLOCATION_ALIGNMENT_WORDS;
    assert(address >= bytemap->firstAddress);
    assert(index < bytemap->size);
    assert(((word_t)address & ALLOCATION_ALIGNMENT_INVERSE_MASK) ==
           (word_t)address);
    return &bytemap->data[index];
}

static inline ObjectMeta *Bytemap_NextLine(ObjectMeta *cursor) {
    return cursor + WORDS_IN_LINE / ALLOCATION_ALIGNMENT_WORDS;
}

#endif // IMMIX_BYTEMAP_H