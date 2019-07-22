#include "GCThread.h"
#include "Phase.h"
#include "State.h"
#include "Allocator.h"
#include "BlockAllocator.h"
#include <stdio.h>
#include <unistd.h>

void Phase_Init(Heap *heap, uint32_t initialBlockCount) {
    pid_t pid = getpid();
    // size = static part + 32 bit int as string
    char startWorkersName[32 + 10];
    char startMasterName[31 + 10];
    snprintf(startWorkersName, 32 + 10, "scalanative_commix_startWorkers_%d",
             pid);
    snprintf(startMasterName, 31 + 10, "scalanative_commix_startMaster_%d",
             pid);
    // only reason for using named semaphores here is for compatibility with
    // MacOs we do not share them across processes
    heap->gcThreads.startWorkers =
        sem_open(startWorkersName, O_CREAT | O_EXCL, 0644, 0);
    heap->gcThreads.startMaster =
        sem_open(startMasterName, O_CREAT | O_EXCL, 0644, 0);
    // clean up when process closes
    // also prevents any other process from `sem_open`ing it
    sem_unlink(startWorkersName);
    sem_unlink(startMasterName);

    heap->sweep.cursor = initialBlockCount;
    heap->lazySweep.cursorDone = initialBlockCount;
    heap->sweep.limit = initialBlockCount;
    heap->sweep.coalesceDone = initialBlockCount;
    heap->sweep.postSweepDone = true;
}

void Phase_StartMark(Heap *heap) {
    heap->mark.lastEnd_ns = heap->mark.currentEnd_ns;
    heap->mark.currentStart_ns = scalanative_nano_time();
    Phase_Set(heap, gc_mark);
    // make sure the gc phase is propagated
    atomic_thread_fence(memory_order_release);
    GCThread_WakeMaster(heap);
}

void Phase_MarkDone(Heap *heap) {
    Phase_Set(heap, gc_idle);
    heap->mark.currentEnd_ns = scalanative_nano_time();
}

void Phase_StartSweep(Heap *heap) {
    Allocator_Clear(&allocator);
    LargeAllocator_Clear(&largeAllocator);
    BlockAllocator_Clear(&blockAllocator);

    // use the reserved block so mutator can does not have to lazy sweep
    // but can allocate imminently
    BlockAllocator_UseReserve(&blockAllocator);

    // all the marking changes should be visible to all threads by now
    atomic_thread_fence(memory_order_seq_cst);

    heap->sweep.cursor = 0;
    uint32_t blockCount = heap->blockCount;
    heap->sweep.limit = blockCount;
    heap->lazySweep.cursorDone = 0;
    // mark as unitialized
    heap->lazySweep.lastActivity = BlockRange_Pack(2, 0);
    heap->lazySweep.lastActivityObserved = BlockRange_Pack(2, 0);
    heap->sweep.coalesceDone = 0;
    heap->sweep.postSweepDone = false;

    // make sure all running parameters are propagated before phase change
    atomic_thread_fence(memory_order_release);
    Phase_Set(heap, gc_sweep);
    // make sure all threads see the phase change
    atomic_thread_fence(memory_order_release);
    // determine how many threads need to start
    int gcThreadCount = heap->gcThreads.count;
    int numberOfBatches = blockCount / SWEEP_BATCH_SIZE;
    int threadsToStart = numberOfBatches / MIN_SWEEP_BATCHES_PER_THREAD;
    if (threadsToStart <= 0) {
        threadsToStart = 1;
    }
    if (threadsToStart > gcThreadCount) {
        threadsToStart = gcThreadCount;
    }
    GCThread_Wake(heap, threadsToStart);
}

void Phase_SweepDone(Heap *heap, Stats *stats) {
    if (!heap->sweep.postSweepDone) {
        Heap_GrowIfNeeded(heap);
        BlockAllocator_ReserveBlocks(&blockAllocator);
        BlockAllocator_FinishCoalescing(&blockAllocator);
        Phase_Set(heap, gc_idle);

        Stats_RecordTime(stats, end_ns);
        Stats_RecordEvent(stats, event_collection,
                          heap->stats->collection_start_ns, end_ns);

        heap->sweep.postSweepDone = true;
    }
}