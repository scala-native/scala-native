#ifndef IMMIX_HEAP_H
#define IMMIX_HEAP_H

#include "GCTypes.h"
#include "Allocator.h"
#include "LargeAllocator.h"
#include "datastructures/Stack.h"
#include "datastructures/Bytemap.h"
#include <stdio.h>

typedef struct {
    FILE *outFile;
    uint64_t collections;
    uint64_t timestamp_us[GC_STATS_MEASUREMENTS];
    uint64_t mark_time_us[GC_STATS_MEASUREMENTS];
    uint64_t sweep_time_us[GC_STATS_MEASUREMENTS];
} HeapStats;

typedef struct {
    size_t memoryLimit;
    word_t *blockHeaderStart;
    word_t *blockHeaderEnd;
    word_t *heapStart;
    word_t *heapEnd;
    size_t smallHeapSize;
    word_t *largeHeapStart;
    word_t *largeHeapEnd;
    size_t largeHeapSize;
    Bytemap *smallBytemap;
    Bytemap *largeBytemap;
    HeapStats *stats;
} Heap;

static inline bool Heap_IsWordInLargeHeap(Heap *heap, word_t *word) {
    return word != NULL && word >= heap->largeHeapStart &&
           word < heap->largeHeapEnd;
}

static inline bool Heap_IsWordInSmallHeap(Heap *heap, word_t *word) {
    return word != NULL && word >= heap->heapStart && word < heap->heapEnd;
}

static inline bool Heap_IsWordInHeap(Heap *heap, word_t *word) {
    return Heap_IsWordInSmallHeap(heap, word) ||
           Heap_IsWordInLargeHeap(heap, word);
}
static inline bool heap_isObjectInHeap(Heap *heap, Object *object) {
    return Heap_IsWordInHeap(heap, (word_t *)object);
}

static inline Bytemap *Heap_BytemapForWord(Heap *heap, word_t *word){
    if (Heap_IsWordInSmallHeap(heap, word)) {
        return heap->smallBytemap;
    } else {
        return heap->largeBytemap;
    }
}

void Heap_Init(Heap *heap, size_t initialSmallHeapSize,
               size_t initialLargeHeapSize);
void Heap_AfterExit(Heap *heap);
word_t *Heap_Alloc(Heap *heap, uint32_t objectSize);
word_t *Heap_AllocSmall(Heap *heap, uint32_t objectSize);
word_t *Heap_AllocLarge(Heap *heap, uint32_t objectSize);

void Heap_Collect(Heap *heap, Stack *stack);

void Heap_Recycle(Heap *heap);
void Heap_Grow(Heap *heap, size_t increment);
void Heap_GrowLarge(Heap *heap, size_t increment);

#endif // IMMIX_HEAP_H
