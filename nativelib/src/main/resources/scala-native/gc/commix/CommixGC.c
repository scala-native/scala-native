#if defined(SCALANATIVE_GC_COMMIX)
#include <shared/ScalaNativeGC.h>
#include <stdlib.h>
#include <memory.h>
#include "shared/GCTypes.h"
#include "Heap.h"
#include "Allocator.h"
#include "LargeAllocator.h"
#include "Marker.h"
#include "immix_commix/Log.h"
#include "Object.h"
#include "State.h"
#include "immix_commix/utils/MathUtils.h"
#include "Constants.h"
#include "Settings.h"
#include "GCThread.h"
#include "WeakRefGreyList.h"
#include "Sweeper.h"
#include "immix_commix/Synchronizer.h"

#include "shared/Parsing.h"

#ifdef SCALANATIVE_MULTITHREADING_ENABLED
#include "Synchronizer.h"
#endif
#include "MutatorThread.h"
#include <stdatomic.h>

void scalanative_GC_collect();

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

NOINLINE void scalanative_GC_init() {
    volatile int dummy = 0;
    Heap_Init(&heap, Settings_MinHeapSize(), Settings_MaxHeapSize());
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    Synchronizer_init();
    weakRefsHandlerThread = GCThread_WeakThreadsHandler_Start();
#endif
    MutatorThreads_init();
    MutatorThread_init((word_t **)&dummy); // approximate stack bottom
    customRoots = GC_Roots_Init();
#ifdef ENABLE_GC_STATS
    atexit(scalanative_afterexit);
#endif
}

INLINE void *scalanative_GC_alloc(void *info, size_t size) {
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

INLINE void *scalanative_GC_alloc_small(void *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);

    void **alloc = (void **)Allocator_Alloc(&heap, size);
    *alloc = info;
    return (void *)alloc;
}

INLINE void *scalanative_GC_alloc_large(void *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);

    void **alloc = (void **)LargeAllocator_Alloc(&heap, size);
    *alloc = info;
    return (void *)alloc;
}

INLINE void *scalanative_GC_alloc_atomic(void *info, size_t size) {
    return scalanative_GC_alloc(info, size);
}

INLINE void scalanative_GC_collect() {
    // Wait until sweeping will end, otherwise we risk segmentation
    // fault or failing an assertion.
    while (!Sweeper_IsSweepDone(&heap))
        thread_yield();
    Heap_Collect(&heap);
}

INLINE void scalanative_GC_register_weak_reference_handler(void *handler) {
    WeakRefGreyList_SetHandler(handler);
}

/* Get the minimum heap size */
/* If the user has set a minimum heap size using the GC_INITIAL_HEAP_SIZE
 * environment variable, */
/* then this size will be returned. */
/* Otherwise, the default minimum heap size will be returned.*/
size_t scalanative_GC_get_init_heapsize() { return Settings_MinHeapSize(); }

/* Get the maximum heap size */
/* If the user has set a maximum heap size using the GC_MAXIMUM_HEAP_SIZE */
/* environment variable,*/
/* then this size will be returned.*/
/* Otherwise, the total size of the physical memory (guarded) will be returned*/
size_t scalanative_GC_get_max_heapsize() {
    return Parse_Env_Or_Default("GC_MAXIMUM_HEAP_SIZE", Heap_getMemoryLimit());
}

void scalanative_GC_add_roots(void *addr_low, void *addr_high) {
    AddressRange range = {addr_low, addr_high};
    GC_Roots_Add(customRoots, range);
}

void scalanative_GC_remove_roots(void *addr_low, void *addr_high) {
    AddressRange range = {addr_low, addr_high};
    GC_Roots_RemoveByRange(customRoots, range);
}

typedef void *RoutineArgs;
typedef struct {
    ThreadStartRoutine fn;
    RoutineArgs args;
} WrappedFunctionCallArgs;

#ifdef _WIN32
static ThreadRoutineReturnType WINAPI ProxyThreadStartRoutine(void *args) {
#else
static ThreadRoutineReturnType ProxyThreadStartRoutine(void *args) {
#endif
    WrappedFunctionCallArgs *wrapped = (WrappedFunctionCallArgs *)args;
    ThreadStartRoutine originalFn = wrapped->fn;
    RoutineArgs originalArgs = wrapped->args;
    int stackBottom = 0;

    free(args);
    MutatorThread_init((Field_t *)&stackBottom);
    originalFn(originalArgs);
    MutatorThread_delete(currentMutatorThread);
    return (ThreadRoutineReturnType)0;
}

#ifdef _WIN32
HANDLE scalanative_GC_CreateThread(LPSECURITY_ATTRIBUTES threadAttributes,
                                   SIZE_T stackSize, ThreadStartRoutine routine,
                                   RoutineArgs args, DWORD creationFlags,
                                   DWORD *threadId) {
    WrappedFunctionCallArgs *proxyArgs =
        (WrappedFunctionCallArgs *)malloc(sizeof(WrappedFunctionCallArgs));
    proxyArgs->fn = routine;
    proxyArgs->args = args;
    return CreateThread(threadAttributes, stackSize,
                        (ThreadStartRoutine)&ProxyThreadStartRoutine,
                        (RoutineArgs)proxyArgs, creationFlags, threadId);
}
#else
int scalanative_GC_pthread_create(pthread_t *thread, pthread_attr_t *attr,
                                  ThreadStartRoutine routine,
                                  RoutineArgs args) {
    WrappedFunctionCallArgs *proxyArgs =
        (WrappedFunctionCallArgs *)malloc(sizeof(WrappedFunctionCallArgs));
    proxyArgs->fn = routine;
    proxyArgs->args = args;
    return pthread_create(thread, attr,
                          (ThreadStartRoutine)&ProxyThreadStartRoutine,
                          (RoutineArgs)proxyArgs);
}
#endif

void scalanative_GC_set_mutator_thread_state(GC_MutatorThreadState state) {
    MutatorThread_switchState(currentMutatorThread, state);
}
void scalanative_GC_yield() {
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    if (atomic_load_explicit(&Synchronizer_stopThreads, memory_order_relaxed))
        Synchronizer_yield();
#endif
}

#endif // defined(SCALANATIVE_GC_COMMIX)
