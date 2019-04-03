#ifndef IMMIX_MARKER_H
#define IMMIX_MARKER_H

#include "Heap.h"
#include "Stats.h"

void Marker_MarkRoots(Heap *heap, Stats *stats, bool collectingOld);
void Marker_Mark(Heap *heap, Stats *stats, bool collectingOld);
void Marker_MarkUtilDone(Heap *heap, Stats *stats, bool collectingOld);
void Marker_MarkAndScale(Heap *heap, Stats *stats, bool collectingOld);
bool Marker_IsMarkDone(Heap *heap);
void Marker_MarkRemembered(Heap *heap, Stats *stats, bool collectingOld);

#endif // IMMIX_MARKER_H
