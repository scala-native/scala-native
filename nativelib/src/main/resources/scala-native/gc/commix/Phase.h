#ifndef IMMIX_PHASE_H
#define IMMIX_PHASE_H

#include "Heap.h"
#include "shared/GCTypes.h"
#include "Stats.h"

typedef enum {
    gc_idle = 0x0,
    gc_mark = 0x1,
    gc_nullify = 0x2,
    gc_sweep = 0x3
} GCPhase;

static inline void Phase_Set(Heap *heap, GCPhase phase) {
    heap->gcThreads.phase = phase;
}

void Phase_Init(Heap *heap, uint32_t initialBlockCount);
void Phase_StartMark(Heap *heap);
void Phase_MarkDone(Heap *heap);
void Phase_Nullify(Heap *heap, Stats *stats);
void Phase_StartSweep(Heap *heap);
void Phase_SweepDone(Heap *heap, Stats *stats);

#endif // IMMIX_PHASE_H