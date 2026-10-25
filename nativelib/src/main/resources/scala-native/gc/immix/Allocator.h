#ifndef IMMIX_ALLOCATOR_H
#define IMMIX_ALLOCATOR_H

#include "shared/GCTypes.h"
#include "datastructures/BlockList.h"
#include "datastructures/Bytemap.h"
#include "BlockAllocator.h"
#include "Heap.h"
#include <stdatomic.h>
#include <assert.h>

typedef struct {
    // The fields here are sorted by how often it is accessed.
    // This should improve cache performance.
    // frequently used by Heap_AllocSmall
    // this is on the fast path
    Bytemap *bytemap;
    word_t *cursor;
    word_t *limit;

    // additional things used for Allocator_getNextLine
    BlockMeta *block;
    word_t *blockStart;
    // additional things used for Allocator_overflowAllocation
    word_t *largeCursor;
    word_t *largeLimit;
    // additional things used for Allocator_newBlock
    BlockList recycledBlocks;
    word_t *blockMetaStart;
    word_t *heapStart;
    BlockAllocator *blockAllocator;
    // additional things used for
    BlockMeta *largeBlock;
    word_t *largeBlockStart;
    // This gets concurrently updated by other threads, keep if it as far away
    // as possible from fast path.
    atomic_uint_fast32_t recycledBlockCount;
    bool canUseReserve;
} Allocator;

void Allocator_Init(Allocator *allocator, BlockAllocator *blockAllocator,
                    Bytemap *bytemap, word_t *blockMetaStart,
                    word_t *heapStart);
void Allocator_Clear(Allocator *allocator);
bool Allocator_Reserve(Allocator *allocator, size_t size);
// Only after a failed reservation, while all mutators are stopped.
bool Allocator_ReserveRecycled(Allocator *allocator, size_t size);
NOINLINE word_t *Allocator_allocSlow(Allocator *allocator, Heap *heap,
                                     uint32_t size);

word_t *Allocator_Alloc(Heap *heap, uint32_t size);

// Keep the dedicated small-object entry point inlineable without LTO.
static INLINE word_t *Allocator_AllocInline(Allocator *allocator, Heap *heap,
                                            uint32_t size) {
    assert(size % ALLOCATION_ALIGNMENT == 0);
    assert(size < MIN_BLOCK_SIZE);

    word_t *start = allocator->cursor;
    word_t *end = (word_t *)((uint8_t *)start + size);

    // Checks if the end of the block overlaps with the limit
    if (start == NULL || end > allocator->limit) {
        return Allocator_allocSlow(allocator, heap, size);
    }

    allocator->cursor = end;

    memset(start, 0, size);

    word_t *object = start;
    ObjectMeta *objectMeta = Bytemap_Get(heap->bytemap, object);
#ifdef GC_ASSERTIONS
    ObjectMeta_AssertIsValidAllocation(objectMeta, size);
#endif
    ObjectMeta_SetAllocated(objectMeta);

    // prefetch starting from 36 words away from the object start
    // rw = 0 => prefetch for reading
    // locality = 3 => data has high locality, leave the values in as many
    // caches as possible
    __builtin_prefetch(object + 36, 0, 3);

    assert(Heap_IsWordInHeap(heap, object));
    return object;
}

#endif // IMMIX_ALLOCATOR_H
