#include "MemoryMap.h"

#ifndef _WIN32

#include <sys/mman.h>

    // Allow read and write
#define HEAP_MEM_PROT (PROT_READ | PROT_WRITE)
// Map private anonymous memory, and prevent from reserving swap
#define HEAP_MEM_FLAGS (MAP_NORESERVE | MAP_PRIVATE | MAP_ANONYMOUS)
// Map anonymous memory (not a file)
#define HEAP_MEM_FD -1
#define HEAP_MEM_FD_OFFSET 0

word_t* memoryMap(size_t memorySize)
{
    return mmap(NULL, memorySize, HEAP_MEM_PROT, HEAP_MEM_FLAGS, HEAP_MEM_FD, HEAP_MEM_FD_OFFSET);
}

#else

#include "ScalaWindows.h"

word_t* memoryMap(size_t memorySize)
{
    HANDLE hMapFile;

    hMapFile = CreateFileMappingW(
        INVALID_HANDLE_VALUE,      // use paging file
        NULL,                      // default security
        PAGE_READWRITE,            // read/write access
        (memorySize >> 32),        // maximum object size (high-order DWORD)
        (memorySize & 0xFFFFFFFF), // maximum object size (low-order DWORD)
        NULL);                     // name of mapping object
    
    if (hMapFile == NULL) {
        return NULL;
    }
    return (word_t*)(MapViewOfFile(
            hMapFile,
            FILE_MAP_ALL_ACCESS,
            0,
            0,
            memorySize));
}
#endif

