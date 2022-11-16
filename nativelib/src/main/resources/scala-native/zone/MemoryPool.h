#ifndef MEMORY_POOL_H
#define MEMORY_POOL_H

#include <stddef.h>
#include "../gc/shared/GCTypes.h"

typedef struct _MemoryPage {
    word_t *start;
    size_t offset;
    struct _MemoryPage *next;
} MemoryPage;

typedef struct _MemoryChunk {
    word_t *start;
    size_t offset;
    size_t size;
    struct _MemoryChunk *next;
} MemoryChunk;

typedef struct _MemoryPool {
    size_t chunkPageCount;
    MemoryChunk *chunk;
    MemoryPage *page;
} MemoryPool;

#define MEMORYPOOL_PAGE_SIZE 4096
#define MEMORYPOOL_MIN_PAGE_COUNT 4
#define MEMORYPOOL_MAX_PAGE_COUNT 256

MemoryPool *memorypool_open();

MemoryPage *memorypool_claim(void *pool);

void memorypool_free(void *pool);

void memorypool_reclaim(void *pool, void *head_page, void *tail_page);

#endif // MEMORY_POOL_H