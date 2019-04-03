#ifndef IMMIX_PHASE_H
#define IMMIX_PHASE_H

#include "Heap.h"
#include "GCTypes.h"
#include "Stats.h"

typedef enum {
    gc_idle = 0x0,
    gc_mark_young = 0x1,
    gc_sweep_young = 0x2,
    gc_mark_old = 0x3,
    gc_sweep_old = 0x4
} GCPhase;

static inline void Phase_Set(Heap *heap, GCPhase phase) {
    heap->gcThreads.phase = phase;
}

void Phase_Init(Heap *heap, uint32_t initialBlockCount);
void Phase_StartMark(Heap *heap, bool collectingOld);
void Phase_MarkDone(Heap *heap, bool collectingOld);
void Phase_StartSweep(Heap *heap, bool collectingOld);
void Phase_SweepDone(Heap *heap, Stats *stats);

#endif // IMMIX_PHASE_H
