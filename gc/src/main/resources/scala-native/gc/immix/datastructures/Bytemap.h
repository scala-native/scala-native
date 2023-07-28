#ifndef IMMIX_BYTEMAP_H
#define IMMIX_BYTEMAP_H

#include <stddef.h>
#include <stdbool.h>
#include <string.h>
#include "shared/GCTypes.h"
#include "immix/Constants.h"
#include "immix_commix/Log.h"
#include "immix/metadata/ObjectMeta.h"

typedef struct {
    word_t *firstAddress;
    size_t size;
    ObjectMeta *end;
    ObjectMeta data[0];
} Bytemap;

void Bytemap_Init(Bytemap *bytemap, word_t *firstAddress, size_t size);

static inline bool Bytemap_isPtrAligned(word_t *address) {
    word_t aligned = ((word_t)address & ALLOCATION_ALIGNMENT_INVERSE_MASK);
    return (word_t *)aligned == address;
}

static inline size_t Bytemap_index(Bytemap *bytemap, word_t *address) {
    size_t index =
        (address - bytemap->firstAddress) / ALLOCATION_ALIGNMENT_WORDS;
    assert(address >= bytemap->firstAddress);
    assert(index < bytemap->size);
    assert(Bytemap_isPtrAligned(address));
    return index;
}

static inline ObjectMeta *Bytemap_Get(Bytemap *bytemap, word_t *address) {
    size_t index = Bytemap_index(bytemap, address);
    return &bytemap->data[index];
}

static inline ObjectMeta *Bytemap_NextLine(ObjectMeta *cursor) {
    return cursor + WORDS_IN_LINE / ALLOCATION_ALIGNMENT_WORDS;
}

#endif // IMMIX_BYTEMAP_H