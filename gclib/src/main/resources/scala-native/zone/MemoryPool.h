#ifndef MEMORY_POOL_H
#define MEMORY_POOL_H

#include <stddef.h>
#include "MemoryPage.h"
#include "../gc/shared/GCTypes.h"

typedef struct _MemoryChunk {
    void *start;
    size_t offset;
    size_t size;
    struct _MemoryChunk *next;
} MemoryChunk;

typedef struct _MemoryPool {
    size_t chunkPageCount;
    MemoryChunk *chunk;
    MemoryPage *page;
} MemoryPool;

#define MEMORYPOOL_PAGE_SIZE 8192
#define MEMORYPOOL_MIN_CHUNK_COUNT 4
#define MEMORYPOOL_MAX_CHUNK_COUNT 512

/**
 * @brief Open an empry memory pool. A memory pool consists of a linked list
 * of chunks. Each chunk is divided into fixed-size pages.
 *
 * @return MemoryPool* The handle of the new memory pool.
 */
MemoryPool *MemoryPool_open();

/** Borrow a single unused page, to be reclaimed later.
 *
 * @param pool The handle of the pool to borrow from.
 * @return MemoryPage* A memory page.
 */
MemoryPage *MemoryPool_claim(MemoryPool *pool);

/**
 * @brief Reclaimed a list of previously borrowed pages.
 *
 * @param pool The handle of the pool to reclaim to.
 * @param head The head of the list of pages to be reclaimed.
 */
void MemoryPool_reclaim(MemoryPool *pool, MemoryPage *head);

/**
 * @brief Free all memory managed by the pool. After closing the pool, the pool
 * handle is no longer valid to visit.
 *
 * @param pool The handle of the pool to be closed.
 */
void MemoryPool_close(MemoryPool *pool);

#endif // MEMORY_POOL_H
