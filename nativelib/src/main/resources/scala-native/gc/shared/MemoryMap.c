#include "MemoryMap.h"

#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#define NOMINMAX
#include <windows.h>

#else // Unix
#include <sys/mman.h>
// Darwin defines MAP_ANON instead of MAP_ANONYMOUS
#if !defined(MAP_ANONYMOUS) && defined(MAP_ANON)
#define MAP_ANONYMOUS MAP_ANON
#endif

// Allow read and write
#define HEAP_MEM_PROT (PROT_READ | PROT_WRITE)

#ifndef MAP_NORESERVE
#define MAP_NORESERVE 0
#endif

// MAP_POPULATE is linux exclusive. We will use madvice.
#ifndef __linux__
#define MAP_POPULATE 0
#endif

// Map private anonymous memory, and prevent from reserving swap
#define HEAP_MEM_FLAGS (MAP_NORESERVE | MAP_PRIVATE | MAP_ANONYMOUS)
#define HEAP_MEM_FLAGS_PREALLOC                                                \
    (MAP_NORESERVE | MAP_PRIVATE | MAP_ANONYMOUS | MAP_POPULATE)

// Map anonymous memory (not a file)
#define HEAP_MEM_FD -1
#define HEAP_MEM_FD_OFFSET 0
#endif // Unix

word_t *memoryMap(size_t memorySize) {
#ifdef _WIN32
    // On Windows only reserve given chunk of memory. It should be explicitly
    // committed later.
    // We don't use MAP_PHYSICAL flag to prevent usage of swap file, since it
    // supports only 32-bit address space and is in most cases not recommended.
    return VirtualAlloc(NULL, memorySize, MEM_RESERVE, PAGE_NOACCESS);
#else // Unix
    return mmap(NULL, memorySize, HEAP_MEM_PROT, HEAP_MEM_FLAGS, HEAP_MEM_FD,
                HEAP_MEM_FD_OFFSET);
#endif
}

word_t *memoryMapPrealloc(size_t memorySize, size_t doPrealloc) {
#ifdef _WIN32
    // No special pre-alloc support on Windows is needed
    return memoryMap(memorySize);
#else // Unix
    if (!doPrealloc) {
        return memoryMap(memorySize);
    }
    word_t *res = mmap(NULL, memorySize, HEAP_MEM_PROT, HEAP_MEM_FLAGS_PREALLOC,
                       HEAP_MEM_FD, HEAP_MEM_FD_OFFSET);
#ifndef __linux__
    // if we are not on linux the next best thing we can do is to mark the pages
    // as MADV_WILLNEED but only if doPrealloc is enabled.
    madvise(res, memorySize, MADV_WILLNEED);
#endif // __linux__

    return res;
#endif // !_WIN32
}

bool memoryCommit(void *ref, size_t memorySize) {
#ifdef _WIN32
    return VirtualAlloc(ref, memorySize, MEM_COMMIT, PAGE_READWRITE) != NULL;
#else
    // No need for committing on UNIX
    return true;
#endif
}
