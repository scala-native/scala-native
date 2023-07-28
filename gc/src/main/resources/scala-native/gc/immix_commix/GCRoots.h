#ifndef GC_ROOTS_H
#define GC_ROOTS_H

#include <stdatomic.h>
#include <stdlib.h>
#include <stdbool.h>
#include "shared/GCTypes.h"

typedef struct AddressRange {
    word_t *address_low;
    word_t *address_high;
} AddressRange;

struct GC_Roots {
    AddressRange range;
    _Atomic(struct GC_Roots *) next;
};
typedef struct GC_Roots GC_Roots;

INLINE static bool AddressRange_Contains(AddressRange self,
                                         AddressRange other) {
    return (other.address_low >= self.address_low &&
            other.address_high <= self.address_high);
}

/* Add given memory address range to the head of linked list of GC roots*/
void GC_Roots_Add(GC_Roots **head, AddressRange range);
/* Remove GC roots nodes for which memory adress range is fully contained within
 * range passed as an argument to this method*/
void GC_Roots_RemoveByRange(GC_Roots **head, AddressRange range);

#endif