#ifndef IMMIX_GENERATIONS_H
#define IMMIX_GENERATIONS_H

// Sticky Immix, enabled by SCALANATIVE_GC_STICKY: a non-moving generational
// collector. An object that survives a collection is old. A minor collection
// traces only young objects, from the usual roots plus the old objects the
// write barrier remembered, and frees only young ones. Old objects keep their
// mark state, and their lines and blocks keep their marks, until a full
// collection clears them and traces the whole heap.

#ifdef SCALANATIVE_GC_STICKY

#ifndef SCALANATIVE_GC_IMMIX
#error "SCALANATIVE_GC_STICKY is supported only by Immix"
#endif
#ifndef SCALANATIVE_GC_WRITE_BARRIER
#error "SCALANATIVE_GC_STICKY requires SCALANATIVE_GC_WRITE_BARRIER"
#endif

#include <stdbool.h>
#include "Heap.h"
#include "immix_commix/WriteBarrier.h"
#include "immix_commix/headers/ObjectHeader.h"

void Generations_Init(void);

// Whether the collection about to start traces the whole heap.
bool Generations_ShouldCollectFull(bool explicitCollection);

// Before a full collection: old objects become plain allocated objects
// again, block and line marks are cleared, and remembered objects are
// forgotten, so that the collection traces and sweeps as a non-generational
// one would.
void Generations_PrepareFull(Heap *heap);

// Roots of a minor collection besides stacks, modules and custom roots: the
// remembered objects of all threads, and the old objects that are written
// without the write barrier.
void Generations_MarkMinorRoots(Heap *heap, Stack *stack);

// The marker found a young object that refers to others in a way the write
// barrier does not see: a blob array, or an object with an object tracer
// (shared/GCHooks.h). Once old, it is a root of every minor collection.
void Generations_NoteUnbarriered(Object *object);

// After the sweep. Decides whether the next collection is full.
void Generations_EndCollection(Heap *heap, bool full);

// Keeps the remembered objects of an exiting thread for the next collection.
void Generations_AdoptRememberedSet(RememberedSet *set);

#endif // SCALANATIVE_GC_STICKY

#endif // IMMIX_GENERATIONS_H
