/* A normal scripted native resource: exercise bootstrap without patching
 * runtime sources. */
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdatomic.h>
#include "externalThreads.h"
#define CHECK(test)                                                            \
    do {                                                                       \
        if (!(test)) {                                                         \
            fprintf(stderr, "bootstrap check failed: %s (%s:%d)\n", #test,     \
                    __FILE__, __LINE__);                                       \
            abort();                                                           \
        }                                                                      \
    } while (0)
#include "external-thread-platform.h"
#ifndef _WIN32
#include <sched.h>
#endif
#ifdef _WIN32
#define BOOTSTRAP_API __declspec(dllexport)
#else
#define BOOTSTRAP_API
#endif

extern int scalanative_threads_init_begin(bool library);
extern int scalanative_threads_init_complete(bool library);
extern void scalanative_threads_init_failed(void);
extern void scalanative_GC_init(void);
extern void *scalanative_currentThread(void);
extern void *scalanative_currentNativeThread(void);

BOOTSTRAP_API void external_bootstrap_failure(int initializeCollector) {
    CHECK(scalanative_threads_init_begin(true) == SCALANATIVE_THREAD_SUCCESS);
    if (initializeCollector)
        scalanative_GC_init();
    scalanative_threads_init_failed();
    scalanative_threads_init_failed();
    CHECK(scalanative_currentThread() == NULL);
    CHECK(scalanative_currentNativeThread() == NULL);
    CHECK(scalanative_threads_init_begin(true) ==
          SCALANATIVE_THREAD_INVALID_OPERATION);
}

#if defined(SCALANATIVE_MULTITHREADING_ENABLED) &&                             \
    (defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX))
/* Opaque runtime types keep this host independent of collector structure
 * layouts. */
extern void *scalanative_currentThreadInfo(void);
extern bool scalanative_detectExternalThreadStack(void *info);
extern bool scalanative_externalGC_register(void);
extern void scalanative_externalGC_admit(void);
extern void scalanative_externalGC_unregister(void);
extern atomic_bool Synchronizer_stopThreads;
extern void external_bootstrap_initialize(void);
extern int mutator_lifecycle_pressure(void);
static atomic_bool collectionFinished;
static void *bootstrap_collect(void *unused) {
    (void)unused;
    CHECK(
        scalanative_detectExternalThreadStack(scalanative_currentThreadInfo()));
    CHECK(scalanative_externalGC_register());
    scalanative_externalGC_admit();
    CHECK(mutator_lifecycle_pressure() == 24);
    scalanative_externalGC_unregister();
    atomic_store_explicit(&collectionFinished, true, memory_order_release);
    return NULL;
}
#endif
BOOTSTRAP_API void external_bootstrap_race(void) {
#if defined(SCALANATIVE_MULTITHREADING_ENABLED) &&                             \
    (defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX))
    CHECK(scalanative_threads_init_begin(true) == SCALANATIVE_THREAD_SUCCESS);
    scalanative_GC_init();
    external_bootstrap_initialize();
    host_thread_t collector = worker_start(bootstrap_collect, NULL);
    /* The collector holds its lifecycle lock and is waiting for this managed
     * loader. Completion must become unmanaged before publishing its context.
     */
    while (!atomic_load_explicit(&Synchronizer_stopThreads,
                                 memory_order_acquire)) {
#ifdef _WIN32
        SwitchToThread();
#else
        sched_yield();
#endif
    }
    CHECK(!atomic_load_explicit(&collectionFinished, memory_order_acquire));
    CHECK(scalanative_threads_init_complete(true) ==
          SCALANATIVE_THREAD_SUCCESS);
    worker_join(collector);
    CHECK(atomic_load_explicit(&collectionFinished, memory_order_acquire));
    scalanative_thread_scope_t scope = scalanative_enter_managed_automatic();
    CHECK(scalanative_currentThread() && scalanative_currentNativeThread());
    CHECK(mutator_lifecycle_pressure() == 24);
    scalanative_leave_managed(scope);
#else
    CHECK(!"bootstrap collection race requires multithreaded Immix or Commix");
#endif
}
