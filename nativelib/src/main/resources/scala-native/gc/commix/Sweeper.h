#ifndef IMMIX_SWEEPER_H
#define IMMIX_SWEEPER_H

#include "Heap.h"
#include "Stats.h"
#include "Phase.h"
#include "MutatorThread.h"

void Sweeper_Sweep(Stats *stats, atomic_uint_fast32_t *cursorDone,
                   uint32_t maxCount, MutatorThread *optionalMutatorThread);
void Sweeper_LazyCoalesce(Heap *heap, Stats *stats);

static inline bool Sweeper_IsCoalescingDone(Heap *heap) {
    return heap->sweep.coalesceDone >= heap->sweep.limit;
}

static inline bool Sweeper_IsSweepDone(Heap *heap) {
    return heap->sweep.postSweepDone;
}

#ifdef GC_ASSERTIONS
void Sweeper_ClearIsSwept(Heap *heap);
void Sweeper_AssertIsConsistent(Heap *heap);
#endif

#endif // IMMIX_SWEEPER_H
