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
#include "ThreadManager.h"

extern word_t **__stack_bottom;

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
    ThreadManager_Init(&threadManager);
    ThreadManager_RegisterThread(&threadManager, __stack_bottom);
#ifdef ENABLE_GC_STATS
    atexit(scalanative_afterexit);
#endif
}

INLINE void *scalanative_alloc(void *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);
    assert(size % ALLOCATION_ALIGNMENT == 0);

    void **alloc;
    pthread_mutex_lock(&threadManager.mutex);
    if (size >= LARGE_BLOCK_SIZE) {
        alloc = (void **)LargeAllocator_Alloc(&threadManager, &heap, size);
    } else {
        alloc = (void **)Allocator_Alloc(&threadManager, &heap, size);
    }

    *alloc = info;
    pthread_mutex_unlock(&threadManager.mutex);
    return (void *)alloc;
}

INLINE void *scalanative_alloc_small(void *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);

    pthread_mutex_lock(&threadManager.mutex);
    void **alloc = (void **)Allocator_Alloc(&threadManager, &heap, size);
    *alloc = info;
    pthread_mutex_unlock(&threadManager.mutex);
    return (void *)alloc;
}

INLINE void *scalanative_alloc_large(void *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);

    pthread_mutex_lock(&threadManager.mutex);
    void **alloc = (void **)LargeAllocator_Alloc(&threadManager, &heap, size);
    *alloc = info;
    pthread_mutex_unlock(&threadManager.mutex);
    return (void *)alloc;
}

INLINE void *scalanative_alloc_atomic(void *info, size_t size) {
    return scalanative_alloc(info, size);
}

INLINE void scalanative_collect() {
    pthread_mutex_lock(&threadManager.mutex);
    Heap_Collect(&threadManager, &heap);
    pthread_mutex_unlock(&threadManager.mutex);
}

INLINE void scalanative_register_thread() {
    pthread_mutex_lock(&threadManager.mutex);
    word_t *dummy;
    ThreadManager_RegisterThread(&threadManager, &dummy);
    pthread_mutex_unlock(&threadManager.mutex);
}
