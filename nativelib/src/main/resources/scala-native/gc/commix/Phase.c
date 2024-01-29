#if defined(SCALANATIVE_GC_COMMIX)

#include "GCThread.h"
#include "Phase.h"
#include "State.h"
#include "Allocator.h"
#include "BlockAllocator.h"
#include <stdio.h>
#include <limits.h>
#include "shared/ThreadUtil.h"
#include <errno.h>
#include <stdlib.h>
#include "WeakRefGreyList.h"
#include "Stats.h"
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

#if defined(__FreeBSD__)
#define SEM_NAME_PREFIX "/" // FreeBSD semaphore names must start with '/'
#else
#define SEM_NAME_PREFIX ""
#endif // __FreeBSD__

    snprintf(startWorkersName, SEM_MAX_LENGTH, SEM_NAME_PREFIX "mt_%d_commix",
             pid);
    snprintf(startMasterName, SEM_MAX_LENGTH, SEM_NAME_PREFIX "wk_%d_commix",
             pid);

    // only reason for using named semaphores here is for compatibility with
    // MacOs we do not share them across processes
    // We open the semaphores and try to check the call succeeded,
    // if not, we exit the process
    if (!semaphore_open(&heap->gcThreads.startWorkers, startWorkersName, 0U)) {
        fprintf(stderr,
                "Opening worker semaphore failed in commix Phase_Init: %d\n",
                errno);
        exit(errno);
    }

    if (!semaphore_open(&heap->gcThreads.startMaster, startMasterName, 0U)) {
        fprintf(stderr,
                "Opening master semaphore failed in commix Phase_Init: %d\n",
                errno);
        exit(errno);
    }
    // clean up when process closes
    // also prevents any other process from `sem_open`ing it
    // On Windows we don't have equivalent of `sem_unlink` - semaphore would be
    // removed automatically after all its handles would be closed. In our case
    // it happens at process exit, since we do never explicitly close
    // semaphores.
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

void Phase_Nullify(Heap *heap, Stats *stats) {
    if (GreyList_Size(&heap->mark.foundWeakRefs) != 0) {
        uint64_t nullifyStart = scalanative_nano_time();
        Phase_Set(heap, gc_nullify);
        // make sure all threads see the phase change
        atomic_thread_fence(memory_order_release);

        GCThread_WakeMaster(heap);
        WeakRefGreyList_NullifyUntilDone(heap, stats);
        Phase_Set(heap, gc_idle);

        uint64_t nullifyEnd = scalanative_nano_time();
        Stats_RecordEvent(stats, event_nullify, nullifyStart, nullifyEnd);
    }
}

void Phase_StartSweep(Heap *heap) {
    MutatorThreads_foreach(mutatorThreads, node) {
        MutatorThread *thread = node->value;
        Allocator_Clear(&thread->allocator);
        LargeAllocator_Clear(&thread->largeAllocator);
    }
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
    int activeThreads = GCThread_ActiveCount(heap);
    GCThread_Wake(heap, threadsToStart - activeThreads);
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
        atomic_thread_fence(memory_order_release);
    }
}

#endif
