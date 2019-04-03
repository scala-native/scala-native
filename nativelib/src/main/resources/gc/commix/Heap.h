#ifndef IMMIX_HEAP_H
#define IMMIX_HEAP_H

#include "GCTypes.h"
#include "datastructures/Bytemap.h"
#include "datastructures/BlockRange.h"
#include "datastructures/GreyPacket.h"
#include "datastructures/Stack.h"
#include "Stats.h"
#include <stdio.h>
#include <stdatomic.h>
#include <stdbool.h>
#include <pthread.h>
#include <semaphore.h>

typedef struct {
    word_t *blockMetaStart;
    word_t *blockMetaEnd;
    word_t *heapStart;
    word_t *heapEnd;
    word_t *greyPacketsStart;
    size_t heapSize;
    size_t maxHeapSize;
    uint32_t blockCount;
    uint32_t maxBlockCount;
    double maxMarkTimeRatio;
    double minFreeRatio;
    struct {
        sem_t startWorkers;
        sem_t startMaster;
        atomic_uint_fast8_t phase;
        int count;
        void *all;
    } gcThreads;
    struct {
        atomic_uint_fast32_t cursor;
        atomic_uint_fast32_t limit;
        atomic_uint_fast32_t coalesceDone;
        atomic_bool postSweepDone;
        pthread_mutex_t growMutex;
    } sweep;
    struct {
        // making cursorDone atomic so it keeps sequential consistency with the
        // other atomics
        atomic_uint_fast32_t cursorDone;
        // NB! This must be sequentially consistent with sweep.cursor.
        // Otherwise coalescing can miss updates.
        // _First = 1 if active, _Limit = last cursor observed
        BlockRange lastActivity;
        // _First = 1 if active, _Limit = last cursor observed
        BlockRangeVal lastActivityObserved;
        bool nextSweepOld;
    } lazySweep;
    struct {
        uint64_t lastEnd_ns;
        uint64_t currentStart_ns;
        uint64_t currentEnd_ns;
        atomic_uint_fast32_t total;
        GreyList empty;
        GreyList full;
        GreyList rememberedOld;
        GreyPacket *oldRoots;
        GreyList rememberedYoung;
        GreyPacket *youngRoots;
    } mark;
    Bytemap *bytemap;
    Stats *stats;
} Heap;

extern long long scalanative_nano_time();

static inline bool Heap_IsWordInHeap(Heap *heap, word_t *word) {
    return word >= heap->heapStart && word < heap->heapEnd;
}

void Heap_Init(Heap *heap, size_t minHeapSize, size_t maxHeapSize);

void Heap_Collect(Heap *heap, bool collectingOld);
void Heap_GrowIfNeeded(Heap *heap);
void Heap_Grow(Heap *heap, uint32_t increment);

#endif // IMMIX_HEAP_H
