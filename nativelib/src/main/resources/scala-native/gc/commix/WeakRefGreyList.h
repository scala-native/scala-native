#ifndef WEAK_REF_GREY_LIST
#define WEAK_REF_GREY_LIST

#include "Heap.h"
#include "datastructures/GreyPacket.h"
#include "Stats.h"

void WeakRefGreyList_NullifyAndScale(Heap *heap, Stats *stats);
void WeakRefGreyList_Nullify(Heap *heap, Stats *stats);
void WeakRefGreyList_NullifyUntilDone(Heap *heap, Stats *stats);
void WeakRefGreyList_SetHandler(void *handler);
void WeakRefGreyList_CallHandlers();

#endif // WEAK_REF_GREY_LIST
