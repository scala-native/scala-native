#ifndef IMMIX_HEAP_H
#define IMMIX_HEAP_H

#include "GCTypes.h"
#include "Allocator.h"
#include "LargeAllocator.h"
#include "datastructures/Stack.h"
#include <stdatomic.h>
#include <pthread.h>

typedef struct {
    size_t memoryLimit;
    word_t *heapStart;
    word_t *heapEnd;
    struct {
        atomic_uintptr_t cursor;
        word_t *unsweepable[2];
        atomic_int processes;
        pthread_mutex_t startMutex;
        pthread_mutex_t postActionMutex;
        pthread_cond_t start;
        pthread_cond_t processStopped; // uses postActionMutex
        atomic_bool isDone;
        pthread_t threads[NUM_SWEEP_THREADS];
    } sweep;
    size_t smallHeapSize;
    word_t *largeHeapStart;
    word_t *largeHeapEnd;
    size_t largeHeapSize;
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

void Heap_Init(Heap *heap, size_t initialSmallHeapSize,
               size_t initialLargeHeapSize);
word_t *Heap_Alloc(Heap *heap, uint32_t objectSize);
word_t *Heap_AllocSmall(Heap *heap, uint32_t objectSize);
word_t *Heap_AllocLarge(Heap *heap, uint32_t objectSize);

void Heap_Collect(Heap *heap, Stack *stack);

void Heap_Recycle(Heap *heap);
word_t *Heap_LazySweep(Heap *heap, uint32_t size);
void Heap_SweepFully(Heap *heap);
void Heap_EnsureSweepDone(Heap *heap);
void Heap_Grow(Heap *heap, size_t increment);
void Heap_GrowLarge(Heap *heap, size_t increment);

#endif // IMMIX_HEAP_H
