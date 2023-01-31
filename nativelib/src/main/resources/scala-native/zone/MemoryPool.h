#ifndef MEMORY_POOL_H
#define MEMORY_POOL_H

#include <stddef.h>
#include "../gc/shared/GCTypes.h"

typedef struct _MemoryPage {
    void *start;
    size_t offset;
    size_t size;
    struct _MemoryPage *next;
} MemoryPage;

typedef struct _MemoryChunk {
    void *start;
    size_t offset;
    size_t size;
    struct _MemoryChunk *next;
} MemoryChunk;

typedef struct _MemoryPool {
    size_t chunkSize;
    MemoryChunk *chunk;
    MemoryPage *page;
} MemoryPool;

#define MEMORYPOOL_PAGE_SIZE 8192
#define MEMORYPOOL_MIN_CHUNK_SIZE 4 * MEMORYPOOL_PAGE_SIZE
#define MEMORYPOOL_MAX_CHUNK_SIZE 512 * MEMORYPOOL_PAGE_SIZE

MemoryPool *memorypool_open();

MemoryPage *memorypool_claim(void *pool);

MemoryPage *memorypool_claim_with_min_size(void *pool, size_t min_size);
void memorypool_free(void *pool);

void memorypool_reclaim(void *pool, void *head_page, void *tail_page);

#endif // MEMORY_POOL_H