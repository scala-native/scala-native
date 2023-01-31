#include <stdio.h>
#include <stdlib.h>
#include <memory.h>
#include "MemoryPool.h"
#include "../gc/shared/GCScalaNative.h"
#include "../gc/shared/MemoryMap.h"

#ifdef _WIN32
#include <windows.h>
#else // Unix
#include <sys/mman.h>
#endif

void memorypool_alloc_chunk(MemoryPool *pool, size_t size);
void memorypool_alloc_page(MemoryPool *pool, size_t size);

size_t max(size_t a, size_t b) { return a > b ? a : b; }

MemoryPool *memorypool_open() {
    MemoryPool *pool = malloc(sizeof(MemoryPool));
    pool->chunkSize = MEMORYPOOL_MIN_CHUNK_SIZE;
    pool->chunk = NULL;
    pool->page = NULL;
    memorypool_alloc_chunk(pool, MEMORYPOOL_MIN_CHUNK_SIZE);
    return pool;
}

void memorypool_alloc_chunk(MemoryPool *pool, size_t size) {
    if (pool->chunkSize < MEMORYPOOL_MAX_CHUNK_SIZE) {
        pool->chunkSize *= 2;
    }
    MemoryChunk *chunk = malloc(sizeof(MemoryChunk));
    chunk->size = max(pool->chunkSize, size);
    chunk->offset = 0;
    chunk->start = memoryMap(chunk->size);
#ifdef _WIN32
#else // Unix
    if (chunk->start == MAP_FAILED) {
        fprintf(stderr, "Failed to allocate memory chunk of size %zu\n",
                chunk->size);
    }
#endif
    chunk->next = pool->chunk;
    pool->chunk = chunk;
}

void memorypool_alloc_page(MemoryPool *pool, size_t size) {
    if (pool->chunk->offset >= pool->chunk->size ||
        pool->chunk->offset + size > pool->chunk->size) {
        memorypool_alloc_chunk(pool, max(pool->chunkSize, size));
    }
    MemoryPage *page = malloc(sizeof(MemoryPage));
    page->start = pool->chunk->start + pool->chunk->offset;
    page->offset = 0;
    page->size = size;
    page->next = pool->page;
    pool->chunk->offset += page->size;
    pool->page = page;
}

MemoryPage *memorypool_claim(void *pool) {
    return memorypool_claim_with_min_size(pool, 0);
}

/** Borrow a single unused page, to be reclaimed later. */
MemoryPage *memorypool_claim_with_min_size(void *_pool, size_t min_size) {
    MemoryPool *pool = (MemoryPool *)_pool;
    size_t page_size = MEMORYPOOL_PAGE_SIZE;
    if (min_size > MEMORYPOOL_MAX_CHUNK_SIZE) {
        fprintf(stderr, "Requested size is too large: %zu\n", min_size);
        return NULL;
    }
    while (min_size > page_size) {
        page_size *= 2;
        if (page_size > MEMORYPOOL_MAX_CHUNK_SIZE) {
            page_size = MEMORYPOOL_MAX_CHUNK_SIZE;
        }
    }
    if (pool->page == NULL) {
        memorypool_alloc_page(pool, page_size);
    }
    MemoryPage *result = pool->page;
    pool->page = result->next;
    result->next = NULL;
    result->offset = 0;
    // Notify the GC that the page is in use.
    scalanative_add_roots(result->start, result->start + result->size);
    return result;
}

/** Reclaimed a list of previously borrowed pages. */
void memorypool_reclaim(void *_pool, void *_head_page, void *_tail_page) {
    MemoryPool *pool = (MemoryPool *)_pool;
    MemoryPage *head = (MemoryPage *)_head_page;
    MemoryPage *tail = (MemoryPage *)_tail_page;
    // Notify the GC that the pages are no longer in use.
    MemoryPage *page = head;
    while (page != NULL) {
        scalanative_remove_roots(page->start, page->start + page->size);
        if (page == tail)
            break;
        page = page->next;
    }
    // Append the reclaimed pages to the pool.
    tail->next = pool->page;
    pool->page = head;
}

void memorypool_free(void *_pool) {
    MemoryPool *pool = (MemoryPool *)_pool;
    // Free chunks.
    MemoryChunk *chunk = pool->chunk, *pre_chunk = NULL;
    while (chunk != NULL) {
        pre_chunk = chunk;
        chunk = chunk->next;
        memoryUnmap(pre_chunk->start, pre_chunk->size);
        free(pre_chunk);
    }
    // Free pages.
    MemoryPage *page = pool->page, *pre_page = NULL;
    while (page != NULL) {
        pre_page = page;
        page = page->next;
        free(pre_page);
    }
    // Free the pool.
    free(pool);
}