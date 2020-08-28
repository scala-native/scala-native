#ifndef IMMIX_GCTHREAD_H
#define IMMIX_GCTHREAD_H

#include "Heap.h"
#include "Stats.h"
#include <stdatomic.h>
#include <pthread.h>
#include <stdbool.h>

typedef struct {
    int id;
    Heap *heap;
    atomic_bool active;
    struct {
        // making cursorDone atomic so it keeps sequential consistency with the
        // other atomics
        atomic_uint_fast32_t cursorDone;
    } sweep;
    Stats *stats;
} GCThread;

void GCThread_Init(GCThread *thread, int id, Heap *heap, Stats *stats);
bool GCThread_AnyActive(Heap *heap);
int GCThread_ActiveCount(Heap *heap);
void GCThread_Wake(Heap *heap, int toWake);
void GCThread_WakeMaster(Heap *heap);
void GCThread_WakeWorkers(Heap *heap, int toWake);
void GCThread_ScaleMarkerThreads(Heap *heap, uint32_t remainingFullPackets);

#endif // IMMIX_GCTHREAD_H