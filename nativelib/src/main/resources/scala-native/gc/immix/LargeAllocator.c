#if defined(SCALANATIVE_GC_IMMIX)

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "LargeAllocator.h"
#include "immix_commix/utils/MathUtils.h"
#include "Object.h"
#include "immix/State.h"
#include "immix_commix/headers/ObjectHeader.h"

inline static int LargeAllocator_sizeToLinkedListIndex(size_t size) {
    assert(size >= MIN_BLOCK_SIZE);
    assert(size % MIN_BLOCK_SIZE == 0);
    int index = size / MIN_BLOCK_SIZE - 1;
    assert(index < FREE_LIST_COUNT);
    return index;
}

Chunk *LargeAllocator_chunkAddOffset(Chunk *chunk, size_t words) {
    return (Chunk *)((ubyte_t *)chunk + words);
}

void LargeAllocator_freeListAddBlockLast(FreeList *freeList, Chunk *chunk) {
    if (freeList->first == NULL) {
        freeList->first = chunk;
    } else {
        freeList->last->next = chunk;
    }
    freeList->last = chunk;
    chunk->next = NULL;
}

Chunk *LargeAllocator_freeListRemoveFirstBlock(FreeList *freeList) {
    if (freeList->first == NULL) {
        return NULL;
    }
    Chunk *chunk = freeList->first;
    if (freeList->first == freeList->last) {
        freeList->last = NULL;
    }

    freeList->first = chunk->next;
    return chunk;
}

void LargeAllocator_freeListInit(FreeList *freeList) {
    freeList->first = NULL;
    freeList->last = NULL;
}

void LargeAllocator_Init(LargeAllocator *allocator,
                         BlockAllocator *blockAllocator, Bytemap *bytemap,
                         word_t *blockMetaStart, word_t *heapStart) {
    allocator->reservedChunk = NULL;
    allocator->heapStart = heapStart;
    allocator->blockMetaStart = blockMetaStart;
    allocator->bytemap = bytemap;
    allocator->blockAllocator = blockAllocator;

    for (int i = 0; i < FREE_LIST_COUNT; i++) {
        LargeAllocator_freeListInit(&allocator->freeLists[i]);
    }
}

void LargeAllocator_AddChunk(LargeAllocator *allocator, Chunk *chunk,
                             size_t total_block_size) {
    assert(total_block_size >= MIN_BLOCK_SIZE);
    assert(total_block_size < BLOCK_TOTAL_SIZE);
    assert(total_block_size % MIN_BLOCK_SIZE == 0);

    int listIndex = LargeAllocator_sizeToLinkedListIndex(total_block_size);
    chunk->nothing = NULL;
    chunk->size = total_block_size;
    ObjectMeta *chunkMeta = Bytemap_Get(allocator->bytemap, (word_t *)chunk);
    ObjectMeta_SetPlaceholder(chunkMeta);

    LargeAllocator_freeListAddBlockLast(&allocator->freeLists[listIndex],
                                        chunk);
}

static inline Chunk *LargeAllocator_getChunkForSize(LargeAllocator *allocator,
                                                    size_t requiredChunkSize) {
    for (int listIndex =
             LargeAllocator_sizeToLinkedListIndex(requiredChunkSize);
         listIndex < FREE_LIST_COUNT; listIndex++) {
        Chunk *chunk = allocator->freeLists[listIndex].first;
        if (chunk != NULL) {
            LargeAllocator_freeListRemoveFirstBlock(
                &allocator->freeLists[listIndex]);
            return chunk;
        }
    }
    return NULL;
}

static Chunk *LargeAllocator_getChunk(LargeAllocator *allocator,
                                      size_t requestedBlockSize) {
    size_t actualBlockSize =
        MathUtils_RoundToNextMultiple(requestedBlockSize, MIN_BLOCK_SIZE);

    Chunk *chunk = NULL;
    if (actualBlockSize < BLOCK_TOTAL_SIZE) {
        // only need to look in free lists for chunks smaller than a block
        chunk = LargeAllocator_getChunkForSize(allocator, actualBlockSize);
    }

    if (chunk == NULL) {
        uint32_t superblockSize = (uint32_t)MathUtils_DivAndRoundUp(
            actualBlockSize, BLOCK_TOTAL_SIZE);
        BlockMeta *superblock = BlockAllocator_GetFreeSuperblock(
            allocator->blockAllocator, superblockSize);
        if (superblock != NULL) {
            chunk = (Chunk *)BlockMeta_GetBlockStart(
                allocator->blockMetaStart, allocator->heapStart, superblock);
            chunk->nothing = NULL;
            chunk->size = superblockSize * BLOCK_TOTAL_SIZE;
        }
    }

    return chunk;
}

bool LargeAllocator_ReserveRecycled(LargeAllocator *allocator, size_t size) {
    size_t actualSize = MathUtils_RoundToNextMultiple(size, MIN_BLOCK_SIZE);
    if (actualSize >= BLOCK_TOTAL_SIZE)
        return false;
    MutatorThreads_foreach(mutatorThreads, node) {
        Chunk *chunk = LargeAllocator_getChunkForSize(
            &node->value->largeAllocator, actualSize);
        if (chunk != NULL) {
            ObjectMeta_SetPlaceholder(
                Bytemap_Get(allocator->bytemap, (word_t *)chunk));
            allocator->reservedChunk = chunk;
            return true;
        }
    }
    return false;
}

bool LargeAllocator_Reserve(LargeAllocator *allocator, size_t size) {
    if (allocator->reservedChunk != NULL)
        return allocator->reservedChunk->size >=
               MathUtils_RoundToNextMultiple(size, MIN_BLOCK_SIZE);
    Chunk *chunk = LargeAllocator_getChunk(allocator, size);
    if (chunk == NULL)
        return false;
    // A weak-reference callback can collect again before the caller consumes
    // this reservation. Keep it recognizable as free storage during sweep.
    ObjectMeta_SetPlaceholder(Bytemap_Get(allocator->bytemap, (word_t *)chunk));
    allocator->reservedChunk = chunk;
    return true;
}

