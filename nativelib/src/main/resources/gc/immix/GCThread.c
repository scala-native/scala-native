#include "GCThread.h"
#include "Constants.h"
#include "Sweeper.h"
#include <semaphore.h>

static inline void GCThread_sweep(GCThread *thread, Heap *heap, Stats *stats) {
    thread->sweep.cursorDone = 0;
    uint64_t start_ns, end_ns;
    if (stats != NULL) {
        start_ns = scalanative_nano_time();
    }
    while (!Sweeper_IsSweepDone(heap)) {
        Sweeper_Sweep(heap, &thread->sweep.cursorDone, SWEEP_BATCH_SIZE);
        Sweeper_LazyCoalesce(heap);
    }
    if (stats != NULL) {
        end_ns = scalanative_nano_time();
        Stats_RecordEvent(stats, event_concurrent_sweep, thread->id,
                          start_ns, end_ns);
    }
}

void *GCThread_loop(void *arg) {
    GCThread *thread = (GCThread *)arg;
    Heap *heap = thread->heap;
    sem_t *start = &heap->gcThreads.start;
    Stats *stats = heap->stats;
    while (true) {
        thread->active = false;
        sem_wait(start);
        thread->active = true;

        uint8_t phase = heap->gcThreads.phase;
        switch (phase) {
            case gc_idle:
                break;
            case gc_sweep:
                GCThread_sweep(thread, heap, stats);
                break;
        }
    }
    return NULL;
}

void GCThread_Init(GCThread *thread, int id, Heap *heap) {
    thread->id = id;
    thread->heap = heap;
    thread->active = false;
    // we do not use the pthread value
    pthread_t self;

    pthread_create(&self, NULL, GCThread_loop, (void *)thread);
}

NOINLINE void GCThread_joinAllSlow(GCThread *gcThreads, int gcThreadCount) {
    // extremely unlikely to enter here
    // unless very many threads running
    bool anyActive = true;
    while (anyActive) {
        sched_yield();
        anyActive = false;
        for (int i = 0; i < gcThreadCount; i++) {
            anyActive |= gcThreads[i].active;
        }
    }
}

INLINE void GCThread_JoinAll(Heap *heap) {
    int gcThreadCount = heap->gcThreads.count;
    GCThread *gcThreads = (GCThread *) heap->gcThreads.all;
    bool anyActive = false;
    for (int i = 0; i < gcThreadCount; i++) {
        anyActive |= gcThreads[i].active;
    }
    if (anyActive) {
        GCThread_joinAllSlow(gcThreads, gcThreadCount);
    }
}

void GCThread_WakeAll(Heap *heap) {
    sem_t *start = &heap->gcThreads.start;
    int gcThreadCount = heap->gcThreads.count;
    for (int i = 0; i < gcThreadCount; i++) {
        sem_post(start);
    }
}