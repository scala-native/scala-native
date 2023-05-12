#ifndef IMMIX_HEAP_H
#define IMMIX_HEAP_H

#include "GCTypes.h"
#include "datastructures/Bytemap.h"
#include "datastructures/BlockRange.h"
#include "datastructures/GreyPacket.h"
#include "metadata/LineMeta.h"
#include "Stats.h"
#include <stdio.h>
#include <stdatomic.h>
#include <stdbool.h>
#include "ThreadUtil.h"
#include <fcntl.h>

typedef struct {
    word_t *blockMetaStart;
    word_t *blockMetaEnd;
    word_t *lineMetaStart;
    word_t *lineMetaEnd;
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
        semaphore_t startWorkers;
        semaphore_t startMaster;
        atomic_uint_fast8_t phase;
        int count;
        void *all;
    } gcThreads;
    struct {
        atomic_uint_fast32_t cursor;
        atomic_uint_fast32_t limit;
        atomic_uint_fast32_t coalesceDone;
        atomic_bool postSweepDone;
        mutex_t growMutex;
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
    } lazySweep;
    struct {
        uint64_t lastEnd_ns;
        uint64_t currentStart_ns;
        uint64_t currentEnd_ns;
        atomic_uint_fast32_t total;
        GreyList empty;
        GreyList full;
        GreyList foundWeakRefs;
    } mark;
    Bytemap *bytemap;
    Stats *stats;
} Heap;

extern long long scalanative_nano_time();

static inline bool Heap_IsWordInHeap(Heap *heap, word_t *word) {
    return word >= heap->heapStart && word < heap->heapEnd;
}

static inline LineMeta *Heap_LineMetaForWord(Heap *heap, word_t *word) {
    // assumes there are no gaps between lines
    assert(LINE_COUNT * LINE_SIZE == BLOCK_TOTAL_SIZE);
    assert(Heap_IsWordInHeap(heap, word));
    word_t lineGlobalIndex =
        ((word_t)word - (word_t)heap->heapStart) >> LINE_SIZE_BITS;
    assert(lineGlobalIndex >= 0);
    LineMeta *lineMeta = (LineMeta *)heap->lineMetaStart + lineGlobalIndex;
    assert(lineMeta < (LineMeta *)heap->lineMetaEnd);
    return lineMeta;
}

void Heap_Init(Heap *heap, size_t minHeapSize, size_t maxHeapSize);

void Heap_Collect(Heap *heap);
void Heap_GrowIfNeeded(Heap *heap);
void Heap_Grow(Heap *heap, uint32_t increment);
size_t Heap_getMemoryLimit();

#endif // IMMIX_HEAP_H
