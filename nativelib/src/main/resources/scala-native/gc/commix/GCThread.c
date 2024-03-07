#if defined(SCALANATIVE_GC_COMMIX)

#include "GCThread.h"
#include "Constants.h"
#include "Sweeper.h"
#include "Marker.h"
#include "Phase.h"
#include "WeakReferences.h"
#include <errno.h>
#include <stdlib.h>
#include "State.h"

#ifdef _WIN32
#define LastError GetLastError()
#define ExitValue 1
#define PRIdErr "lu"
#else
#define LastError errno
#define ExitValue errno
#define PRIdErr "d"
#endif

static inline void GCThread_markMaster(Heap *heap, Stats *stats) {
    Stats_RecordTime(stats, start_ns);
    Stats_PhaseStarted(stats);

    while (!Marker_IsMarkDone(heap)) {
        Marker_MarkAndScale(heap, stats);
        if (!Marker_IsMarkDone(heap)) {
            thread_yield();
        }
    }

    Stats_RecordTime(stats, end_ns);
    Stats_RecordEvent(stats, event_concurrent_mark, start_ns, end_ns);
    Stats_RecordEventSync(stats, mark_waiting, stats->packet_waiting_start_ns,
                          stats->packet_waiting_end_ns);
}

static inline void GCThread_mark(Heap *heap, Stats *stats) {
    Stats_RecordTime(stats, start_ns);
    Stats_PhaseStarted(stats);

    Marker_Mark(heap, stats);
    // Marker on the worker thread stops after failing to get a full packet.

    Stats_RecordTime(stats, end_ns);
    Stats_RecordEvent(stats, event_concurrent_mark, start_ns, end_ns);
    Stats_RecordEvent(stats, mark_waiting, stats->packet_waiting_start_ns,
                      stats->packet_waiting_end_ns);
}

static inline void GCThread_nullify(Heap *heap, Stats *stats) {
    Stats_RecordTime(stats, start_ns);
    Stats_PhaseStarted(stats);

    WeakReferences_Nullify(heap, stats);

    Stats_RecordTime(stats, end_ns);
    Stats_RecordEvent(stats, event_concurrent_nullify, start_ns, end_ns);
}

static inline void GCThread_nullifyMaster(Heap *heap, Stats *stats) {
    Stats_RecordTime(stats, start_ns);
    Stats_PhaseStarted(stats);

    WeakReferences_NullifyAndScale(heap, stats);

    Stats_RecordTime(stats, end_ns);
    Stats_RecordEvent(stats, event_concurrent_nullify, start_ns, end_ns);
}

static inline void GCThread_sweep(GCThread *thread, Heap *heap, Stats *stats) {
    thread->sweep.cursorDone = 0;
    Stats_RecordTime(stats, start_ns);

    while (heap->sweep.cursor < heap->sweep.limit) {
        Sweeper_Sweep(stats, &thread->sweep.cursorDone, SWEEP_BATCH_SIZE, NULL);
    }
    thread->sweep.cursorDone = heap->sweep.limit;

    Stats_RecordTime(stats, end_ns);
    Stats_RecordEvent(stats, event_concurrent_sweep, start_ns, end_ns);
}

static inline void GCThread_sweepMaster(GCThread *thread, Heap *heap,
                                        Stats *stats) {
    thread->sweep.cursorDone = 0;
    Stats_RecordTime(stats, start_ns);

    while (heap->sweep.cursor < heap->sweep.limit) {
        Sweeper_Sweep(stats, &thread->sweep.cursorDone, SWEEP_BATCH_SIZE, NULL);
        Sweeper_LazyCoalesce(heap, stats);
    }
    thread->sweep.cursorDone = heap->sweep.limit;
    while (!Sweeper_IsCoalescingDone(heap)) {
        Sweeper_LazyCoalesce(heap, stats);
    }
    Phase_SweepDone(heap, stats);
    Stats_RecordTime(stats, end_ns);
    Stats_RecordEvent(stats, event_concurrent_sweep, start_ns, end_ns);
}

void *GCThread_loop(void *arg) {
    GCThread *thread = (GCThread *)arg;
    Heap *heap = thread->heap;
    semaphore_t start = heap->gcThreads.startWorkers;
    Stats *stats = Stats_OrNull(thread->stats);

    while (true) {
        thread->active = false;
        if (!semaphore_wait(start)) {
            fprintf(stderr,
                    "Acquiring semaphore failed in commix GCThread_loop, "
                    "error=%" PRIdErr "\n",
                    LastError);
            exit(ExitValue);
        }
        thread->active = true;
        // hard fence before proceeding with the next phase
        atomic_thread_fence(memory_order_seq_cst);

        uint8_t phase = heap->gcThreads.phase;
        switch (phase) {
        case gc_idle:
            break;
        case gc_mark:
            GCThread_mark(heap, stats);
            break;
        case gc_nullify:
            GCThread_nullify(heap, stats);
            break;
        case gc_sweep:
            GCThread_sweep(thread, heap, stats);
            Stats_WriteToFile(stats);
            break;
        }
        // hard fence before proceeding with the next phase
        atomic_thread_fence(memory_order_seq_cst);
    }
    return NULL;
}

