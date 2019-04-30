#include <stdio.h>
#include <stdlib.h>
#include <memory.h>
#include "GCTypes.h"
#include "Heap.h"
#include "Allocator.h"
#include "LargeAllocator.h"
#include "Marker.h"
#include "Log.h"
#include "Object.h"
#include "State.h"
#include "utils/MathUtils.h"
#include "Constants.h"
#include "Settings.h"
#include "GCThread.h"
#include "Sweeper.h"

void scalanative_collect();

void scalanative_afterexit() {
#ifdef ENABLE_GC_STATS
    Stats_OnExit(heap.stats);

    int gcThreadCount = heap.gcThreads.count;
    GCThread *gcThreads = (GCThread *)heap.gcThreads.all;
    for (int i = 0; i < gcThreadCount; i++) {
        Stats_OnExit(gcThreads[i].stats);
    }
#endif
}

NOINLINE void scalanative_init() {
    Heap_Init(&heap, Settings_MinHeapSize(), Settings_MaxHeapSize());
#ifdef ENABLE_GC_STATS
    atexit(scalanative_afterexit);
#endif
}

INLINE void *scalanative_alloc(void *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);
    assert(size % ALLOCATION_ALIGNMENT == 0);

    void **alloc;
    if (size >= LARGE_BLOCK_SIZE) {
        alloc = (void **)LargeAllocator_Alloc(&heap, size);
    } else if (PRETENURE_OBJECT && size >= PRETENURE_THRESHOLD) {
        alloc = (void **)Allocator_AllocPretenure(&heap, size);
    } else {
        alloc = (void **)Allocator_Alloc(&heap, size);
    }

    *alloc = info;
    return (void *)alloc;
}

INLINE void *scalanative_alloc_small(void *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);

    void **alloc = (void **)Allocator_Alloc(&heap, size);
    *alloc = info;
    return (void *)alloc;
}

INLINE void *scalanative_alloc_large(void *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);

    void **alloc = (void **)LargeAllocator_Alloc(&heap, size);
    *alloc = info;
    return (void *)alloc;
}

INLINE void *scalanative_alloc_atomic(void *info, size_t size) {
    return scalanative_alloc(info, size);
}

INLINE void scalanative_collect() {
    Heap_Collect(&heap, false);
    Heap_Collect(&heap, true);
}

NOINLINE void write_barrier_push_object(Object *object) {
    if (!GreyPacket_Push(heap.mark.oldRoots, object)) {
        atomic_thread_fence(memory_order_acquire);
        GreyList_Push(&heap.mark.full, heap.greyPacketsStart, heap.mark.oldRoots);
        heap.mark.oldRoots = GreyList_Pop(&heap.mark.empty, heap.greyPacketsStart);
        assert(heap.mark.oldRoots != NULL);
        heap.mark.oldRoots->size = 0;
        heap.mark.oldRoots->type = grey_packet_reflist;
        GreyPacket_Push(heap.mark.oldRoots, object);
    }
}

INLINE void write_barrier_no_sweep(Object *object, BlockMeta *blockMeta) {
    ObjectMeta *objectMeta = Bytemap_Get(heap.bytemap, (word_t *)object);
    if (ObjectMeta_IsMarked(objectMeta) && BlockMeta_IsOld(blockMeta)) {
        ObjectMeta_SetMarkedRem(objectMeta);
        write_barrier_push_object(object);
    }
}

NOINLINE void write_barrier_sweep(Object *object, BlockMeta *blockMeta) {
    uint_fast32_t limitIdx = heap.sweep.cursor;
    BlockMeta *sweepDoneUntil = BlockMeta_GetFromIndex(heap.blockMetaStart, (uint32_t) limitIdx);
    if (blockMeta > sweepDoneUntil) {
        write_barrier_push_object(object);
    } else {
        write_barrier_no_sweep(object, blockMeta);
    }
}

NOINLINE void write_barrier_slow(Object *object, BlockMeta *blockMeta) {
    if (!Sweeper_IsSweepDone(&heap)) {
        write_barrier_sweep(object, blockMeta);
    } else {
        write_barrier_no_sweep(object, blockMeta);
    }
}


INLINE void scalanative_write_barrier(void *object) {
    BlockMeta *blockMeta = Block_GetBlockMeta(heap.blockMetaStart, heap.heapStart, (word_t *)object);
    if (BlockMeta_IsOld(blockMeta)) {
        write_barrier_slow(object, blockMeta);
    }
}
