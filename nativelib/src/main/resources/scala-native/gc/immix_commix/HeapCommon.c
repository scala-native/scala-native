#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX)

#include <stdlib.h>
#include <stdio.h>
#include <stdatomic.h>
#include "immix_commix/CollectorState.h"
#include "immix_commix/Constants.h"
#include "shared/Log.h"
#include "shared/MemoryInfo.h"
#include "shared/MemoryMap.h"
#include "immix_commix/WriteBarrier.h"
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
    uint64_t limit = getMemorySize();
    if (limit > MAX_HEAP_SIZE)
        limit = MAX_HEAP_SIZE;
    // The heap is reserved upfront, it needs to fit in 32-bit address space
    if (sizeof(void *) == 4 && limit > MAX_HEAP_SIZE_32BIT)
        limit = MAX_HEAP_SIZE_32BIT;
    return (size_t)limit;
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

// The heap must fit in the write barrier's window.
size_t Heap_limitMaxHeapSize(size_t maxHeapSize) {
#ifdef SCALANATIVE_GC_BARRIER_WINDOW
    if (maxHeapSize > SCALANATIVE_GC_BARRIER_WINDOW)
        return SCALANATIVE_GC_BARRIER_WINDOW;
#endif
    return maxHeapSize;
}

// Bytes of address space, from the heap start, that the bytemap covers
size_t Heap_bytemapCoverage(size_t maxHeapSize) {
#ifdef SCALANATIVE_GC_BARRIER_WINDOW
    return SCALANATIVE_GC_BARRIER_WINDOW;
#else
    return maxHeapSize;
#endif
}

// Reserves the heap, aligned to the write barrier's window when it uses one
word_t *Heap_mapHeapSpace(size_t maxHeapSize) {
#ifdef SCALANATIVE_GC_BARRIER_WINDOW
    size_t window = SCALANATIVE_GC_BARRIER_WINDOW;
    word_t *reserved = memoryMap(maxHeapSize + window);
    if (reserved == NULL)
        return NULL;
    word_t start = ((word_t)reserved + window - 1) & ~(window - 1);
    // Release the unaligned head and the tail beyond the heap
    size_t head = start - (word_t)reserved;
    if (head > 0)
        memoryUnmap(reserved, head);
    // From a page boundary: the heap size need not be a multiple of the
    // page size, and munmap rejects other addresses.
    word_t page = getPageSize();
    word_t tailStart = (start + maxHeapSize + page - 1) & ~(page - 1);
    word_t tailEnd = (word_t)reserved + maxHeapSize + window;
    if (tailStart < tailEnd)
        memoryUnmap((void *)tailStart, tailEnd - tailStart);
    return (word_t *)start;
#else
    return Heap_mapAndAlign(maxHeapSize, BLOCK_TOTAL_SIZE);
#endif
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