void *GCThread_loopMaster(void *arg) {
    GCThread *thread = (GCThread *)arg;
    Heap *heap = thread->heap;
    semaphore_t start = heap->gcThreads.startMaster;
    Stats *stats = Stats_OrNull(thread->stats);
    while (true) {
        thread->active = false;
        if (!semaphore_wait(start)) {
            fprintf(stderr,
                    "Acquiring semaphore failed in commix GCThread_loopMaster "
                    "error=%" PRIdErr "\n",
                    LastError);
            exit(ExitValue);
        }
        thread->active = true;
        // hard fence before proceeding with the next phase
        atomic_thread_fence(memory_order_seq_cst);

        uint8_t phase = heap->gcThreads.phase;
        switch (phase) {
        case gc_idle:
            break;
        case gc_mark:
            GCThread_markMaster(heap, stats);
            break;
        case gc_nullify:
            GCThread_nullifyMaster(heap, stats);
            break;
        case gc_sweep:
            GCThread_sweepMaster(thread, heap, stats);
            Stats_WriteToFile(stats);
            break;
        }
        // hard fence before proceeding with the next phase
        atomic_thread_fence(memory_order_seq_cst);
    }
    return NULL;
}

void GCThread_Init(GCThread *thread, int id, Heap *heap, Stats *stats) {
    thread->id = id;
    thread->heap = heap;
    thread->stats = stats;
    thread->active = false;
    // we do not use the thread handle
    thread_t self;

    if (id == 0) {
        thread_create(&self, GCThread_loopMaster, (void *)thread);
    } else {
        thread_create(&self, GCThread_loop, (void *)thread);
    }
}

bool GCThread_AnyActive(Heap *heap) {
    int gcThreadCount = heap->gcThreads.count;
    GCThread *gcThreads = (GCThread *)heap->gcThreads.all;
    bool anyActive = false;
    for (int i = 0; i < gcThreadCount; i++) {
        if (gcThreads[i].active) {
            return true;
        }
    }
    return false;
}

int GCThread_ActiveCount(Heap *heap) {
    int gcThreadCount = heap->gcThreads.count;
    GCThread *gcThreads = (GCThread *)heap->gcThreads.all;
    int count = 0;
    for (int i = 0; i < gcThreadCount; i++) {
        if (gcThreads[i].active) {
            count += 1;
        }
    }
    return count;
}

INLINE void GCThread_WakeMaster(Heap *heap) {
    if (!semaphore_unlock(heap->gcThreads.startMaster)) {
        fprintf(stderr,
                "Releasing semaphore failed in commix GCThread_WakeMaster, "
                "error=%" PRIdErr "\n",
                LastError);
        exit(ExitValue);
    }
}

INLINE void GCThread_WakeWorkers(Heap *heap, int toWake) {
    semaphore_t startWorkers = heap->gcThreads.startWorkers;
    int maxThreads = heap->gcThreads.count;
    long prevCount = 0;
    for (int i = 0; i < toWake; i++) {
#ifdef _WIN32
        bool status = ReleaseSemaphore(startWorkers, 1, &prevCount);
        if (prevCount > maxThreads)
            break;
        if (!status) {
#else
        if (!semaphore_unlock(startWorkers)) {
#endif
            fprintf(stderr,
                    "Releasing semaphore failed in commix "
                    "GCThread_WakeWorkers, error=%" PRIdErr "\n",
                    LastError);
            exit(ExitValue);
        }
    }
}

INLINE void GCThread_Wake(Heap *heap, int toWake) {
    if (toWake > 0) {
        GCThread_WakeMaster(heap);
        GCThread_WakeWorkers(heap, toWake - 1);
    }
}

void GCThread_ScaleMarkerThreads(Heap *heap, uint32_t remainingFullPackets) {
    if (remainingFullPackets > MARK_SPAWN_THREADS_MIN_PACKETS) {
        int maxThreads = heap->gcThreads.count;
        int targetThreadCount =
            (remainingFullPackets - MARK_SPAWN_THREADS_MIN_PACKETS) /
            MARK_MIN_PACKETS_PER_THREAD;
        if (targetThreadCount > maxThreads) {
            targetThreadCount = maxThreads;
        }
        int activeThreads = GCThread_ActiveCount(heap);
        int toSpawn = targetThreadCount - activeThreads;
        if (toSpawn > 0) {
            GCThread_WakeWorkers(heap, toSpawn);
        }
    }
}

#endif
