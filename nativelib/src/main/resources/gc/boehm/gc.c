#include <gc.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

// At the moment we rely on the conservative
// mode of Boehm GC as our garbage collector.

void scalanative_init() { GC_init(); }

void *scalanative_alloc(void *info, size_t size) {
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
