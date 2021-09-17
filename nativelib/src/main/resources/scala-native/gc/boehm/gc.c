#include <gc.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

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

void scalanative_collect() { GC_gcollect(); }
