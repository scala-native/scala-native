#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX)

#include <stdlib.h>
#include <stdio.h>
#include <stdatomic.h>
#include "immix_commix/CollectorState.h"
#include "immix_commix/Constants.h"
#include "shared/Log.h"
#include "shared/MemoryInfo.h"
#include "shared/MemoryMap.h"
#include "StackTrace.h"

extern void *scalanative_createOutOfMemoryError(void);
__attribute__((noreturn)) extern void scalanative_throw(void *obj);
__attribute__((noreturn)) extern void
scalanative_throwOutOfMemoryErrorFallback(void);

void Heap_exitWithOutOfMemory(const char *details) {
    GC_LOG_ERROR("Out of heap space %s", details);
    StackTrace_PrintStackTrace();
    fflush(stdout);
    // _Exit rather than exit: Heap_Grow holds the blockAllocator lock here, and
    // exit() would run shutdown hooks on a spawned thread whose
    // MutatorThread_init blocks on that same lock, hanging the process.
    _Exit(1);
}

bool Heap_isGrowingPossible(Heap *heap, uint32_t incrementInBlocks) {
    return heap->blockCount + incrementInBlocks <= heap->maxBlockCount;
}

size_t Heap_getMemoryLimit() {
    size_t memorySize = getMemorySize();
    if ((uint64_t)memorySize > MAX_HEAP_SIZE) {
        return (size_t)MAX_HEAP_SIZE;
    } else {
        return memorySize;
    }
}

size_t Heap_getMemoryUsed(Heap *heap) { return heap->heapSize; }

/**
 * Maps `MAX_SIZE` of memory and returns the first address aligned on
 * `alignement` mask
 */
word_t *Heap_mapAndAlign(size_t memoryLimit, size_t alignmentSize) {
    assert(alignmentSize % WORD_SIZE == 0);
    word_t *heapStart = memoryMap(memoryLimit);
    size_t alignmentMask = ~(alignmentSize - 1);
    // Heap start not aligned on
    if (((word_t)heapStart & alignmentMask) != (word_t)heapStart) {
        word_t *previousBlock = (word_t *)((word_t)heapStart & alignmentMask);
        heapStart = previousBlock + alignmentSize / WORD_SIZE;
    }
    return heapStart;
}

void Heap_RefillEmergencyBlock(Heap *heap) {
    if (heap->emergencyBlockStart != NULL)
        return;

    BlockMeta *block = BlockAllocator_GetFreeBlock(&blockAllocator);
    if (block == NULL)
        return;

    heap->emergencyBlockMeta = block;
    heap->emergencyBlockStart =
        BlockMeta_GetBlockStart(heap->blockMetaStart, heap->heapStart, block);
    BlockMeta_SetFlag(block, block_reserved);
    atomic_store_explicit(&heap->emergencyBlockClaimed, false,
                          memory_order_release);
}

void Heap_ThrowOutOfMemory(Heap *heap) {
    if (!Heap_BeginEmergencyAllocation(heap)) {
        scalanative_throwOutOfMemoryErrorFallback();
    }
    void *error = scalanative_createOutOfMemoryError();
    scalanative_throw(error);
}

void Heap_Grow(Heap *heap, uint32_t incrementInBlocks) {
    if (!Heap_TryGrow(heap, incrementInBlocks)) {
        Heap_exitWithOutOfMemory("grow heap");
    }
}

#endif // SCALANATIVE_GC_IMMIX || SCALANATIVE_GC_COMMIX
