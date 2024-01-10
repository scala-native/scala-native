#ifndef GC_ROOTS_H
#define GC_ROOTS_H

#include <stdbool.h>
#include "shared/GCTypes.h"
#include "shared/ThreadUtil.h"

typedef struct AddressRange {
    const word_t *address_low;
    const word_t *address_high;
} AddressRange;

typedef struct GC_Root {
    AddressRange range;
    struct GC_Root *next;
} GC_Root;

typedef struct GC_Roots {
    GC_Root *head;
    mutex_t modificationLock;
} GC_Roots;

INLINE static bool AddressRange_Contains(AddressRange self,
                                         AddressRange other) {
    return (other.address_low >= self.address_low &&
            other.address_high <= self.address_high);
}

GC_Roots *GC_Roots_Init();
/* Add given memory address range to the head of linked list of GC roots*/
void GC_Roots_Add(GC_Roots *roots, AddressRange range);
/* Remove GC roots nodes for which memory adress range is fully contained within
 * range passed as an argument to this method*/
void GC_Roots_RemoveByRange(GC_Roots *roots, AddressRange range);

#endif