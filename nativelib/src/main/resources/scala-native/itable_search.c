#include "gc/immix_commix/headers/ObjectHeader.h"
#include <stdbool.h>

void *__scalanative_trait_dispatch_slowpath(Rtti *rtti, int traitId,
                                            int methodIdx) {
    int low = 0;
    int high = -rtti->itableCount;
    // if (high == 0)
    //     return NULL; // unreachable
    while (low <= high) {
        int idx = (low + high) / 2;
        ITableEntry *itable = &rtti->itable[idx];
        int itableId = itable->interfaceId;
        if (itableId == traitId)
            return itable->vtable[methodIdx];
        if (itableId < traitId)
            low = idx + 1;
        else
            high = idx - 1;
    }
    return NULL; // unreachable
}

bool __scalanative_class_has_trait_slowpath(Rtti *rtti, int traitId) {
    int low = 0;
    int high = -rtti->itableCount;
    if (high == 0)
        return false;
    while (low <= high) {
        int idx = (low + high) / 2;
        ITableEntry *itable = &rtti->itable[idx];
        int itableId = itable->interfaceId;
        if (itableId == traitId)
            return true;
        if (itableId < traitId)
            low = idx + 1;
        else
            high = idx - 1;
    }
    return false;
}
