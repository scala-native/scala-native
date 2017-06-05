#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "LargeAllocator.h"
#include "utils/MathUtils.h"
#include "Object.h"
#include "Log.h"
#include "headers/ObjectHeader.h"

inline static int LargeAllocator_sizeToLinkedListIndex(size_t size) {
    assert(size >= MIN_BLOCK_SIZE);
    return log2_floor(size) - LARGE_OBJECT_MIN_SIZE_BITS;
}

static inline size_t LargeAllocator_getChunkSize(Chunk *chunk) {
    return (chunk->header.size << WORD_SIZE_BITS);
}

static inline void LargeAllocator_setChunkSize(Chunk *chunk, size_t size) {
    chunk->header.size = (uint32_t)(size >> WORD_SIZE_BITS);
}

Chunk *LargeAllocator_chunkAddOffset(Chunk *chunk, size_t words) {
    return (Chunk *)((ubyte_t *)chunk + words);
}

void LargeAllocator_printFreeList(FreeList *list, int i) {
    Chunk *current = list->first;
    printf("list %d: ", i);
    while (current != NULL) {
        assert(((size_t)1 << (i + LARGE_OBJECT_MIN_SIZE_BITS)) ==
               LargeAllocator_getChunkSize(current));
        printf("[%p %zu] -> ", current, LargeAllocator_getChunkSize(current));
        current = current->next;
    }
    printf("\n");
}

void LargeAllocator_freeListAddBlockLast(FreeList *freeList, Chunk *chunk) {
    if (freeList->first == NULL) {
        freeList->first = chunk;
    }
    freeList->last = chunk;
    chunk->next = NULL;
}

Chunk *LargeAllocator_freeListRemoveFirstBlock(FreeList *freeList) {
    if (freeList->first == NULL) {
        return NULL;
    }
    Chunk *chunk = freeList->first;
    if (freeList->first == freeList->last) {
        freeList->last = NULL;
    }

    freeList->first = chunk->next;
    return chunk;
}

void LargeAllocator_freeListInit(FreeList *freeList) {
    freeList->first = NULL;
    freeList->last = NULL;
}

LargeAllocator *LargeAllocator_Create(word_t *offset, size_t size) {
    LargeAllocator *allocator = malloc(sizeof(LargeAllocator));
    allocator->offset = offset;
    allocator->size = size;
    allocator->bitmap = Bitmap_Alloc(size, offset);

    for (int i = 0; i < FREE_LIST_COUNT; i++) {
        LargeAllocator_freeListInit(&allocator->freeLists[i]);
    }

    LargeAllocator_AddChunk(allocator, (Chunk *)offset, size);

    return allocator;
}

void LargeAllocator_AddChunk(LargeAllocator *allocator, Chunk *chunk,
                             size_t total_block_size) {
    assert(total_block_size >= MIN_BLOCK_SIZE);
    assert(total_block_size % MIN_BLOCK_SIZE == 0);

    size_t remaining_size = total_block_size;
    ubyte_t *current = (ubyte_t *)chunk;
    while (remaining_size > 0) {
        int log2_f = log2_floor(remaining_size);
        size_t chunkSize = ((size_t)1UL) << log2_f;
        chunkSize = chunkSize > MAX_BLOCK_SIZE ? MAX_BLOCK_SIZE : chunkSize;
        assert(chunkSize >= MIN_BLOCK_SIZE && chunkSize <= MAX_BLOCK_SIZE);
        int listIndex = LargeAllocator_sizeToLinkedListIndex(chunkSize);

        Chunk *currentChunk = (Chunk *)current;
        LargeAllocator_freeListAddBlockLast(&allocator->freeLists[listIndex],
                                            (Chunk *)current);
        LargeAllocator_setChunkSize(currentChunk, chunkSize);
        currentChunk->header.type = object_large;
        Object_SetFree(&((Object *)currentChunk)->header);
        Bitmap_SetBit(allocator->bitmap, current);

        current += chunkSize;
        remaining_size -= chunkSize;
    }
}

Object *LargeAllocator_GetBlock(LargeAllocator *allocator,
                                size_t requestedBlockSize) {
    size_t actualBlockSize =
        MathUtils_RoundToNextMultiple(requestedBlockSize, MIN_BLOCK_SIZE);
    size_t requiredChunkSize = (unsigned long long)1UL
                               << MathUtils_Log2Ceil(actualBlockSize);

    int listIndex = LargeAllocator_sizeToLinkedListIndex(requiredChunkSize);
    Chunk *chunk = NULL;
    while (listIndex <= FREE_LIST_COUNT - 1 &&
           (chunk = allocator->freeLists[listIndex].first) == NULL) {
        ++listIndex;
    }

    if (chunk == NULL) {
        return NULL;
    }

    size_t chunkSize = LargeAllocator_getChunkSize(chunk);
    assert(chunkSize >= MIN_BLOCK_SIZE);

    if (chunkSize - MIN_BLOCK_SIZE >= actualBlockSize) {
        Chunk *remainingChunk =
            LargeAllocator_chunkAddOffset(chunk, actualBlockSize);
        LargeAllocator_freeListRemoveFirstBlock(
            &allocator->freeLists[listIndex]);
        size_t remainingChunkSize = chunkSize - actualBlockSize;
        LargeAllocator_AddChunk(allocator, remainingChunk, remainingChunkSize);
    } else {
        LargeAllocator_freeListRemoveFirstBlock(
            &allocator->freeLists[listIndex]);
    }

    Bitmap_SetBit(allocator->bitmap, (ubyte_t *)chunk);
    Object *object = (Object *)chunk;
    Object_SetAllocated(&object->header);
    memset(Object_ToMutatorAddress(object), 0, actualBlockSize - WORD_SIZE);
    return object;
}

void LargeAllocator_Print(LargeAllocator *alloc) {
    for (int i = 0; i < FREE_LIST_COUNT; i++) {
        if (alloc->freeLists[i].first != NULL) {

            LargeAllocator_printFreeList(&alloc->freeLists[i], i);
        }
    }
}

void LargeAllocator_clearFreeLists(LargeAllocator *allocator) {
    for (int i = 0; i < FREE_LIST_COUNT; i++) {
        allocator->freeLists[i].first = NULL;
        allocator->freeLists[i].last = NULL;
    }
}

void LargeAllocator_Sweep(LargeAllocator *allocator) {
    LargeAllocator_clearFreeLists(allocator);

    Object *current = (Object *)allocator->offset;
    void *heapEnd = (ubyte_t *)allocator->offset + allocator->size;

    while (current != heapEnd) {
        assert(Bitmap_GetBit(allocator->bitmap, (ubyte_t *)current));
        ObjectHeader *currentHeader = &current->header;
        if (Object_IsMarked(currentHeader)) {
            Object_SetAllocated(currentHeader);

            current = Object_NextLargeObject(current);
        } else {
            size_t currentSize = Object_ChunkSize(current);
            Object *next = Object_NextLargeObject(current);
            while (next != heapEnd && !Object_IsMarked(&next->header)) {
                currentSize += Object_ChunkSize(next);
                Bitmap_ClearBit(allocator->bitmap, (ubyte_t *)next);
                next = Object_NextLargeObject(next);
            }
            LargeAllocator_AddChunk(allocator, (Chunk *)current, currentSize);
            current = next;
        }
    }
}
