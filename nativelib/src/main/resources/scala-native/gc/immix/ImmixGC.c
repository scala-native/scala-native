#include <stdio.h>
#include <stdlib.h>
#include <memory.h>
#include <pthread.h>
#include "GCTypes.h"
#include "Heap.h"
#include "datastructures/Stack.h"
#include "Marker.h"
#include "Log.h"
#include "Object.h"
#include "State.h"
#include "utils/MathUtils.h"
#include "Constants.h"
#include "Settings.h"
#include "ThreadManager.h"

extern word_t **__stack_bottom;

void scalanative_collect();

void scalanative_afterexit() { Stats_OnExit(heap.stats); }

NOINLINE void scalanative_init() {
    Heap_Init(&heap, Settings_MinHeapSize(), Settings_MaxHeapSize());
    Stack_Init(&stack, INITIAL_STACK_SIZE);
    ThreadManager_Init(&threadManager);
    ThreadManager_RegisterThread(&threadManager, __stack_bottom);
    atexit(scalanative_afterexit);
}

INLINE void *scalanative_alloc(void *info, size_t size) {
    pthread_mutex_lock(&threadManager.mutex);
    size = MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);

    void **alloc = (void **)Heap_Alloc(&threadManager, &heap, size);
    *alloc = info;
    pthread_mutex_unlock(&threadManager.mutex);
    return (void *)alloc;
}

INLINE void *scalanative_alloc_small(void *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);

    pthread_mutex_lock(&threadManager.mutex);
    void **alloc = (void **)Heap_AllocSmall(&threadManager, &heap, size);
    *alloc = info;
    pthread_mutex_unlock(&threadManager.mutex);
    return (void *)alloc;
}

INLINE void *scalanative_alloc_large(void *info, size_t size) {
    pthread_mutex_lock(&threadManager.mutex);
    size = MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);

    void **alloc = (void **)Heap_AllocLarge(&threadManager, &heap, size);
    *alloc = info;
    pthread_mutex_unlock(&threadManager.mutex);
    return (void *)alloc;
}

INLINE void *scalanative_alloc_atomic(void *info, size_t size) {
    return scalanative_alloc(info, size);
}

INLINE void scalanative_collect() {
    pthread_mutex_lock(&threadManager.mutex);
    Heap_Collect(&threadManager, &heap, &stack);
    pthread_mutex_unlock(&threadManager.mutex);
}

INLINE void scalanative_register_thread() {
    pthread_mutex_lock(&threadManager.mutex);
    word_t *dummy;
    ThreadManager_RegisterThread(&threadManager, &dummy);
    pthread_mutex_unlock(&threadManager.mutex);
}
