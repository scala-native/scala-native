#ifndef IMMIX_HEAP_H
#define IMMIX_HEAP_H

#include "GCTypes.h"
#include "Allocator.h"
#include "LargeAllocator.h"
#include "datastructures/Stack.h"
#include "datastructures/Bytemap.h"
#include "metadata/LineMeta.h"
#include "Stats.h"
#include <stdio.h>

typedef struct {
    size_t memoryLimit;
    word_t *blockMetaStart;
    word_t *blockMetaEnd;
    word_t *lineMetaStart;
    word_t *lineMetaEnd;
    word_t *heapStart;
    word_t *heapEnd;
    size_t smallHeapSize;
    word_t *largeHeapStart;
    word_t *largeHeapEnd;
    size_t largeHeapSize;
    Bytemap *smallBytemap;
    Bytemap *largeBytemap;
    Stats *stats;
} Heap;

static inline bool Heap_IsWordInLargeHeap(Heap *heap, word_t *word) {
    return word >= heap->largeHeapStart && word < heap->largeHeapEnd;
}

static inline bool Heap_IsWordInSmallHeap(Heap *heap, word_t *word) {
    return word >= heap->heapStart && word < heap->heapEnd;
}

static inline bool Heap_IsWordInHeap(Heap *heap, word_t *word) {
    return Heap_IsWordInSmallHeap(heap, word) ||
           Heap_IsWordInLargeHeap(heap, word);
}

static inline Bytemap *Heap_BytemapForWord(Heap *heap, word_t *word) {
    if (Heap_IsWordInSmallHeap(heap, word)) {
        return heap->smallBytemap;
    } else if (Heap_IsWordInLargeHeap(heap, word)) {
        return heap->largeBytemap;
    } else {
        return NULL;
    }
}

static inline LineMeta *Heap_LineMetaForWord(Heap *heap, word_t *word) {
    // assumes there are no gaps between lines
    assert(LINE_COUNT * LINE_SIZE == BLOCK_TOTAL_SIZE);
    assert(Heap_IsWordInSmallHeap(heap, word));
    word_t lineGlobalIndex =
        ((word_t)word - (word_t)heap->heapStart) >> LINE_SIZE_BITS;
    assert(lineGlobalIndex >= 0);
    LineMeta *lineMeta = (LineMeta *)heap->lineMetaStart + lineGlobalIndex;
    assert(lineMeta < (LineMeta *)heap->lineMetaEnd);
    return lineMeta;
}

void Heap_Init(Heap *heap, size_t initialSmallHeapSize,
               size_t initialLargeHeapSize);
word_t *Heap_Alloc(Heap *heap, uint32_t objectSize);
word_t *Heap_AllocSmall(Heap *heap, uint32_t objectSize);
word_t *Heap_AllocLarge(Heap *heap, uint32_t objectSize);

void Heap_Collect(Heap *heap, Stack *stack);

void Heap_Recycle(Heap *heap);
void Heap_Grow(Heap *heap, size_t increment);
void Heap_GrowLarge(Heap *heap, size_t increment);

#endif // IMMIX_HEAP_H
