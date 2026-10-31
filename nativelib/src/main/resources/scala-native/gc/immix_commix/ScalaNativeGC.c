#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX)

// The entry points of the GC interface (shared/ScalaNativeGC.h) that are the
// same for Immix and Commix. Each keeps its own initialization, small object
// allocation and collection (ImmixGC.c, CommixGC.c).

#include <stdlib.h>
#include <memory.h>
#include <stdatomic.h>
#include <assert.h>
#include "shared/ScalaNativeGC.h"
#include "shared/GCTypes.h"
#include "shared/Parsing.h"
#include "shared/Log.h"
#include "shared/jmx.h"
#include "immix_commix/CollectorState.h"
#include "immix_commix/Settings.h"
#include "immix_commix/utils/MathUtils.h"
#if defined(SCALANATIVE_GC_IMMIX)
#include "immix/WeakReferences.h"
#elif defined(SCALANATIVE_GC_COMMIX)
#include "commix/WeakReferences.h"
#endif
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
#include "immix_commix/Synchronizer.h"
#endif
#include "nativeThreadTLS.h"

INLINE void *scalanative_GC_alloc(Rtti *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);

    assert(size % ALLOCATION_ALIGNMENT == 0);

    Object *alloc;
    if (size >= LARGE_BLOCK_SIZE) {
        alloc = (Object *)LargeAllocator_Alloc(&heap, size);
    } else {
        alloc = (Object *)Allocator_Alloc(&heap, size);
    }
    alloc->rtti = info;
    return (void *)alloc;
}

INLINE void *scalanative_GC_alloc_large(Rtti *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);

    Object *alloc = (Object *)LargeAllocator_Alloc(&heap, size);
    alloc->rtti = info;
    return (void *)alloc;
}

INLINE void *scalanative_GC_alloc_array(Rtti *info, size_t length,
                                        size_t stride) {
    size_t size;
    // A size that a size_t cannot hold (on 32-bit platforms) is larger than
    // any heap: the largest aligned size stands for it, which the large-object
    // allocator turns into an OutOfMemoryError
    if (__builtin_mul_overflow(length, stride, &size) ||
        __builtin_add_overflow(size, (size_t)info->size, &size) ||
        size > MAX_ALLOCATION_SIZE)
        size = MAX_ALLOCATION_SIZE;
    ArrayHeader *alloc = (ArrayHeader *)scalanative_GC_alloc(info, size);
    alloc->length = length;
    alloc->stride = stride;
    return (void *)alloc;
}

INLINE void scalanative_GC_set_weak_references_collected_callback(
    WeakReferencesCollectedCallback callback) {
    WeakReferences_SetGCFinishedCallback(callback);
}

/* Get the minimum heap size */
/* If the user has set a minimum heap size using the GC_INITIAL_HEAP_SIZE
 * environment variable, */
/* then this size will be returned. */
/* Otherwise, the default minimum heap size will be returned.*/
size_t scalanative_GC_get_init_heapsize() { return Settings_MinHeapSize(); }

/* Get the maximum heap size */
/* If the user has set a maximum heap size using the GC_MAXIMUM_HEAP_SIZE
 * environment variable,*/
/* then this size will be returned.*/
/* Otherwise, the total size of the physical memory (guarded) will be returned*/
size_t scalanative_GC_get_max_heapsize() {
    return Parse_Env_Or_Default("GC_MAXIMUM_HEAP_SIZE", Heap_getMemoryLimit());
}

size_t scalanative_GC_get_used_heapsize() { return Heap_getMemoryUsed(&heap); }

size_t scalanative_GC_stats_collection_total() {
    return jmx_stats_get_collection_total();
}

size_t scalanative_GC_stats_collection_duration_total() {
    return jmx_stats_get_collection_duration_total();
}

void scalanative_GC_add_roots(void *addr_low, void *addr_high) {
    AddressRange range = {addr_low, addr_high};
    if (!GC_Roots_Add(customRoots, range))
        abort();
}

void scalanative_GC_remove_roots(void *addr_low, void *addr_high) {
    AddressRange range = {addr_low, addr_high};
    GC_Roots_RemoveByRange(customRoots, range);
}

void scalanative_GC_lock_word_inflated(void *lockWord) {
    word_t **slot = (word_t **)lockWord;
    // Marking reaches a lock word in the heap through its object. The test is
    // against the heap's reservation, whose bounds do not move as it grows.
    if ((uintptr_t)slot - (uintptr_t)heap.heapStart < heap.maxHeapSize)
        return;
    // A zone's object is in a root range already
    if (!GC_Roots_Covers(customRoots, (word_t *)slot, (word_t *)(slot + 1)) &&
        !GC_Roots_Add(customRoots,
                      (AddressRange){(word_t *)slot, (word_t *)(slot + 1)}))
        abort();
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
    volatile word_t stackBottom = 0;
    stackBottom = (word_t)&stackBottom;
    WrappedFunctionCallArgs *wrapped = (WrappedFunctionCallArgs *)args;
    ThreadStartRoutine originalFn = wrapped->fn;
    RoutineArgs originalArgs = wrapped->args;

    free(args);
    if (!MutatorThread_init((Field_t *)stackBottom))
        abort();
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
    if (state == GC_MutatorThreadState_Managed)
        MutatorThread_admit(currentMutatorThread);
    else
        MutatorThread_switchState(currentMutatorThread, state);
}

void scalanative_GC_yield() {
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    if (atomic_load_explicit(&Synchronizer_stopThreads, memory_order_relaxed))
        Synchronizer_yield();
#endif
}

#endif // SCALANATIVE_GC_IMMIX || SCALANATIVE_GC_COMMIX
