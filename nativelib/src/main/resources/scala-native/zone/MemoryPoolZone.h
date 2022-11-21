#ifndef MEMORY_POOL_ZONE_H
#define MEMORY_POOL_ZONE_H

#include "MemoryPool.h"
#include <stdbool.h>

typedef struct _MemoryPoolZone {
    MemoryPool *pool;
    MemoryPage *head_page;
    MemoryPage *tail_page;
} MemoryPoolZone;

MemoryPoolZone *memorypoolzone_open(void *pool);

void *memorypoolzone_alloc(void *zone, void *info, size_t size);

void memorypoolzone_close(void *zone);

void memorypoolzone_free(void *zone);

#endif // MEMORYPOLL_ZONE_H