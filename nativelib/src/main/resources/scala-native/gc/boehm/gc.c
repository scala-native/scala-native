#if defined(SCALANATIVE_GC_BOEHM)
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
// Enable support for multithreading in BoehmGC
#define GC_THREADS
#endif

#include <gc/gc.h>
#include "shared/ScalaNativeGC.h"
#include <stdlib.h>
#include <string.h>
#include "shared/Parsing.h"

// At the moment we rely on the conservative
// mode of Boehm GC as our garbage collector.

void scalanative_GC_init() { GC_INIT(); }

void *scalanative_GC_alloc(void *info, size_t size) {
    void **alloc = (void **)GC_malloc(size);
    *alloc = info;
    return (void *)alloc;
}

void *scalanative_GC_alloc_small(void *info, size_t size) {
    void **alloc = (void **)GC_malloc(size);
    *alloc = info;
    return (void *)alloc;
}

void *scalanative_GC_alloc_large(void *info, size_t size) {
    void **alloc = (void **)GC_malloc(size);
    *alloc = info;
    return (void *)alloc;
}

void *scalanative_GC_alloc_atomic(void *info, size_t size) {
    void **alloc = (void **)GC_malloc_atomic(size);
    memset(alloc, 0, size);
    *alloc = info;
    return (void *)alloc;
}

size_t scalanative_GC_get_init_heapsize() {
    return Parse_Env_Or_Default("GC_INITIAL_HEAP_SIZE", 0L);
}

size_t scalanative_GC_get_max_heapsize() {
    struct GC_prof_stats_s *stats =
        (struct GC_prof_stats_s *)malloc(sizeof(struct GC_prof_stats_s));
    GC_get_prof_stats(stats, sizeof(struct GC_prof_stats_s));
    size_t heap_sz = stats->heapsize_full;
    free(stats);
    return Parse_Env_Or_Default("GC_MAXIMUM_HEAP_SIZE", heap_sz);
}

void scalanative_GC_collect() { GC_gcollect(); }

void scalanative_GC_register_weak_reference_handler(void *handler) {}

#ifdef SCALANATIVE_MULTITHREADING_ENABLED
#ifdef _WIN32
HANDLE scalanative_GC_CreateThread(LPSECURITY_ATTRIBUTES threadAttributes,
                                   SIZE_T stackSize, ThreadStartRoutine routine,
                                   RoutineArgs args, DWORD creationFlags,
                                   DWORD *threadId) {
    return GC_CreateThread(threadAttributes, stackSize, routine, args,
                           creationFlags, threadId);
}
#else
int scalanative_GC_pthread_create(pthread_t *thread, pthread_attr_t *attr,
                                  ThreadStartRoutine routine,
                                  RoutineArgs args) {
    return GC_pthread_create(thread, attr, routine, args);
}
#endif
#endif // SCALANATIVE_MULTITHREADING_ENABLED

// ScalaNativeGC interface stubs. Boehm GC relies on STW using signal handlers
void scalanative_GC_set_mutator_thread_state(GC_MutatorThreadState unused){};
void scalanative_GC_set_mutator_thread_interruptible(bool){};
void scalanative_GC_yield(){};

void scalanative_GC_add_roots(void *addr_low, void *addr_high) {
    GC_add_roots(addr_low, addr_high);
}

void scalanative_GC_remove_roots(void *addr_low, void *addr_high) {
    GC_remove_roots(addr_low, addr_high);
}
#endif
