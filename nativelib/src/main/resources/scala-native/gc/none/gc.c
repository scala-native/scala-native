#include <stdlib.h>
#include "MemoryMap.h"

// Dummy GC that maps chunks of 4GB and allocates but never frees.

// Map 4GB
#define CHUNK (4 * 1024 * 1024 * 1024L)

void *current = 0;
void *end = 0;

void scalanative_init() {
    current = memoryMap(CHUNK);
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
