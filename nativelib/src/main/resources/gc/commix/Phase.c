#include "GCThread.h"
#include "Phase.h"
#include "State.h"
#include "Allocator.h"
#include "BlockAllocator.h"
#include "Sweeper.h"

void Phase_Init(Heap *heap, uint32_t initialBlockCount) {
    sem_init(&heap->gcThreads.startWorkers, 0, 0);
    sem_init(&heap->gcThreads.startMaster, 0, 0);

    heap->sweep.cursor = initialBlockCount;
    heap->lazySweep.cursorDone = initialBlockCount;
    heap->lazySweep.nextSweepOld = false;
    heap->sweep.limit = initialBlockCount;
    heap->sweep.coalesceDone = initialBlockCount;
    heap->sweep.postSweepDone = true;
    heap->sweep.shouldDoFullCollection = false;
}

void Phase_StartMark(Heap *heap, bool collectingOld) {
    if (collectingOld) {
        // Before collecting the old generation, a full young collection has been done.
        // We add the pointer from young to old in the full set to be processed
        while(GreyList_Size(&heap->mark.rememberedYoung) > 0) {
            GreyPacket *packet = GreyList_Pop(&heap->mark.rememberedYoung, heap->greyPacketsStart);
            GreyList_Push(&heap->mark.full, heap->greyPacketsStart, packet);
        }
        // If the current packet for young roots is not empty, the objects need to be pushed
        if (heap->mark.youngRoots->size > 0) {
            GreyList_Push(&heap->mark.full, heap->greyPacketsStart, heap->mark.youngRoots);
            heap->mark.youngRoots = GreyList_Pop(&heap->mark.empty, heap->greyPacketsStart);
            heap->mark.youngRoots->size = 0;
            heap->mark.youngRoots->type = grey_packet_reflist;
        }
        Phase_Set(heap, gc_mark_old);
    } else {
        // Before a young collection, we reset all the young->old inter-generational pointers
        while (GreyList_Size(&heap->mark.rememberedYoung) > 0) {
            GreyPacket *packet = GreyList_Pop(&heap->mark.rememberedYoung, heap->greyPacketsStart);
            packet->size = 0;
            GreyList_Push(&heap->mark.empty, heap->greyPacketsStart, packet);
        }
        // If there is packet from the last young collection in the set of old objects remembered,
        // we need to add the list of packet to be processed
        while (GreyList_Size(&heap->mark.rememberedOld) > 0) {
            GreyPacket *packet = GreyList_Pop(&heap->mark.rememberedOld, heap->greyPacketsStart);
            GreyList_Push(&heap->mark.full, heap->greyPacketsStart, packet);
        }
        // If the current packet for old roots contains some objects, we need to push them aswell
        if (heap->mark.oldRoots->size > 0) {
            GreyList_Push(&heap->mark.full, heap->greyPacketsStart, heap->mark.oldRoots);
            heap->mark.oldRoots = GreyList_Pop(&heap->mark.empty, heap->greyPacketsStart);
            heap->mark.oldRoots->size = 0;
            heap->mark.oldRoots->type = grey_packet_reflist;
        }
        Phase_Set(heap, gc_mark_young);
    }
    heap->mark.lastEnd_ns = heap->mark.currentEnd_ns;
    heap->mark.currentStart_ns = scalanative_nano_time();
    // make sure the gc phase is propagated
    atomic_thread_fence(memory_order_release);
    GCThread_WakeMaster(heap);
}

void Phase_MarkDone(Heap *heap, bool collectingOld) {
    Phase_Set(heap, gc_idle);
    heap->mark.currentEnd_ns = scalanative_nano_time();
}

void Phase_StartSweep(Heap *heap, bool collectingOld) {
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
    heap->lazySweep.nextSweepOld = collectingOld;
    heap->lazySweep.cursorDone = 0;
    // mark as unitialized
    heap->lazySweep.lastActivity = BlockRange_Pack(2, 0);
    heap->lazySweep.lastActivityObserved = BlockRange_Pack(2, 0);
    heap->sweep.coalesceDone = 0;
    heap->sweep.postSweepDone = false;

    // make sure all running parameters are propagated before phase change
    atomic_thread_fence(memory_order_release);
    if (collectingOld) {
        Phase_Set(heap, gc_sweep_old);
    } else {
        Phase_Set(heap, gc_sweep_young);
    }
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

void Phase_SweepDone(Heap *heap, Stats *stats, bool collectingOld) {
    if (!heap->sweep.postSweepDone) {
        heap->sweep.shouldDoFullCollection = false;
        if (!collectingOld) {
            heap->sweep.shouldDoFullCollection = Heap_shouldGrow(heap);
        } else {
            Heap_GrowIfNeeded(heap);
        }
        BlockAllocator_ReserveBlocks(&blockAllocator);
        BlockAllocator_FinishCoalescing(&blockAllocator);
        Phase_Set(heap, gc_idle);

        Stats_RecordTime(stats, end_ns);
        Stats_RecordEvent(stats, event_collection,
                          heap->stats->collection_start_ns, end_ns);

        heap->sweep.postSweepDone = true;
    }
}
