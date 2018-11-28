#include "GCThread.h"
#include "Constants.h"
#include "Sweeper.h"
#include <semaphore.h>

void *GCThread_loop(void *arg) {
    GCThread *thread = (GCThread *) arg;
    Heap *heap = thread->heap;
    sem_t *start = &heap->gcThreads.start;
    Stats *stats = heap->stats;
    while (true) {
        thread->active = false;
        sem_wait(start);
        thread->active = true;

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
            Stats_RecordEvent(stats, event_concurrent_sweep, thread->id, start_ns, end_ns);
        }
    }
    return NULL;
}

void GCThread_Init(GCThread *thread, int id, Heap *heap) {
   thread->id = id;
   thread->heap = heap;
   thread->active = false;

   pthread_create(&thread->self, NULL, GCThread_loop, (void *) thread);
}