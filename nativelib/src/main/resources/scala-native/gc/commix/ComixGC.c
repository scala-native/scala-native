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
#include "WeakRefGreyList.h"
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
    // Wait until sweeping will end, otherwise we risk segmentation
    // fault or failing an assertion.
    while (!Sweeper_IsSweepDone(&heap))
        thread_yield();
    Heap_Collect(&heap);
}

INLINE void scalanative_register_weak_reference_handler(void *handler) {
    WeakRefGreyList_SetHandler(handler);
}

INLINE void scalanative_add_roots(void *addr_low, void *addr_high) {
    AddressRange range = {addr_low, addr_high};
    GC_Roots_Add(&roots, range);
}

INLINE void scalanative_remove_roots(void *addr_low, void *addr_high) {
    AddressRange range = {addr_low, addr_high};
    GC_Roots_RemoveByRange(&roots, range);
}