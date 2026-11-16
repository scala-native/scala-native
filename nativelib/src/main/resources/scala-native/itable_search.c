#include "gc/immix_commix/headers/ObjectHeader.h"
#include <stdint.h>

/* Continues an itable lookup for `trait` after slot `pos`, which holds
 * another trait, see codegen.ITable: probes the following slots until the
 * trait or an empty slot. Returns the slot of the trait, or -1. Generated
 * code probes the first slot inline. */
int32_t scalanative_itable_find(const Rtti *rtti, const void *trait,
                                int32_t pos) {
    int32_t mask = rtti->itableMask;
    for (int32_t probes = 0; probes < mask; probes++) {
        pos = (pos + 1) & mask;
        const void *entry = rtti->itable[pos].trait;
        if (entry == trait)
            return pos;
        if (entry == NULL)
            return -1;
    }
    return -1;
}
