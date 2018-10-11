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
    bm_marked = 0x3,
} Flag;

void Bytemap_Init(Bytemap *bytemap, word_t *firstAddress, size_t size);

static inline size_t Bytemap_index(Bytemap *bytemap, word_t* address) {
    size_t index = address - bytemap->firstAddress;
    assert(address >= bytemap->firstAddress);
    assert(index < bytemap -> size);
    return index;
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

static inline void Bytemap_SetAreaFree(Bytemap *bytemap, word_t* start, size_t words){
    memset(&bytemap->data[Bytemap_index(bytemap, start)], 0, words);
}

#endif // IMMIX_BYTEMAP_H