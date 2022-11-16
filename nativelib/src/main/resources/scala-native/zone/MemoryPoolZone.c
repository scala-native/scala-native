#include <stdio.h>
#include <stdlib.h>
#include <memory.h>
#include "MemoryPoolZone.h"

MemoryPoolZone *memorypoolzone_open(void *_pool) {
    MemoryPoolZone *zone = malloc(sizeof(MemoryPoolZone));
    zone->pool = (MemoryPool *)_pool;
    zone->tail_page = memorypool_claim(zone->pool);
    zone->head_page = zone->tail_page;
    return zone;
}

bool memorypoolzone_isopen(void *_zone) {
    MemoryPoolZone *zone = (MemoryPoolZone *)_zone;
    return (zone->head_page) != NULL;
}

bool memorypoolzone_isclosed(void *_zone) {
    return !memorypoolzone_isopen(_zone);
}

void exitWithZoneIsClosed() {
    fprintf(stderr, "Zone is already closed.\n");
    exit(1);
}

void memorypoolzone_checkopen(void *_zone) {
    if (!memorypoolzone_isopen(_zone)) {
        exitWithZoneIsClosed();
    }
}

void memorypoolzone_close(void *_zone) {
    memorypoolzone_checkopen(_zone);
    MemoryPoolZone *zone = (MemoryPoolZone *)_zone;
    // Reclaim borrowed pages to the memory pool.
    memorypool_reclaim(zone->pool, zone->head_page, zone->tail_page);
    zone->head_page = NULL;
    zone->tail_page = NULL;
}

void exitWithZoneIsOpen() {
    fprintf(stderr, "Zone is still open.\n");
    exit(1);
}

void memorypoolzone_checkclose(void *_zone) {
    if (memorypoolzone_isopen(_zone)) {
        exitWithZoneIsOpen();
    }
}

void memorypoolzone_free(void *_zone) {
    memorypoolzone_checkclose(_zone);
    free((MemoryPoolZone *)_zone);
}

size_t pad(size_t addr, size_t alignment) {
    size_t alignment_mask = alignment - 1;
    size_t padding = ((addr & alignment_mask) == 0)
                         ? 0
                         : (alignment - (addr & alignment_mask));
    return addr + padding;
}

void *memorypoolzone_alloc(void *_zone, void *info, size_t size) {
    memorypoolzone_checkopen(_zone);
    MemoryPoolZone *zone = (MemoryPoolZone *)_zone;
    size_t current_offset = zone->head_page->offset;
    size_t padded_offset = pad(current_offset, 8);
    size_t res_offset = 0;
    if (padded_offset + size <= MEMORYPOOL_PAGE_SIZE) {
        res_offset = padded_offset;
    } else {
        MemoryPage *newpage = memorypool_claim(zone->pool);
        newpage->next = zone->head_page;
        zone->head_page = newpage;
        res_offset = 0;
    }
    zone->head_page->offset = res_offset + size;
    void **alloc = (void **)(zone->head_page->start + res_offset);
    *alloc = info;
    return alloc;
}
