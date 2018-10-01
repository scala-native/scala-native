#include <stdio.h>
#include <stdlib.h>
#include <memory.h>
#include <string.h>
#include "GCTypes.h"
#include "Heap.h"
#include "datastructures/Stack.h"
#include "Marker.h"
#include "Log.h"
#include "Object.h"
#include "State.h"
#include "utils/MathUtils.h"
#include "Constants.h"

void scalanative_collect();

/*
 Accepts number of bytes or number with a suffix letter for indicating the units.
 k or K for kilobytes(1024 bytes), m or M for megabytes and g or G for gigabytes.
*/
size_t parseSizeStr(char * str) {
    int length = strlen(str);
    size_t size;
    sscanf(str, "%zu", &size);
    char possibleSuffix = str[length - 1];
    switch (possibleSuffix) {
        case 'k':
        case 'K':
            size *= 1024;
            break;
        case 'm':
        case 'M':
            size *= 1024 * 1024;
            break;
        case 'g':
        case 'G':
            size *= 1024 * 1024 * 1024;
    }
    return size;
}

NOINLINE void scalanative_init() {
    char *minHeapSizeStr = getenv("SCALANATIVE_MIN_HEAP_SIZE");
    size_t minHeapSize;
    if (minHeapSizeStr != NULL) {
        minHeapSize = parseSizeStr(minHeapSizeStr);
    } else {
        minHeapSize = DEFAULT_MIN_SMALL_HEAP_SIZE;
    }

    if (minHeapSize < 2 * BLOCK_TOTAL_SIZE) {
        printf("SCALANATIVE_MIN_HEAP_SIZE too small to initialize heap.\n");
        printf("Minimum required: %dk \n", 2 * BLOCK_TOTAL_SIZE / 1024);
        exit(1);
    }

    char *maxHeapSizeStr = getenv("SCALANATIVE_MAX_HEAP_SIZE");
    size_t maxHeapSize;
    if (maxHeapSizeStr != NULL) {
        maxHeapSize = parseSizeStr(maxHeapSizeStr);
    } else {
        maxHeapSize = UNLIMITED_HEAP_SIZE;
    }


    Heap_Init(&heap, minHeapSize, INITIAL_LARGE_HEAP_SIZE, maxHeapSize);
    Stack_Init(&stack, INITIAL_STACK_SIZE);
}

INLINE void *scalanative_alloc(void *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, WORD_SIZE);

    void **alloc = (void **)Heap_Alloc(&heap, size);
    *alloc = info;
    return (void *)alloc;
}

INLINE void *scalanative_alloc_small(void *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, WORD_SIZE);

    void **alloc = (void **)Heap_AllocSmall(&heap, size);
    *alloc = info;
    return (void *)alloc;
}

INLINE void *scalanative_alloc_large(void *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, WORD_SIZE);

    void **alloc = (void **)Heap_AllocLarge(&heap, size);
    *alloc = info;
    return (void *)alloc;
}

INLINE void *scalanative_alloc_atomic(void *info, size_t size) {
    return scalanative_alloc(info, size);
}

INLINE void scalanative_collect() { Heap_Collect(&heap, &stack); }
