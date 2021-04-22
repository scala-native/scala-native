#include "GCThread.h"
#include "Phase.h"
#include "State.h"
#include "Allocator.h"
#include "BlockAllocator.h"
#include <stdio.h>
#include <limits.h>
#include "util/ThreadUtil.h"
#include <errno.h>
#include <stdlib.h>
#ifndef _WIN32
#include <unistd.h>
#endif

/*
If in OSX, sem_open cannot create a semaphore whose name is longer than
PSEMNAMLEN characters. Else, we stick to POSIX compliance:

  The length of the name argument cannot exceed {_POSIX_PATH_MAX} on systems
  that do not support the XSI option or exceed {_XOPEN_PATH_MAX} on XSI systems.
  We restrict to _POSIX_PATH_MAX, since it is the most restrictive and
  _XOPEN_PATH_MAX does not seem to be available in all Linux distros

The +1 accounts for the null char at the end of the name
*/
#ifdef __APPLE__
#include <sys/posix_sem.h>
#define SEM_MAX_LENGTH PSEMNAMLEN + 1
#elif defined(_WIN32)
#define SEM_MAX_LENGTH MAX_PATH + 1
#define SEM_FAILED NULL
#else
#define SEM_MAX_LENGTH _POSIX_PATH_MAX + 1
#endif

void Phase_Init(Heap *heap, uint32_t initialBlockCount) {
    pid_t pid = process_getid();
    char startWorkersName[SEM_MAX_LENGTH];
    char startMasterName[SEM_MAX_LENGTH];
    snprintf(startWorkersName, SEM_MAX_LENGTH, "mt_%d_commix", pid);
    snprintf(startMasterName, SEM_MAX_LENGTH, "wk_%d_commix", pid);
    // only reason for using named semaphores here is for compatibility with
    // MacOs we do not share them across processes
    // We open the semaphores and try to check the call succeeded,
    // if not, we exit the process
    heap->gcThreads.startWorkers = semaphore_open(startWorkersName, 0U);
    if (heap->gcThreads.startWorkers == SEM_FAILED) {
        fprintf(stderr,
                "Opening worker semaphore failed in commix Phase_Init\n");
        exit(errno);
    }

    heap->gcThreads.startMaster = semaphore_open(startMasterName, 0U);
    if (heap->gcThreads.startMaster == SEM_FAILED) {
        fprintf(stderr,
                "Opening master semaphore failed in commix Phase_Init\n");
        exit(errno);
    }
    // clean up when process closes
    // also prevents any other process from `sem_open`ing it
    // Closing now semaphore on windows would cause undefined behaviour.
#ifndef _WIN32
    if (sem_unlink(startWorkersName) != 0) {
        fprintf(stderr,
                "Unlinking worker semaphore failed in commix Phase_Init\n");
        exit(errno);
    }
    if (sem_unlink(startMasterName) != 0) {
        fprintf(stderr,
                "Unlinking master semaphore failed in commix Phase_Init\n");
        exit(errno);
    }
#endif

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