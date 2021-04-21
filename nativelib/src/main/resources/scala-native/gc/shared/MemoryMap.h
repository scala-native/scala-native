#ifndef MEMORYMAP_H
#define MEMORYMAP_H

#include "GCTypes.h"
#include <stddef.h>

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
#define DUMMY_GC_PROT (PROT_READ | PROT_WRITE)
// Map private anonymous memory, and prevent from reserving swap

// Allow read and write
#define HEAP_MEM_PROT (PROT_READ | PROT_WRITE)
// Map private anonymous memory, and prevent from reserving swap

#ifdef MAP_NORESERVE
#define HEAP_MEM_FLAGS (MAP_NORESERVE | MAP_PRIVATE | MAP_ANONYMOUS)
#else
#define HEAP_MEM_FLAGS (MAP_PRIVATE | MAP_ANONYMOUS)
#endif

// Map anonymous memory (not a file)
#define HEAP_MEM_FD -1
#define HEAP_MEM_FD_OFFSET 0
#endif // Unix

word_t *memoryMap(size_t memorySize) {
#ifdef _WIN32
    HANDLE hMapFile;
    ULARGE_INTEGER memSize;
    memSize.QuadPart = memorySize;

    hMapFile = CreateFileMappingW(
        INVALID_HANDLE_VALUE, // use paging file
        NULL,                 // default security
        PAGE_READWRITE,       // read/write access
        memSize.u.HighPart,   // maximum object size (high-order DWORD)
        memSize.u.LowPart,    // maximum object size (low-order DWORD)
        NULL);                // name of mapping object

    if (hMapFile == NULL) {
        return NULL;
    }
    return (word_t *)(MapViewOfFile(hMapFile, FILE_MAP_ALL_ACCESS, 0, 0,
                                    memorySize));
#else // Unix
    return mmap(NULL, memorySize, HEAP_MEM_PROT, HEAP_MEM_FLAGS, HEAP_MEM_FD,
                HEAP_MEM_FD_OFFSET);
#endif
}

#endif // MEMORYMAP_H
