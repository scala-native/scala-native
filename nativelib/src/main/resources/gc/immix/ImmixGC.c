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

#define INITIAL_HEAP_SIZE (128 * 1024 *1024)



void scalanative_collect();

void scalanative_init() {
    heap = Heap_create(INITIAL_HEAP_SIZE);
    stack = Stack_alloc(INITIAL_STACK_SIZE);
}

void* scalanative_alloc(void *info, size_t size) {
    assert(size <= MAX_BLOCK_SIZE);
    size = roundToNextMultiple(size, WORD_SIZE);
    if(heap == NULL) {
        scalanative_init();
    }


    word_t* object = Heap_alloc(heap, (uint32_t) size);
    if(object == NULL) {
        scalanative_collect();

        object = Heap_alloc(heap, (uint32_t) size);
        if(object == NULL) {
            LargeAllocator_print(heap->largeAllocator);
            printf("Failed to alloc: %zu\n", size + 8);
            printf("No more memory available\n");
            fflush(stdout);
            exit(1);
        }
    }
    *(void**)object = info;
    return object;
}

void* scalanative_alloc_small(void* info, size_t size) {
    size = (size + sizeof(word_t) - 1 ) / sizeof(word_t) * sizeof(word_t);

    void** alloc = (void**) Heap_allocSmall(heap, size);
    *alloc = info;
    return (void*) alloc;
}

void* scalanative_alloc_large(void* info, size_t size) {
    size = roundToNextMultiple(size, WORD_SIZE);

    void** alloc = (void**) Heap_allocLarge(heap, size);
    *alloc = info;
    return (void*) alloc;
}

void* scalanative_alloc_atomic(void* info, size_t size) {
    return scalanative_alloc(info, size);
}

void scalanative_collect() {
    Heap_collect(heap, stack);
}
