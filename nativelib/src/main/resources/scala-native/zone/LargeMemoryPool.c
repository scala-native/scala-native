#include <stdio.h>
#include <stdlib.h>
#include "LargeMemoryPool.h"
#include "../gc/shared/GCScalaNative.h"
#include "../gc/shared/MemoryMap.h"
#include "Util.h"

LargeMemoryPool *LargeMemoryPool_open() {
    LargeMemoryPool *largePool = malloc(sizeof(LargeMemoryPool));
    largePool->page = NULL;
    return largePool;
}

void LargeMemoryPool_alloc_page(LargeMemoryPool *largePool, size_t size) {
    MemoryPage *page = malloc(sizeof(MemoryPage));
    page->start = memoryMapOrExitOnError(size);
    page->offset = 0;
    page->size = size;
    page->next = largePool->page;
    largePool->page = page;
}

MemoryPage *LargeMemoryPool_claim(LargeMemoryPool *largePool, size_t size) {
    MemoryPage *result = NULL;
    if (largePool->page == NULL) {
        LargeMemoryPool_alloc_page(largePool, size);
        result = largePool->page;
    } else if (largePool->page->size < size) {
        // Find the first page that is large enough.
        MemoryPage *page = largePool->page, *prePage = NULL;
        while (page != NULL) {
            if (page->size >= size) {
                result = page;
                break;
            }
            prePage = page;
            page = page->next;
        }
        if (result != NULL) {
            // Move the large enough page to head.
            prePage->next = result->next;
            result->next = largePool->page;
        } else {
            // Allocate a new large enough page.
            LargeMemoryPool_alloc_page(largePool, size);
            result = largePool->page;
        }
    } else {
        // Use the first page.
        result = largePool->page;
    }
    largePool->page = result->next;
    result->next = NULL;
    result->offset = 0;
    // Notify the GC that the page is in use.
    scalanative_add_roots(result->start, result->start + result->size);
    return result;
}

void LargeMemoryPool_reclaim(LargeMemoryPool *largePool, MemoryPage *head) {
    // Notify the GC that the pages are no longer in use.
    MemoryPage *page = head, *tail = NULL;
    while (page != NULL) {
        scalanative_remove_roots(page->start, page->start + page->size);
        tail = page;
        page = page->next;
    }
    // Append the reclaimed pages to the pool.
    if (tail != NULL) {
        tail->next = largePool->page;
        largePool->page = head;
    }
}

void LargeMemoryPool_close(LargeMemoryPool *largePool) {
    // Free pages.
    MemoryPage *page = largePool->page, *prePage = NULL;
    while (page != NULL) {
        prePage = page;
        page = page->next;
        memoryUnmapOrExitOnError(prePage->start, prePage->size);
        free(prePage);
    }
    // Free the pool.
    free(largePool);
}
