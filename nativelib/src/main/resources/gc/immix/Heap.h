#ifndef IMMIX_HEAP_H
#define IMMIX_HEAP_H

#include "GCTypes.h"
#include "Allocator.h"
#include "LargeAllocator.h"

typedef struct {
    word_t* heapStart;
    word_t* heapEnd;
    size_t smallHeapSize;
    word_t* largeHeapStart;
    word_t* largeHeapEnd;
    word_t* largeHeapSize;
    Allocator* allocator;
    LargeAllocator* largeAllocator;
} Heap;

static inline bool heap_isWordInLargeHeap(Heap* heap, word_t* word) {
    return word != NULL && word >= heap->largeHeapStart && word < heap->largeHeapEnd;
}

static inline bool heap_isWordInSmallHeap(Heap* heap, word_t* word) {
    return word != NULL && word >= heap->heapStart && word < heap->heapEnd;
}

static inline bool heap_isWordInHeap(Heap* heap, word_t* word) {
    return heap_isWordInSmallHeap(heap, word) || heap_isWordInLargeHeap(heap, word);
}
static inline bool heap_isObjectInHeap(Heap* heap, ObjectHeader* object) {
    return heap_isWordInHeap(heap, (word_t*) object);
}

Heap* heap_create(size_t);
ObjectHeader* heap_alloc(Heap*, uint32_t);
bool heap_recycle(Heap*);
void heap_grow(Heap*, size_t);

#endif //IMMIX_HEAP_H
