// White-box checks for scripted run/gc-immix-concurrent-allocation. This file
// emits no production code. Hooks run only while the collector owns the world.
#if defined(SCALANATIVE_GC_IMMIX) && defined(SCALANATIVE_GC_TESTING)

#include "State.h"
#include "Block.h"
#include "shared/MemoryMap.h"
#include "immix_commix/Synchronizer.h"
#include <stdlib.h>
#include <string.h>

static atomic_int requestedCheck;
static int checkResult;

static void unmapTestMemory(void *memory, size_t size) {
#ifdef _WIN32
    memoryUnmap(memory, 0); // MEM_RELEASE requires zero size.
#else
    memoryUnmap(memory, size);
#endif
}

static int checkRecycledCapacity(void) {
    const unsigned blocks = 8;
    size_t bytes = blocks * BLOCK_TOTAL_SIZE;
    word_t *storage = memoryMapOrExitOnError(bytes);
    BlockMeta *metas =
        (BlockMeta *)memoryMapOrExitOnError(blocks * sizeof(BlockMeta));
    size_t mapBytes = sizeof(Bytemap) + bytes / ALLOCATION_ALIGNMENT;
    Bytemap *map = (Bytemap *)memoryMapOrExitOnError(mapBytes);
    Bytemap_Init(map, storage, bytes);
    BlockAllocator pool;
    BlockAllocator_Init(&pool, (word_t *)metas, blocks);
    MutatorThread threads[2] = {0};
    MutatorThreadNode second = {.value = threads + 1, .next = NULL};
    MutatorThreadNode first = {.value = threads, .next = &second};
    MutatorThreads saved = atomic_exchange(&mutatorThreads, &first);
    int result = 1;
#define CHECK(condition)                                                       \
    do {                                                                       \
        if (!(condition)) {                                                    \
            result = 0;                                                        \
            goto cleanup;                                                      \
        }                                                                      \
    } while (0)
    for (unsigned i = 0; i < 2; i++) {
        Allocator_Init(&threads[i].allocator, &pool, map, (word_t *)metas,
                       storage);
        LargeAllocator_Init(&threads[i].largeAllocator, &pool, map,
                            (word_t *)metas, storage);
    }
    BlockAllocator_Clear(&pool);
    BlockAllocator_AddFreeBlocks(&pool, metas + 1, 3);
    BlockAllocator_SweepDone(&pool);
    CHECK(BlockAllocator_GetFreeSuperblock(&pool, 3) == metas + 1);
    CHECK(atomic_load(&pool.freeBlockCount) == 0);

    BlockAllocator_Clear(&pool);
    BlockAllocator_AddFreeBlocks(&pool, metas + 1, 2);
    BlockAllocator_SweepDone(&pool);
    CHECK(BlockAllocator_GetFreeBlockForSmallAllocation(&pool, false) == NULL);
    CHECK(atomic_load(&pool.freeBlockCount) == 2);
    CHECK(BlockAllocator_GetFreeBlock(&pool) != NULL);
    CHECK(atomic_load(&pool.freeBlockCount) == 1);
    BlockAllocator_Clear(&pool);

    // An idle allocator owns these runs. The middle block's explicit link to
    // block zero must remain distinct from the adjacent-block encoding.
    Allocator *source = &threads[0].allocator;
    Allocator *target = &threads[1].allocator;
    unsigned order[] = {2, 0, 3};
    for (unsigned i = 0; i < 3; i++) {
        unsigned index = order[i];
        BlockMeta_SetFlag(metas + index, block_simple);
        BlockMeta_SetFirstFreeLine(metas + index, 0);
        FreeLineMeta *hole = (FreeLineMeta *)(storage + index * WORDS_IN_BLOCK);
        hole->size = index == 0 ? 4 : 1;
        hole->next = LAST_HOLE;
        BlockList_AddLast(&source->recycledBlocks, metas + index);
    }
    atomic_store(&source->recycledBlockCount, 3);
    CHECK(Allocator_ReserveRecycled(target, 544));
    CHECK(target->largeCursor == storage);
    CHECK(target->largeLimit == storage + 4 * WORDS_IN_LINE);
    CHECK(Allocator_Reserve(target, 544));
    CHECK(source->recycledBlocks.first == metas + 2);
    CHECK(source->recycledBlocks.last == metas + 3);
    CHECK(Allocator_ReserveRecycled(target, 16));
    CHECK(target->cursor == storage + 2 * WORDS_IN_BLOCK);
    CHECK(Allocator_ReserveRecycled(target, 16));
    CHECK(target->cursor == storage + 3 * WORDS_IN_BLOCK);
    CHECK(source->recycledBlocks.first == NULL);
    CHECK(source->recycledBlocks.last == NULL);
    CHECK(atomic_load(&source->recycledBlockCount) == 0);

    // A medium reservation must also search later runs of its own block.
    target->largeCursor = target->largeLimit;
    target->block = metas + 1;
    target->blockStart = storage + WORDS_IN_BLOCK;
    BlockMeta_SetFlag(target->block, block_simple);
    BlockMeta_SetFirstFreeLine(target->block, 8);
    FreeLineMeta *hole =
        (FreeLineMeta *)(target->blockStart + 8 * WORDS_IN_LINE);
    hole->size = 4;
    hole->next = LAST_HOLE;
    CHECK(Allocator_ReserveRecycled(target, 544));
    CHECK(target->largeCursor == target->blockStart + 8 * WORDS_IN_LINE);

    Chunk *a = (Chunk *)storage;
    Chunk *b = (Chunk *)(storage + WORDS_IN_BLOCK);
    LargeAllocator_AddChunk(&threads[0].largeAllocator, a, MIN_BLOCK_SIZE);
    LargeAllocator_AddChunk(&threads[0].largeAllocator, b, MIN_BLOCK_SIZE);
    CHECK(LargeAllocator_ReserveRecycled(&threads[1].largeAllocator,
                                         MIN_BLOCK_SIZE));
    CHECK(threads[1].largeAllocator.reservedChunk == a);
    threads[1].largeAllocator.reservedChunk = NULL;
    CHECK(LargeAllocator_ReserveRecycled(&threads[1].largeAllocator,
                                         MIN_BLOCK_SIZE));
    CHECK(threads[1].largeAllocator.reservedChunk == b);
    threads[1].largeAllocator.reservedChunk = NULL;
    CHECK(!LargeAllocator_ReserveRecycled(&threads[1].largeAllocator,
                                          MIN_BLOCK_SIZE));
    // A callback can allocate a smaller object before an outer failed request
    // consumes its reservation. Never split a multi-block remainder into the
    // sub-block free lists.
    Chunk *pending = (Chunk *)(storage + 4 * WORDS_IN_BLOCK);
    pending->nothing = NULL;
    pending->size = 3 * BLOCK_TOTAL_SIZE;
    threads[1].largeAllocator.reservedChunk = pending;
    CHECK(LargeAllocator_tryAlloc(&threads[1].largeAllocator, MIN_BLOCK_SIZE) ==
          NULL);
    CHECK(threads[1].largeAllocator.reservedChunk == pending);
    BlockAllocator_AddFreeBlocks(&pool, metas + 2, 1);
    BlockAllocator_SweepDone(&pool);
    CHECK(LargeAllocator_tryAlloc(&threads[1].largeAllocator, MIN_BLOCK_SIZE) ==
          storage + 2 * WORDS_IN_BLOCK);
    CHECK(threads[1].largeAllocator.reservedChunk == pending);
    CHECK(LargeAllocator_tryAlloc(&threads[1].largeAllocator,
                                  3 * BLOCK_TOTAL_SIZE) == (word_t *)pending);
    CHECK(threads[1].largeAllocator.reservedChunk == NULL);
cleanup:
    atomic_store(&mutatorThreads, saved);
#ifdef _WIN32
    CloseHandle(pool.allocationLock);
#else
    pthread_mutex_destroy(&pool.allocationLock);
#endif
    unmapTestMemory(map, mapBytes);
    unmapTestMemory(metas, blocks * sizeof(BlockMeta));
    unmapTestMemory(storage, bytes);
    return result;
#undef CHECK
}

