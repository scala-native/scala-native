#ifndef IMMIX_HEAP_H
#define IMMIX_HEAP_H

#include "shared/GCTypes.h"
#include "immix_commix/datastructures/Stack.h"
#include "immix_commix/datastructures/Bytemap.h"
#include "immix_commix/metadata/LineMeta.h"
#include "Stats.h"
#include "shared/ThreadUtil.h"
#include <stdatomic.h>

typedef struct {
    word_t *blockMetaStart;
    word_t *blockMetaEnd;
    word_t *lineMetaStart;
    word_t *lineMetaEnd;
    word_t *heapStart;
    word_t *heapEnd;
    atomic_size_t heapSize;
    // Collection completion epoch, used to reject obsolete allocation retries.
    atomic_uint_fast64_t collectionGeneration;
    size_t maxHeapSize;
    uint32_t blockCount;
    uint32_t maxBlockCount;
    word_t *emergencyBlockStart;
    void *emergencyBlockMeta;
    atomic_bool emergencyBlockClaimed;
    Bytemap *bytemap;
    Stats *stats;
    mutex_t lock;
    // For the GC overhead limit: when the last collection ended, and how many
    // recovery collections in a row met both of its conditions.
    uint64_t lastCollectionEndNs;
    uint32_t overheadStreak;
} Heap;

#include "immix_commix/HeapCommon.h"

void Heap_Init(Heap *heap, size_t minHeapSize, size_t maxHeapSize);

void Heap_Collect(Heap *heap, Stack *stack);
bool Heap_RecoverAllocation(Heap *heap, size_t size, uint64_t generation);
void Heap_Recycle(Heap *heap);
bool Heap_TryGrow(Heap *heap, uint32_t increment);
bool Heap_BeginEmergencyAllocation(Heap *heap);

#endif // IMMIX_HEAP_H
