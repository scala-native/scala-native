// sscanf and getEnv is deprecated in WinCRT, disable warnings
// These functions are not used directly, but are included in "Parsing.h".
// The definition used to disable warnings needs to be placed before the first
// include of Windows.h, depending on the version of Windows runtime
// it might happen while preprocessing some of stdlib headers.
#define _CRT_SECURE_NO_WARNINGS

#include <stdlib.h>
#include <stdio.h>
#include "MemoryMap.h"
#include "MemoryInfo.h"
#include "Parsing.h"
#include <ThreadUtil.h>
#include "ScalaNativeGC.h"

// Dummy GC that maps chunks of memory and allocates but never frees.
#ifdef _WIN32
// On Windows we need to commit memory in relatively small chunks - this way
// process would not use too much resources.
#define DEFAULT_CHUNK_SIZE "64M"
#else
#define DEFAULT_CHUNK_SIZE "4G"
#endif

#if defined(__has_feature)
#if __has_feature(address_sanitizer)
#define GC_ASAN
#endif
#endif

thread_local void *current = 0;
thread_local void *end = 0;

static size_t DEFAULT_CHUNK;
static size_t PREALLOC_CHUNK;
static size_t CHUNK;
static size_t TO_NORMAL_MMAP = 1L;
static size_t DO_PREALLOC = 0L; // No Preallocation.

void exitWithOutOfMemory() {
    fprintf(stderr, "Out of heap space\n");
    exit(1);
}

size_t scalanative_get_init_heapsize() {
    return Parse_Env_Or_Default("GC_INITIAL_HEAP_SIZE", 0L);
}

size_t scalanative_get_max_heapsize() {
    return Parse_Env_Or_Default("GC_MAXIMUM_HEAP_SIZE", getMemorySize());
}

void Prealloc_Or_Default() {

    if (TO_NORMAL_MMAP == 1L) { // Check if we have prealloc env varible
                                // or execute default mmap settings
        size_t memorySize = getMemorySize();

        DEFAULT_CHUNK = // Default Maximum allocation Map 4GB
            Choose_IF(Parse_Env_Or_Default_String("GC_MAXIMUM_HEAP_SIZE",
                                                  DEFAULT_CHUNK_SIZE),
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
#ifndef GC_ASAN
    Prealloc_Or_Default();
    current = memoryMapPrealloc(CHUNK, DO_PREALLOC);
    if (current == NULL) {
        exitWithOutOfMemory();
    }
    end = current + CHUNK;
#ifdef _WIN32
    if (!memoryCommit(current, CHUNK)) {
        exitWithOutOfMemory();
    };
#endif // _WIN32
#endif // GC_ASAN
}

void *scalanative_alloc(void *info, size_t size) {
    size = size + (8 - size % 8);
#ifndef GC_ASAN
    if (current + size < end) {
        void **alloc = current;
        *alloc = info;
        current += size;
        return alloc;
    } else {
        scalanative_init();
        return scalanative_alloc(info, size);
    }
#else
    void **alloc = calloc(size, 1);
    *alloc = info;
    return alloc;
#endif
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

void scalanative_register_weak_reference_handler(void *handler) {}

#ifdef SCALANATIVE_MULTITHREADING_ENABLED
#ifdef _WIN32
HANDLE scalanative_CreateThread(LPSECURITY_ATTRIBUTES threadAttributes,
                                SIZE_T stackSize, ThreadStartRoutine routine,
                                RoutineArgs args, DWORD creationFlags,
                                DWORD *threadId) {
    return CreateThread(threadAttributes, stackSize, routine, args,
                        creationFlags, threadId);
}
#else
int scalanative_pthread_create(pthread_t *thread, pthread_attr_t *attr,
                               ThreadStartRoutine routine, RoutineArgs args) {
    return pthread_create(thread, attr, routine, args);
}
#endif
#endif // SCALANATIVE_MULTITHREADING_ENABLED

// ScalaNativeGC interface stubs. None GC does not need STW
void scalanative_gc_set_mutator_thread_state(MutatorThreadState unused){};
void scalanative_gc_safepoint_poll(){};
safepoint_t scalanative_gc_safepoint = NULL;
