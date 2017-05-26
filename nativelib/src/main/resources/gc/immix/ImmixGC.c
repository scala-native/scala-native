#include <stdio.h>
#include <stdlib.h>
#include <memory.h>
#include "GCTypes.h"
#include "Heap.h"
#include "datastructures/Stack.h"
#include "Marker.h"
#include "Log.h"
#include "Object.h"
#include "State.h"
#include "utils/MathUtils.h"

#define INITIAL_HEAP_SIZE (1024 * 1024UL)

void scalanative_collect();

void scalanative_init() {
    heap = Heap_Create(INITIAL_HEAP_SIZE);
    stack = Stack_Alloc(INITIAL_STACK_SIZE);
}

void *scalanative_alloc(void *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, WORD_SIZE);

    void **alloc = (void **)Heap_Alloc(heap, size);
    *alloc = info;
    return (void *)alloc;
}

void *scalanative_alloc_small(void *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, WORD_SIZE);

    void **alloc = (void **)Heap_AllocSmall(heap, size);
    *alloc = info;
    return (void *)alloc;
}

void *scalanative_alloc_large(void *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, WORD_SIZE);

    void **alloc = (void **)Heap_AllocLarge(heap, size);
    *alloc = info;
    return (void *)alloc;
}

void *scalanative_alloc_atomic(void *info, size_t size) {
    return scalanative_alloc(info, size);
}

void scalanative_collect() { Heap_Collect(heap, stack); }
