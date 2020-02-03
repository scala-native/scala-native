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

void scalanative_collect();

void scalanative_afterexit() { Stats_OnExit(heap.stats); }

NOINLINE void scalanative_init() {
    Heap_Init(&heap, Settings_MinHeapSize(), Settings_MaxHeapSize());
    Stack_Init(&stack, INITIAL_STACK_SIZE);
    ThreadManager_Init();
    register_thread();
    threadList = ThreadList_Cons(pthread_self(), threadList);
    atexit(scalanative_afterexit);
}

INLINE void *scalanative_alloc(void *info, size_t size) {
    pthread_mutex_lock(&mutex);
    size = MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);

    void **alloc = (void **)Heap_Alloc(&heap, size);
    *alloc = info;
    pthread_mutex_unlock(&mutex);
    return (void *)alloc;
}

INLINE void *scalanative_alloc_small(void *info, size_t size) {
    pthread_mutex_lock(&mutex);
    size = MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);

    void **alloc = (void **)Heap_AllocSmall(&heap, size);
    *alloc = info;
    pthread_mutex_unlock(&mutex);
    return (void *)alloc;
}

INLINE void *scalanative_alloc_large(void *info, size_t size) {
    pthread_mutex_lock(&mutex);
    size = MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);

    void **alloc = (void **)Heap_AllocLarge(&heap, size);
    *alloc = info;
    pthread_mutex_unlock(&mutex);
    return (void *)alloc;
}

INLINE void *scalanative_alloc_atomic(void *info, size_t size) {
    pthread_mutex_lock(&mutex);
    void *res = scalanative_alloc(info, size);
    pthread_mutex_unlock(&mutex);
    return res;
}

INLINE void scalanative_collect() {
    puts("collect start");
    pthread_mutex_lock(&mutex);
    Heap_Collect(&heap, &stack);
    pthread_mutex_unlock(&mutex);
    puts("collect end");
}

void scalanative_register_thread() {
    pthread_mutex_lock(&mutex);
    register_thread();
    pthread_mutex_unlock(&mutex);
}
