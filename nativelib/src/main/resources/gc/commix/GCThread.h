#ifndef IMMIX_GCTHREAD_H
#define IMMIX_GCTHREAD_H

#include "Heap.h"
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
} GCThread;

void GCThread_Init(GCThread *thread, int id, Heap *heap);
void GCThread_JoinAll(Heap *heap);
void GCThread_WakeAll(Heap *heap);

#endif // IMMIX_GCTHREAD_H