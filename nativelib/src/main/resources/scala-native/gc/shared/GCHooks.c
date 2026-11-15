#include "shared/GCHooks.h"

#define MAX_ROOT_PROVIDERS 8

typedef struct RootProvider {
    GC_RootProviderFn *provider;
    void *data;
} RootProvider;

// An entry is written before the count that publishes it.
static RootProvider rootProviders[MAX_ROOT_PROVIDERS];
static atomic_size_t rootProvidersCount;
static atomic_flag rootProvidersLock = ATOMIC_FLAG_INIT;

atomic_uint_fast64_t scalanative_GC_markCycle = 0;
atomic_uint_fast64_t scalanative_GC_completedMarkCycle = 0;

bool scalanative_GC_add_root_provider(GC_RootProviderFn *provider, void *data) {
    while (atomic_flag_test_and_set(&rootProvidersLock)) {
    }
    size_t count = atomic_load(&rootProvidersCount);
    bool added = count < MAX_ROOT_PROVIDERS;
    if (added) {
        rootProviders[count] = (RootProvider){provider, data};
        atomic_store(&rootProvidersCount, count + 1);
    }
    atomic_flag_clear(&rootProvidersLock);
    return added;
}

void GCHooks_scanDynamicRoots(GC_ScanRangeFn *scan, void *ctx) {
    size_t count = atomic_load(&rootProvidersCount);
    for (size_t i = 0; i < count; i++) {
        rootProviders[i].provider(rootProviders[i].data, scan, ctx);
    }
}
