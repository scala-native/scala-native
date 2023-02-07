#ifdef SCALANATIVE_MULTITHREADING_ENABLED
// Enable support for multithreading in BoehmGC
#define GC_THREADS
#endif

#include <gc/gc.h>
#include "../shared/ScalaNativeGC.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

// At the moment we rely on the conservative
// mode of Boehm GC as our garbage collector.

void scalanative_init() { GC_INIT(); }

void *scalanative_alloc(void *info, size_t size) {
    void **alloc = (void **)GC_malloc(size);
    *alloc = info;
    return (void *)alloc;
}

void *scalanative_alloc_small(void *info, size_t size) {
    void **alloc = (void **)GC_malloc(size);
    *alloc = info;
    return (void *)alloc;
}

void *scalanative_alloc_large(void *info, size_t size) {
    void **alloc = (void **)GC_malloc(size);
    *alloc = info;
    return (void *)alloc;
}

void *scalanative_alloc_atomic(void *info, size_t size) {
    void **alloc = (void **)GC_malloc_atomic(size);
    memset(alloc, 0, size);
    *alloc = info;
    return (void *)alloc;
}

void scalanative_collect() { GC_gcollect(); }

void scalanative_register_weak_reference_handler(void *handler) {}

#ifdef SCALANATIVE_MULTITHREADING_ENABLED
#ifdef _WIN32
HANDLE scalanative_CreateThread(LPSECURITY_ATTRIBUTES threadAttributes,
                                SIZE_T stackSize, ThreadStartRoutine routine,
                                RoutineArgs args, DWORD creationFlags,
                                DWORD *threadId) {
    return GC_CreateThread(threadAttributes, stackSize, routine, args,
                           creationFlags, threadId);
}
#else
int scalanative_pthread_create(pthread_t *thread, pthread_attr_t *attr,
                               ThreadStartRoutine routine, RoutineArgs args) {
    return GC_pthread_create(thread, attr, routine, args);
}
#endif
#endif // SCALANATIVE_MULTITHREADING_ENABLED

// ScalaNativeGC interface stubs. Boehm GC relies on STW using signal handlers
void scalanative_gc_set_mutator_thread_state(MutatorThreadState unused){};
void scalanative_gc_safepoint_poll(){};
safepoint_t scalanative_gc_safepoint = NULL;
