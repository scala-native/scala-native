#include "shared/GCHooks.h"
#include <stddef.h>

#define MAX_ROOT_PROVIDERS 8

typedef struct RootProvider {
    GC_RootProviderFn *provider;
    void *data;
} RootProvider;

// An entry is written before the count that publishes it.
static RootProvider rootProviders[MAX_ROOT_PROVIDERS];
static atomic_size_t rootProvidersCount;
// Guards additions to both tables.
static atomic_flag registrationLock = ATOMIC_FLAG_INIT;

GC_ObjectTracer scalanative_GC_objectTracers[GC_MAX_OBJECT_TRACERS];
atomic_size_t scalanative_GC_objectTracersCount = 0;
atomic_uint_fast64_t scalanative_GC_markCycle = 0;
atomic_uint_fast64_t scalanative_GC_completedMarkCycle = 0;

bool scalanative_GC_add_root_provider(GC_RootProviderFn *provider, void *data) {
    while (atomic_flag_test_and_set(&registrationLock)) {
    }
    size_t count = atomic_load(&rootProvidersCount);
    bool added = count < MAX_ROOT_PROVIDERS;
    if (added) {
        rootProviders[count] = (RootProvider){provider, data};
        atomic_store(&rootProvidersCount, count + 1);
    }
    atomic_flag_clear(&registrationLock);
    return added;
}

#ifdef SCALANATIVE_GC_BOEHM
// gc/boehm/gc.c has the traced anchors.
bool scalanative_GC_add_object_tracer(void *object, GC_ObjectTracerFn *tracer) {
    (void)object;
    (void)tracer;
    return false;
}
#else
void **scalanative_GC_alloc_traced_anchor(GC_ObjectTracerFn *tracer) {
    (void)tracer;
    return NULL;
}

bool scalanative_GC_add_object_tracer(void *object, GC_ObjectTracerFn *tracer) {
    // The type descriptor is the first word of every object.
    void *rtti = *(void **)object;
    while (atomic_flag_test_and_set(&registrationLock)) {
    }
    size_t count = atomic_load(&scalanative_GC_objectTracersCount);
    bool added = count < GC_MAX_OBJECT_TRACERS;
    if (added) {
        scalanative_GC_objectTracers[count] = (GC_ObjectTracer){rtti, tracer};
        atomic_store(&scalanative_GC_objectTracersCount, count + 1);
    }
    atomic_flag_clear(&registrationLock);
    return added;
}
#endif

void GCHooks_scanDynamicRoots(GC_ScanRangeFn *scan, void *ctx) {
    size_t count = atomic_load(&rootProvidersCount);
    for (size_t i = 0; i < count; i++) {
        rootProviders[i].provider(rootProviders[i].data, scan, ctx);
    }
}
