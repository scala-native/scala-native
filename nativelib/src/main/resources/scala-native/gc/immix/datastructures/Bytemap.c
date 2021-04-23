#include "Bytemap.h"
#include <stdio.h>
#include "utils/MathUtils.h"

void Bytemap_Init(Bytemap *bytemap, word_t *firstAddress, size_t size) {
    bytemap->firstAddress = firstAddress;
    bytemap->size = size / ALLOCATION_ALIGNMENT;
    bytemap->end = &bytemap->data[bytemap->size];
    assert(Bytemap_index(bytemap, (word_t *)((ubyte_t *)(firstAddress) + size) -
                                      ALLOCATION_ALIGNMENT) < bytemap->size);
}