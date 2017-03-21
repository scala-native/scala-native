#include <stdlib.h>
#include <sys/mman.h>

// Dummy GC that maps 1tb of memory and allocates but never frees.

void* start = 0;
void* last = 0;

#define CHUNK 1024*1024*1024*1024L

void scalanative_init() {
    start = mmap(NULL, CHUNK, PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    last = start;
}

void* scalanative_alloc_raw(size_t size) {
    size = size + (8 - size % 8);
    if (start != 0) {
        void* alloc = last;
        last += size;
        return alloc;
    } else {
        scalanative_init();
        return scalanative_alloc_raw(size);
    }
}

void* scalanative_alloc_raw_atomic(size_t size) {
    return scalanative_alloc_raw(size);
}

void* scalanative_alloc(void* info, size_t size) {
    void** alloc = (void**) scalanative_alloc_raw(size);
    *alloc = info;
    return (void*) alloc;
}

void scalanative_collect() {}
