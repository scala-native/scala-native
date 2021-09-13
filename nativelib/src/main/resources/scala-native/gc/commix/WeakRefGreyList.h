#ifndef WEAK_REF_GREY_LIST
#define WEAK_REF_GREY_LIST

#include "Heap.h"
#include "datastructures/GreyPacket.h"
#include "Stats.h"

void WeakRefGreyList_GiveWeakRefPacket(Heap *heap, Stats *stats,
                                       GreyPacket *packet);
void WeakRefGreyList_Nullify(Heap *heap);
void WeakRefGreyList_CallHandlers();

#endif // WEAK_REF_GREY_LIST