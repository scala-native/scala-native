#include <stdlib.h>
#include <sys/mman.h>
#include <pthread.h>
#include <stdatomic.h>

// Darwin defines MAP_ANON instead of MAP_ANONYMOUS
#if !defined(MAP_ANONYMOUS) && defined(MAP_ANON)
#define MAP_ANONYMOUS MAP_ANON
#endif

// Dummy GC that maps chunks of 4GB and allocates but never frees.

// Map 4GB
#define CHUNK (4 * 1024 * 1024 * 1024L)
// Allow read and write
#define DUMMY_GC_PROT (PROT_READ | PROT_WRITE)
// Map private anonymous memory, and prevent from reserving swap
#define DUMMY_GC_FLAGS (MAP_NORESERVE | MAP_PRIVATE | MAP_ANONYMOUS)
// Map anonymous memory (not a file)
#define DUMMY_GC_FD -1
#define DUMMY_GC_FD_OFFSET 0

atomic_long current_atomic = 0;
void *end = 0;

pthread_mutex_t chunk_alloc_mutex = PTHREAD_MUTEX_INITIALIZER;

void allocateChunksUpTo(void *target) {
    pthread_mutex_lock(&chunk_alloc_mutex);
    // do not allocate multiple chunks at once
    if (target >= end) {
        void *current = (void *)current_atomic;
        current = mmap(NULL, CHUNK, DUMMY_GC_PROT, DUMMY_GC_FLAGS, DUMMY_GC_FD,
                       DUMMY_GC_FD_OFFSET);
        current_atomic = (long)current;
        end = current + CHUNK;
    }
    pthread_mutex_unlock(&chunk_alloc_mutex);
}

void scalanative_init() {
    // get some space initially
    allocateChunksUpTo((void *)1);
}

void *scalanative_alloc(void *info, size_t size) {
    size = size + (8 - size % 8);
    void *new_current;
    new_current = (void *)atomic_fetch_add(&current_atomic, size);
    if (new_current >= end) {
        allocateChunksUpTo(new_current);
    }

    void **alloc = new_current;
    *alloc = info;
    return alloc;
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
