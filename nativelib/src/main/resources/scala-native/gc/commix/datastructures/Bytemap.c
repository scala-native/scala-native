#if defined(SCALANATIVE_GC_COMMIX)

#include "Bytemap.h"
#include "commix/Constants.h"
#include "immix_commix/Log.h"
#include "immix_commix/utils/MathUtils.h"
#include <stdio.h>

void Bytemap_Init(Bytemap *bytemap, word_t *firstAddress, size_t size) {
    bytemap->firstAddress = firstAddress;
    bytemap->size = size / ALLOCATION_ALIGNMENT;
    bytemap->end = &bytemap->data[bytemap->size];
    assert(Bytemap_index(bytemap, (word_t *)((ubyte_t *)(firstAddress) + size) -
                                      ALLOCATION_ALIGNMENT) < bytemap->size);
}

#endif
