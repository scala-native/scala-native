#ifndef WEAK_REF_GREY_LIST
#define WEAK_REF_GREY_LIST

#include "Heap.h"
#include "Stats.h"

void WeakReferences_NullifyAndScale(Heap *heap, Stats *stats);
void WeakReferences_Nullify(Heap *heap, Stats *stats);
void WeakReferences_NullifyUntilDone(Heap *heap, Stats *stats);
void WeakReferences_SetGCFinishedCallback(void *handler);
void WeakReferences_InvokeGCFinishedCallback();

#endif // WEAK_REF_GREY_LIST
