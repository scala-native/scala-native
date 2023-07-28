#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <memory.h>
#include "Zone.h"
#include "Util.h"
#include "MemoryPool.h"

MemoryPool *scalanative_zone_default_pool = NULL;
LargeMemoryPool *scalanative_zone_default_largepool = NULL;

void *scalanative_zone_open() {
    if (scalanative_zone_default_pool == NULL) {
        scalanative_zone_default_pool = MemoryPool_open();
    }
    if (scalanative_zone_default_largepool == NULL) {
        scalanative_zone_default_largepool = LargeMemoryPool_open();
    }
    Zone *zone = malloc(sizeof(Zone));
    zone->pool = scalanative_zone_default_pool;
    zone->page = NULL;
    zone->largePool = scalanative_zone_default_largepool;
    zone->largePage = NULL;
    return (void *)zone;
}

void scalanative_zone_close(void *_zone) {
    Zone *zone = (Zone *)_zone;
    // Reclaim borrowed pages to the memory pool.
    MemoryPool_reclaim(zone->pool, zone->page);
    LargeMemoryPool_reclaim(zone->largePool, zone->largePage);
    free(zone);
}

MemoryPage *scalanative_zone_claim(Zone *zone, size_t size) {
    return (size <= MEMORYPOOL_PAGE_SIZE)
               ? MemoryPool_claim(zone->pool)
               : LargeMemoryPool_claim(zone->largePool, Util_pad(size, 8));
}

void *scalanative_zone_alloc(void *_zone, void *info, size_t size) {
    Zone *zone = (Zone *)_zone;
    MemoryPage *page =
        (size <= MEMORYPOOL_PAGE_SIZE) ? zone->page : zone->largePage;
    page = (page == NULL) ? scalanative_zone_claim(zone, size) : page;
    size_t paddedOffset = Util_pad(page->offset, 8);
    size_t resOffset = 0;
    if (paddedOffset + size <= page->size) {
        resOffset = paddedOffset;
    } else {
        MemoryPage *newPage = scalanative_zone_claim(zone, size);
        newPage->next = page;
        page = newPage;
        resOffset = 0;
    }
    page->offset = resOffset + size;
    void *current = (void *)(page->start + resOffset);
    memset(current, 0, size);
    void **alloc = (void **)current;
    *alloc = info;
    if (size <= MEMORYPOOL_PAGE_SIZE) {
        zone->page = page;
    } else {
        zone->largePage = page;
    }
    return (void *)alloc;
}
