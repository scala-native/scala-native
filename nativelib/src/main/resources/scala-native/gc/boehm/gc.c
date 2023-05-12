#include <gc.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "../shared/Parsing.h"

#if defined(_WIN32) || defined(WIN32)
// Boehm on Windows needs User32.lib linked
#pragma comment(lib, "User32.lib")
#endif

// At the moment we rely on the conservative
// mode of Boehm GC as our garbage collector.

void scalanative_init() { GC_init(); }

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

size_t scalanative_get_init_heapsize() {
    return Parse_Env_Or_Default("GC_INITIAL_HEAP_SIZE", 0L);
}

size_t scalanative_get_max_heapsize() {
    struct GC_prof_stats_s *stats =
        (struct GC_prof_stats_s *)malloc(sizeof(struct GC_prof_stats_s));
    GC_get_prof_stats(stats, sizeof(struct GC_prof_stats_s));
    size_t heap_sz = stats->heapsize_full;
    free(stats);
    return Parse_Env_Or_Default("GC_MAXIMUM_HEAP_SIZE", heap_sz);
}

void scalanative_collect() { GC_gcollect(); }

void scalanative_register_weak_reference_handler(void *handler) {}
