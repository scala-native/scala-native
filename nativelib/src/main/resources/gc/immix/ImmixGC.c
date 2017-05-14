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

#define INITIAL_HEAP_SIZE (128*1024*1024)



void scalanative_collect();

void scalanative_init() {
    heap = heap_create(INITIAL_HEAP_SIZE);
    stack = stack_alloc(INITIAL_STACK_SIZE);
}

void* scalanative_alloc(void *info, size_t size) {
    assert(size <= MAX_BLOCK_SIZE);
    size = (size + sizeof(word_t) - 1 ) / sizeof(word_t) * sizeof(word_t);
    if(heap == NULL) {
        scalanative_init();
    }


    word_t* object = heap_alloc(heap, (uint32_t)size);
    if(object == NULL) {
        scalanative_collect();

        object = heap_alloc(heap, (uint32_t)size);
        if(object == NULL) {
            largeAllocator_print(heap->largeAllocator);
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

    void** alloc = (void**) heap_allocSmall(heap, size);
    *alloc = info;
    return (void*) alloc;
}

void* scalanative_alloc_large(void* info, size_t size) {
    size = (size + sizeof(word_t) - 1 ) / sizeof(word_t) * sizeof(word_t);

    void** alloc = (void**) heap_allocLarge(heap, size);
    *alloc = info;
    return (void*) alloc;
}

void* scalanative_alloc_atomic(void* info, size_t size) {
    return scalanative_alloc(info, size);
}

void scalanative_collect() {
    heap_collect(heap, stack);
}
