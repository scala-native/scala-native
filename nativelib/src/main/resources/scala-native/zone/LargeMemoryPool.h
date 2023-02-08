#ifndef LARGE_MEMORY_POOL_H
#define LARGE_MEMORY_POOL_H

#include "MemoryPage.h"

typedef struct _LargeMemoryPool {
    MemoryPage *page;
} LargeMemoryPool;

/**
 * @brief Open an empry large memory pool. A large memory pool consists of a
 * linked list of pages. For each page, its size depends on the size of object
 * to be allocated in this pool.
 *
 * @return LargeMemoryPool* The handle of the new memory pool.
 */
LargeMemoryPool *LargeMemoryPool_open();

/** Borrow a single unused page, to be reclaimed later.
 *
 * @param pool The handle of the pool to borrow from.
 * @param size The minimum size of the page to be claimed.
 * @return MemoryPage* A memory page.
 */
MemoryPage *LargeMemoryPool_claim(LargeMemoryPool *largePool, size_t size);

/** See `MemoryPool_reclaim`. */
void LargeMemoryPool_reclaim(LargeMemoryPool *largePool, MemoryPage *head);

/** See `MemoryPool_close`. */
void LargeMemoryPool_close(LargeMemoryPool *largePool);

#endif // LARGE_MEMORY_POOL_H