void scalanative_gc_test_after_sweep(void) {
    if (atomic_load(&requestedCheck) == 1) {
        atomic_store(&requestedCheck, 0);
        checkResult = checkRecycledCapacity();
    }
}

void scalanative_gc_test_after_reservation(uint32_t size, bool available) {
    if (size == 0 || atomic_load(&requestedCheck) != 2)
        return;
    atomic_store(&requestedCheck, 0);
    // Simulate competitors taking every remaining shared block after GC. The
    // requester's reserved capacity must still satisfy its retry without
    // growth.
    uint32_t count = atomic_load(&blockAllocator.freeBlockCount);
    BlockMeta **taken = malloc((size_t)(count + 1) * sizeof(BlockMeta *));
    if (taken == NULL) {
        checkResult = 0;
        return;
    }
    uint32_t used = 0;
    BlockMeta *block;
    while ((block = BlockAllocator_GetFreeBlock(&blockAllocator)) != NULL) {
        taken[used++] = block;
        BlockMeta_SetFlag(block, block_reserved);
        assert(used <= count);
    }
    size_t before = atomic_load(&heap.heapSize);
    checkResult =
        available &&
        (size < MIN_BLOCK_SIZE
             ? Allocator_Reserve(&currentMutatorThread->allocator, size)
             : LargeAllocator_Reserve(&currentMutatorThread->largeAllocator,
                                      size)) &&
        atomic_load(&heap.heapSize) == before;
    for (uint32_t i = 0; i < used; i++)
        BlockAllocator_AddFreeBlocks(&blockAllocator, taken[i], 1);
    BlockAllocator_SweepDone(&blockAllocator);
    checkResult &= atomic_load(&blockAllocator.freeBlockCount) == count;
    free(taken);
}

int scalanative_gc_test_recycled_capacity(void) {
    checkResult = 0;
    atomic_store(&requestedCheck, 1);
    Heap_Collect(&heap, &stack);
    return checkResult;
}

int scalanative_gc_test_reservation(void) {
    uint32_t sizes[] = {16, 528, 8224};
    for (unsigned i = 0; i < sizeof(sizes) / sizeof(sizes[0]); i++) {
        checkResult = 0;
        atomic_store(&requestedCheck, 2);
        if (!Heap_RecoverAllocation(&heap, sizes[i],
                                    atomic_load(&heap.collectionGeneration)) ||
            !checkResult)
            return 0;
    }
    return 1;
}

int scalanative_gc_test_stale_generation(void) {
    uint64_t before = atomic_load(&heap.collectionGeneration);
    Heap_Collect(&heap, &stack);
    uint64_t after = atomic_load(&heap.collectionGeneration);
    if (after <= before || !Heap_RecoverAllocation(&heap, 16, before) ||
        atomic_load(&heap.collectionGeneration) != after)
        return 0;
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    if (Synchronizer_acquireForAllocation(&heap.collectionGeneration, before)) {
        Synchronizer_release();
        return 0;
    }
#endif
    return 1;
}

#endif
