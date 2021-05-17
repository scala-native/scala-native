#include <stdlib.h>
#include "MemoryMap.h"
#include "MemoryInfo.h"
#include "Parsing.h"

// Dummy GC that maps chunks of 4GB and allocates but never frees.

void *current = 0;
void *end = 0;

static size_t DEFAULT_CHUNK;
static size_t PREALLOC_CHUNK;
static size_t CHUNK;
static size_t TO_NORMAL_MMAP = 1L;
static size_t DO_PREALLOC = 0L; // No Preallocation.

void Prealloc_Or_Default() {

    if (TO_NORMAL_MMAP == 1L) { // Check if we have prealloc env varible
                                // or execute default mmap settings
        size_t memorySize = getMemorySize();

        DEFAULT_CHUNK = // Default Maximum allocation Map 4GB
            Choose_IF(Parse_Env_Or_Default_String("GC_MAXIMUM_HEAP_SIZE", "4G"),
                      Less_OR_Equal, memorySize);

        PREALLOC_CHUNK = // Preallocation
            Choose_IF(Parse_Env_Or_Default("GC_INITIAL_HEAP_SIZE", 0L),
                      Less_OR_Equal, DEFAULT_CHUNK);

        if (PREALLOC_CHUNK == 0L) { // no prealloc settings.
            CHUNK = DEFAULT_CHUNK;
            TO_NORMAL_MMAP = 0L;

        } else { // config prealloc settings and the flag to reset the
                 // mmap settings the next iteration.
            CHUNK = PREALLOC_CHUNK;
            DO_PREALLOC = 1L;    // Do Preallocate.
            TO_NORMAL_MMAP = 2L; // Return settings to normal on next iteration.
        }
    } else if (TO_NORMAL_MMAP == 2L) {
        DO_PREALLOC = 0L;
        CHUNK = DEFAULT_CHUNK;
        TO_NORMAL_MMAP = 0L; // break the cycle and return to normal mmap alloc
    } else {
    }
}

void scalanative_init() {
    Prealloc_Or_Default();
    current = memoryMapPrealloc(CHUNK, DO_PREALLOC);
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
