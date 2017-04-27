#include <stdio.h>
#include <stdlib.h>
#include <memory.h>
#include "GCTypes.h"
#include "Heap.h"
#include "datastructures/Stack.h"
#include "Marker.h"
#include "Log.h"
#include "Object.h"


#define INITIAL_HEAP_SIZE (128*1024*1024)

Heap* heap = NULL;
Stack* stack = NULL;


void scalanative_collect();

void scalanative_init() {
    heap = heap_create(INITIAL_HEAP_SIZE);
    stack = stack_alloc(INITIAL_STACK_SIZE);
}

void* scalanative_alloc_raw(size_t size) {
    assert(size <= MAX_BLOCK_SIZE);
    size = (size + sizeof(word_t) - 1 ) / sizeof(word_t) * sizeof(word_t);
    if(heap == NULL) {
        scalanative_init();
    }


    ObjectHeader* block = heap_alloc(heap, (uint32_t)size);
    if(block == NULL) {
        scalanative_collect();

        block = heap_alloc(heap, (uint32_t)size);
        if(block == NULL) {
            largeAllocator_print(heap->largeAllocator);
            printf("Failed to alloc: %zu\n", size + 8);
            printf("No more memory available\n");
            fflush(stdout);
            exit(1);
        }
    }

    assert((object_isLargeObject(block) && object_chunkSize(block) > size && object_chunkSize(block) <= 2 * size)
           || (object_isStandardObject(block) && object_size(block) > size && object_size(block) <= 2 * size));
    assert(object_isLargeObject(block) || (word_t*)block >= block_getFirstWord(block_getBlockHeader((word_t*)block)));
    return (word_t*)block + 1;
}

void* scalanative_alloc_raw_atomic(size_t size) {
    return scalanative_alloc_raw(size);
}

void* scalanative_alloc(void* info, size_t size) {
    void** alloc = (void**) scalanative_alloc_raw(size);
    *alloc = info;
    return (void*) alloc;
}

void* alloc(size_t size) {
    return scalanative_alloc_raw(size);
}

void scalanative_collect() {
#ifdef DEBUG_PRINT
    printf("\nCollect\n");
    fflush(stdout);
#endif
    mark_roots(heap, stack);
    bool success = heap_recycle(heap);

    if(!success) {
        printf("Failed to recycle enough memory.\n");
        printf("No more memory available\n");
        fflush(stdout);
        exit(1);
    }
#ifdef DEBUG_PRINT
    printf("End collect\n");
    fflush(stdout);
#endif
}

void scalanative_safepoint() {}
