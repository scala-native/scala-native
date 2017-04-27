#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "LargeAllocator.h"
#include "utils/MathUtils.h"
#include "Object.h"
#include "Log.h"

inline static int size_to_linked_list(size_t size) {
    assert(size >= MIN_BLOCK_SIZE);
    return log2_floor(size) - LARGE_OBJECT_MIN_SIZE_BITS;
}


void largeAllocator_addChunk(LargeAllocator* allocator, Chunk* chunk, size_t total_block_size);

void freeList_init(FreeList* freeList) {
    freeList->first = NULL;
    freeList->last = NULL;
}


LargeAllocator* largeAllocator_create(word_t* offset, size_t size) {
    LargeAllocator* allocator = malloc(sizeof(LargeAllocator));
    allocator->offset = offset;
    allocator->size = size;
    allocator->bitmap = bitmap_alloc(size, offset);

    for(int i=0; i < FREE_LIST_COUNT; i++) {
        freeList_init(&allocator->freeLists[i]);
    }

    largeAllocator_addChunk(allocator, (Chunk*)offset, size);


    return allocator;
}

void freeList_addBlockLast(FreeList* freeList, Chunk* chunk) {
    if(freeList->first == NULL) {
        freeList->first = chunk;
    }
    freeList->last = chunk;
    chunk->next = NULL;
}

Chunk* freeList_removeFirstBlock(FreeList* freeList) {
    if(freeList->first == NULL) {
        return NULL;
    }
    Chunk* chunk = freeList->first;
    if(freeList->first == freeList->last) {
        freeList->last = NULL;
    }

    freeList->first = chunk->next;
    return chunk;
}

Chunk* chunkAddOffset(Chunk* chunk, size_t words) {
    return (Chunk*)((ubyte_t *)chunk + words);
}

void largeAllocator_addChunk(LargeAllocator* allocator, Chunk* chunk, size_t total_block_size) {
    assert(total_block_size >= MIN_BLOCK_SIZE);
    assert(total_block_size % MIN_BLOCK_SIZE == 0);

    size_t remaining_size = total_block_size;
    ubyte_t* current = (ubyte_t*)chunk;
    while(remaining_size > 0) {
        int log2_f = log2_floor(remaining_size);
        size_t chunkSize = 1UL << log2_f;
        chunkSize = chunkSize > MAX_BLOCK_SIZE ? MAX_BLOCK_SIZE : chunkSize;
        assert(chunkSize >= MIN_BLOCK_SIZE && chunkSize <= MAX_BLOCK_SIZE);
        int listIndex = size_to_linked_list(chunkSize);

        Chunk* currentChunk = (Chunk*) current;
        freeList_addBlockLast(&allocator->freeLists[listIndex], (Chunk*)current);
        currentChunk->header.size = (uint32_t)chunkSize;
        currentChunk->header.type = object_large;
        object_setNotAllocated((ObjectHeader*) currentChunk);
        bitmap_setBit(allocator->bitmap, current);

        current += chunkSize;
        remaining_size -= chunkSize;
    }
}

ObjectHeader* largeAllocator_getBlock(LargeAllocator* allocator, size_t requestedBlockSize) {
    size_t actualBlockSize = (requestedBlockSize + MIN_BLOCK_SIZE - 1) / MIN_BLOCK_SIZE * MIN_BLOCK_SIZE;
    size_t requiredChunkSize = 1UL << log2_ceil(actualBlockSize);

    int listIndex = size_to_linked_list(requiredChunkSize);
    Chunk* chunk = NULL;
    while(listIndex <= FREE_LIST_COUNT - 1 && (chunk = allocator->freeLists[listIndex].first) == NULL) {
        ++listIndex;
    }

    if(chunk == NULL) {
        return NULL;
    }

    size_t chunkSize = chunk->header.size;
    assert(chunkSize >= MIN_BLOCK_SIZE);

    if(chunkSize - MIN_BLOCK_SIZE >= actualBlockSize) {
        Chunk* remainingChunk = chunkAddOffset(chunk, actualBlockSize);
        freeList_removeFirstBlock(&allocator->freeLists[listIndex]);
        size_t remainingChunkSize = chunkSize - actualBlockSize;
        largeAllocator_addChunk(allocator, remainingChunk, remainingChunkSize);
    } else {
        freeList_removeFirstBlock(&allocator->freeLists[listIndex]);
    }

    bitmap_setBit(allocator->bitmap, (ubyte_t*)chunk);
    ObjectHeader* object = (ObjectHeader*)chunk;
    object_setAllocated(object);
    memset((word_t*)object + 1, 0, actualBlockSize - WORD_SIZE);
    return object;

}

void freeList_print(FreeList* list, int i) {
    Chunk* current = list->first;
    printf("list %d: ", i);
    while(current != NULL) {
        assert((1 << (i + LARGE_OBJECT_MIN_SIZE_BITS)) == current->header.size);
        printf("[%p %u] -> ", current, current->header.size);
        current = current->next;
    }
    printf("\n");
}

void largeAllocator_print(LargeAllocator* alloc) {
    for(int i = 0; i < FREE_LIST_COUNT; i++) {
        if(alloc->freeLists[i].first != NULL) {

            freeList_print(&alloc->freeLists[i], i);
        }
    }
}

void clearFreeLists(LargeAllocator* allocator) {
    for(int i = 0; i < FREE_LIST_COUNT; i++) {
        allocator->freeLists[i].first = NULL;
        allocator->freeLists[i].last = NULL;
    }
}

void largeAllocator_sweep(LargeAllocator* allocator) {
    clearFreeLists(allocator);

    ObjectHeader* current = (ObjectHeader*) allocator->offset;
    void* heapEnd = (ubyte_t*)allocator->offset + allocator->size;

    while(current != heapEnd) {
        assert(bitmap_getBit(allocator->bitmap, (ubyte_t*)current));
        if(object_isMarked(current)) {
            object_unmarkObjectHeader(current);

            current = objectNextLargeObject(current);
        } else {
            size_t currentSize = object_chunkSize(current);
            ObjectHeader* next = objectNextLargeObject(current);
            while(next != heapEnd && !object_isMarked(next)) {
                currentSize += object_chunkSize(next);
                bitmap_clearBit(allocator->bitmap, (ubyte_t*)next);
                next = objectNextLargeObject(next);
            }
            largeAllocator_addChunk(allocator, (Chunk*) current, currentSize);
            current = next;
        }
    }

}

