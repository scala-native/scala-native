#include <stdlib.h>
#ifndef _WIN32
#include <sys/mman.h>
#else
#include "../../os_win_mman.h"
void scalanative_safepoint_init();
#endif

// Darwin defines MAP_ANON instead of MAP_ANONYMOUS
#if !defined(MAP_ANONYMOUS) && defined(MAP_ANON)
#define MAP_ANONYMOUS MAP_ANON
#endif

// Dummy GC that maps chunks of 4GB and allocates but never frees.

// Map 4GB
#define CHUNK ((unsigned long long)4 * 1024 * 1024 * 1024L)
// Allow read and write
#define DUMMY_GC_PROT (PROT_READ | PROT_WRITE)
// Map private anonymous memory, and prevent from reserving swap
#define DUMMY_GC_FLAGS (MAP_NORESERVE | MAP_PRIVATE | MAP_ANONYMOUS)
// Map anonymous memory (not a file)
#define DUMMY_GC_FD -1
#define DUMMY_GC_FD_OFFSET 0

void *current = 0;
void *end = 0;

void scalanative_init() {
    current = mmap(NULL, CHUNK, DUMMY_GC_PROT, DUMMY_GC_FLAGS, DUMMY_GC_FD,
                   DUMMY_GC_FD_OFFSET);
    end = current + CHUNK;
}

void *scalanative_alloc(void *info, size_t size) {
    size = size + (8 - size % 8);
    if (current + size < end) {
        void **alloc = current;
        *alloc = info;
        current += size;
        return alloc;
    } else {
        scalanative_init();
        return scalanative_alloc(info, size);
    }
}

void *scalanative_alloc_small(void *info, size_t size) {
    return scalanative_alloc(info, size);
}

void *scalanative_alloc_large(void *info, size_t size) {
    return scalanative_alloc(info, size);
}

void *scalanative_alloc_atomic(void *info, size_t size) {
    return scalanative_alloc(info, size);
}

void scalanative_collect() {}
