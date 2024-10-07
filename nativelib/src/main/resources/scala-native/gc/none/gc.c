#if defined(SCALANATIVE_GC_NONE)

// sscanf and getEnv is deprecated in WinCRT, disable warnings
// These functions are not used directly, but are included in
// "shared/Parsing.h". The definition used to disable warnings needs to be
// placed before the first include of Windows.h, depending on the version of
// Windows runtime it might happen while preprocessing some of stdlib headers.
#define _CRT_SECURE_NO_WARNINGS

#include <stdlib.h>
#include <stdio.h>
#include "shared/MemoryMap.h"
#include "shared/MemoryInfo.h"
#include "shared/Parsing.h"
#include "shared/ThreadUtil.h"
#include "shared/ScalaNativeGC.h"
#include <assert.h>

// Dummy GC that maps chunks of memory and allocates but never frees.
#define DEFAULT_CHUNK_SIZE "64M"

#if defined(__has_feature)
#if __has_feature(address_sanitizer)
#define GC_ASAN
#endif
#endif

SN_ThreadLocal void *current = 0;
SN_ThreadLocal void *end = 0;

static size_t DEFAULT_CHUNK;
static size_t PREALLOC_CHUNK;
static size_t CHUNK;
static size_t TO_NORMAL_MMAP = 1L;
static size_t DO_PREALLOC = 0L;     // No Preallocation.
static size_t TOTAL_ALLOCATED = 0L; // Track total allocated memory

static void exitWithOutOfMemory() {
    fprintf(stderr, "Out of heap space\n");
    exit(1);
}

size_t scalanative_GC_get_init_heapsize() {
    return Parse_Env_Or_Default("GC_INITIAL_HEAP_SIZE", 0L);
}

size_t scalanative_GC_get_max_heapsize() {
    return Parse_Env_Or_Default("GC_MAXIMUM_HEAP_SIZE", getMemorySize());
}

size_t scalanative_GC_get_used_heapsize() { return TOTAL_ALLOCATED; }

size_t scalanative_GC_stats_collection_total() { return -1L; }

size_t scalanative_GC_stats_collection_duration_total() { return -1L; }

void Prealloc_Or_Default() {

    if (TO_NORMAL_MMAP == 1L) { // Check if we have prealloc env varible
                                // or execute default mmap settings
        size_t memorySize = getMemorySize();

#if defined(SCALANATIVE_MULTITHREADING_ENABLED)
        // Every starting thread would want to allocate chunk of memory.
        // We want to limit their size to prevent OOM errors.
        DEFAULT_CHUNK =
            Choose_IF(Parse_Env_Or_Default_String("GC_THREAD_HEAP_BLOCK_SIZE",
                                                  DEFAULT_CHUNK_SIZE),
                      Less_OR_Equal, memorySize);
        // Preallocation not support in multithreading mode
#else
        DEFAULT_CHUNK =
            Choose_IF(Parse_Env_Or_Default_String("GC_MAXIMUM_HEAP_SIZE",
                                                  DEFAULT_CHUNK_SIZE),
                      Less_OR_Equal, memorySize);
        PREALLOC_CHUNK = // Preallocation
            Choose_IF(Parse_Env_Or_Default("GC_INITIAL_HEAP_SIZE", 0L),
                      Less_OR_Equal, DEFAULT_CHUNK);
#endif

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

void scalanative_GC_init() {
#ifndef GC_ASAN
    Prealloc_Or_Default();
    current = memoryMapPrealloc(CHUNK, DO_PREALLOC);
    if (current == NULL) {
        const float bytesToMB = 1024.0 * 1024.0;
        fprintf(stderr,
                "[Scala Native None GC] Failed to allocate or grow heap space, "
                "requested size=%.2fMB, available memory=%.2fMB, already "
                "allocated=%.2fMB, should preallocate=%s. Consider setting "
                "GC_MAXIMUM_HEAP_SIZE env variable to limit maximal heap size",
                CHUNK / bytesToMB, getFreeMemorySize() / bytesToMB,
                TOTAL_ALLOCATED / bytesToMB,
                DO_PREALLOC == 0 ? "false" : "true");
        exit(1);
    }
    end = current + CHUNK;
#ifdef _WIN32
    if (!memoryCommit(current, CHUNK)) {
        exitWithOutOfMemory();
    };
#endif // _WIN32
#endif // GC_ASAN
}

void *scalanative_GC_alloc(Rtti *info, size_t size) {
    size = size + (8 - size % 8);
#ifndef GC_ASAN
    if (current + size < end) {
        Object *alloc = (Object *)current;
        alloc->rtti = info;
        current += size;
        TOTAL_ALLOCATED += size;
        return alloc;
    } else {
        scalanative_GC_init();
        return scalanative_GC_alloc(info, size);
    }
#else
    Object *alloc = (Object *)calloc(size, 1);
    alloc->rtti = info;
    TOTAL_ALLOCATED += size;
    return alloc;
#endif
}

void *scalanative_GC_alloc_small(Rtti *info, size_t size) {
    return scalanative_GC_alloc(info, size);
}

void *scalanative_GC_alloc_large(Rtti *info, size_t size) {
    return scalanative_GC_alloc(info, size);
}

void *scalanative_GC_alloc_array(Rtti *info, size_t length, size_t stride) {
    size_t size = info->size + length * stride;
    ArrayHeader *alloc = (ArrayHeader *)scalanative_GC_alloc(info, size);
    alloc->length = length;
    alloc->stride = stride;
    return alloc;
}

void scalanative_GC_collect() {}

void scalanative_GC_set_weak_references_collected_callback(
    WeakReferencesCollectedCallback callback) {}

#ifdef _WIN32
HANDLE scalanative_GC_CreateThread(LPSECURITY_ATTRIBUTES threadAttributes,
                                   SIZE_T stackSize, ThreadStartRoutine routine,
                                   RoutineArgs args, DWORD creationFlags,
                                   DWORD *threadId) {
    return CreateThread(threadAttributes, stackSize, routine, args,
                        creationFlags, threadId);
}
#else
int scalanative_GC_pthread_create(pthread_t *thread, pthread_attr_t *attr,
                                  ThreadStartRoutine routine,
                                  RoutineArgs args) {
    return pthread_create(thread, attr, routine, args);
}
#endif

// ScalaNativeGC interface stubs. None GC does not need STW
void scalanative_GC_set_mutator_thread_state(GC_MutatorThreadState unused){};
void scalanative_GC_yield(){};
void scalanative_GC_add_roots(void *addr_low, void *addr_high) {}
void scalanative_GC_remove_roots(void *addr_low, void *addr_high) {}
#endif
