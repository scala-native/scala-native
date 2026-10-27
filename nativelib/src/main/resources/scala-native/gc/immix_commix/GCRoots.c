#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX)

#include "immix_commix/GCRoots.h"

#include <stdio.h>
#include <assert.h>
#include <stdlib.h>
#include "shared/ThreadUtil.h"

GC_Roots *GC_Roots_Init() {
    GC_Roots *roots = (GC_Roots *)malloc(sizeof(GC_Roots));
    if (roots == NULL)
        return NULL;
    roots->head = NULL;
    if (!mutex_init(&roots->modificationLock)) {
        free(roots);
        return NULL;
    }
    return roots;
}

bool GC_Roots_Add(GC_Roots *roots, AddressRange range) {
    // Prepend the node with given range to the head of linked list of GC roots
    GC_Root *node = (GC_Root *)malloc(sizeof(GC_Root));
    if (node == NULL)
        return false;
    node->range = range;
    mutex_lock(&roots->modificationLock);
    node->next = roots->head;
    roots->head = node;
    mutex_unlock(&roots->modificationLock);
    return true;
}

void GC_Roots_RemoveByRange(GC_Roots *roots, AddressRange range) {
    mutex_lock(&roots->modificationLock);
    GC_Root **link = &roots->head;
    while (*link != NULL) {
        GC_Root *current = *link;
        if (AddressRange_Contains(range, current->range)) {
            *link = current->next;
            free(current);
        } else {
            link = &current->next;
        }
    }
    mutex_unlock(&roots->modificationLock);
}

bool GC_Roots_Covers(GC_Roots *roots, const word_t *low, const word_t *high) {
    AddressRange range = {low, high};
    bool covered = false;
    mutex_lock(&roots->modificationLock);
    for (GC_Root *it = roots->head; it != NULL && !covered; it = it->next)
        covered = AddressRange_Contains(it->range, range);
    mutex_unlock(&roots->modificationLock);
    return covered;
}
#endif