word_t *LargeAllocator_tryAlloc(LargeAllocator *allocator, size_t size) {
    size_t actualBlockSize =
        MathUtils_RoundToNextMultiple(size, MIN_BLOCK_SIZE);
    Chunk *chunk = allocator->reservedChunk;
    if (chunk != NULL && chunk->size >= actualBlockSize)
        allocator->reservedChunk = NULL;
    else
        chunk = LargeAllocator_getChunk(allocator, size);
    if (chunk == NULL)
        return NULL;

    size_t chunkSize = chunk->size;
    assert(chunkSize >= MIN_BLOCK_SIZE);

    if (chunkSize - MIN_BLOCK_SIZE >= actualBlockSize) {
        Chunk *remainingChunk =
            LargeAllocator_chunkAddOffset(chunk, actualBlockSize);

        size_t remainingChunkSize = chunkSize - actualBlockSize;
        LargeAllocator_AddChunk(allocator, remainingChunk, remainingChunkSize);
    }

    ObjectMeta *objectMeta = Bytemap_Get(allocator->bytemap, (word_t *)chunk);
#ifdef GC_ASSERTIONS
    ObjectMeta_AssertIsValidAllocation(objectMeta, actualBlockSize);
#endif
    ObjectMeta_SetAllocated(objectMeta);
    word_t *object = (word_t *)chunk;
    memset(object, 0, actualBlockSize);
    return object;
}

void LargeAllocator_Clear(LargeAllocator *allocator) {
    allocator->reservedChunk = NULL;
    for (int i = 0; i < FREE_LIST_COUNT; i++) {
        LargeAllocator_freeListInit(&allocator->freeLists[i]);
    }
}

void LargeAllocator_Sweep(LargeAllocator *allocator, BlockMeta *blockMeta,
                          word_t *blockStart) {
    // Objects that are larger than a block
    // are always allocated at the begining the smallest possible superblock.
    // Any gaps at the end can be filled with large objects, that are smaller
    // than a block. This means that objects can ONLY start at the begining at
    // the first block or anywhere at the last block, except the begining.
    // Therefore we only need to look at a few locations.
    uint32_t superblockSize = BlockMeta_SuperblockSize(blockMeta);
    ObjectMeta *firstObjectMeta = Bytemap_Get(heap.bytemap, blockStart);

    word_t *blockEnd = blockStart + WORDS_IN_BLOCK * superblockSize;

    assert(!ObjectMeta_IsFree(firstObjectMeta));
    BlockMeta *lastBlock = blockMeta + superblockSize - 1;
    if (superblockSize > 1 && !ObjectMeta_IsMarked(firstObjectMeta)) {
        // release free superblock starting from the first object
        BlockAllocator_AddFreeBlocks(&blockAllocator, blockMeta,
                                     superblockSize - 1);

        BlockMeta_SetFlag(lastBlock, block_superblock_start);
        BlockMeta_SetSuperblockSize(lastBlock, 1);
    }

    word_t *lastBlockStart = blockEnd - WORDS_IN_BLOCK;
    word_t *chunkStart = NULL;

    // the tail end of the first object
    if (!ObjectMeta_IsMarked(firstObjectMeta)) {
        chunkStart = lastBlockStart;
    }
    ObjectMeta_Sweep(firstObjectMeta);

    word_t *current = lastBlockStart + (MIN_BLOCK_SIZE / WORD_SIZE);
    ObjectMeta *currentMeta = Bytemap_Get(heap.bytemap, current);
    while (current < blockEnd) {
        if (chunkStart == NULL) {
            if (ObjectMeta_IsAllocated(currentMeta) ||
                ObjectMeta_IsPlaceholder(currentMeta)) {
                chunkStart = current;
            }
        } else {
            if (ObjectMeta_IsMarked(currentMeta)) {
                size_t currentSize = (current - chunkStart) * WORD_SIZE;
                LargeAllocator_AddChunk(allocator, (Chunk *)chunkStart,
                                        currentSize);
                chunkStart = NULL;
            }
        }
        ObjectMeta_Sweep(currentMeta);

        current += MIN_BLOCK_SIZE / WORD_SIZE;
        currentMeta += MIN_BLOCK_SIZE / ALLOCATION_ALIGNMENT;
    }
    if (chunkStart == lastBlockStart) {
        // free chunk covers the entire last block, released it to the block
        // allocator
        BlockAllocator_AddFreeBlocks(&blockAllocator, lastBlock, 1);
    } else if (chunkStart != NULL) {
        size_t currentSize = (current - chunkStart) * WORD_SIZE;
        LargeAllocator_AddChunk(allocator, (Chunk *)chunkStart, currentSize);
    }
}

word_t *LargeAllocator_Alloc(Heap *heap, uint32_t size) {
    assert(size % ALLOCATION_ALIGNMENT == 0);
    assert(size >= MIN_BLOCK_SIZE);
    LargeAllocator *largeAllocator = &currentMutatorThread->largeAllocator;
    do {
        uint64_t generation = atomic_load_explicit(&heap->collectionGeneration,
                                                   memory_order_acquire);
        word_t *object = LargeAllocator_tryAlloc(largeAllocator, size);
        if (object != NULL) {
            assert(Heap_IsWordInHeap(heap, object));
            return object;
        }
        if (!Heap_RecoverAllocation(heap, size, generation))
            Heap_ThrowOutOfMemory(heap);
    } while (true);
    return NULL; // unreachable
}

#endif
