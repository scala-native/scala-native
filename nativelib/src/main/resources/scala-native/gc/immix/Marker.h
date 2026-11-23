#ifndef IMMIX_MARKER_H
#define IMMIX_MARKER_H

#include "Heap.h"
#include "immix_commix/datastructures/Stack.h"
#include "immix_commix/headers/ObjectHeader.h"

#ifdef SCALANATIVE_GC_STICKY
// Set for a minor collection that may copy young survivors: marking records
// them and pins the conservatively referenced ones, and leaves line marks to
// Generations_Evacuate.
extern bool Marker_evacuating;
#endif

// A minor collection also traces from remembered objects (Sticky Immix).
void Marker_MarkRoots(Heap *heap, Stack *stack, bool minor);
void Marker_Mark(Heap *heap, Stack *stack);
// Traces the references of an object that is not marked itself: an old
// object in a minor collection.
void Marker_MarkRemembered(Heap *heap, Stack *stack, Object *object);
// Traces the references in the slots from `from` up to `to`.
void Marker_MarkSlots(Heap *heap, Stack *stack, word_t **from, word_t **to);

#endif // IMMIX_MARKER_H
