#ifndef IMMIX_HEAP_H
#define IMMIX_HEAP_H

#include "shared/GCTypes.h"
#include "immix_commix/datastructures/Bytemap.h"
#include "datastructures/BlockRange.h"
#include "datastructures/GreyPacket.h"
#include "immix_commix/metadata/LineMeta.h"
#include "Stats.h"
#include <stdatomic.h>
#include <stdbool.h>
#include "shared/ThreadUtil.h"
#include <fcntl.h>
#include "shared/Time.h"

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
    word_t *emergencyBlockStart;
    void *emergencyBlockMeta;
    atomic_bool emergencyBlockClaimed;
    double maxMarkTimeRatio;
    double minFreeRatio;
    // Set only for collections caused by a failed allocation.
    // System.gc() must not drive heap growth.
    atomic_bool growAfterCollection;
    // The timestamp when the GC collection has started
    size_t gcCollectionStart_ns;
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
    mutex_t lock;
} Heap;

#include "immix_commix/HeapCommon.h"

void Heap_Init(Heap *heap, size_t minHeapSize, size_t maxHeapSize);

bool Heap_Collect(Heap *heap, bool allowHeapGrowth);
void Heap_GrowIfNeeded(Heap *heap);
bool Heap_TryGrow(Heap *heap, uint32_t increment);
bool Heap_BeginEmergencyAllocation(Heap *heap);

#endif // IMMIX_HEAP_H
