#ifndef ZONE_H
#define ZONE_H

#include <stdbool.h>
#include "MemoryPool.h"
#include "LargeMemoryPool.h"

typedef struct _Zone {
    MemoryPool *pool;
    LargeMemoryPool *largePool;
    MemoryPage *page;
    MemoryPage *largePage;
} Zone;

/**
 * @brief Open a new zone. The memory managed by the zone will be claimed from
 * the default memory pool.
 *
 * @return void* The handle of the new zone.
 */
void *scalanative_zone_open();

/**
 * @brief Allocate data in a zone.
 *
 * @param zone The handle of the zone to be allocated from.
 * @param info A pointer to data to be allocated.
 * @param size The size of the data.
 * @return void* The pointer to the allocated data.
 */
void *scalanative_zone_alloc(void *zone, void *info, size_t size);

/**
 * @brief Given a zone handle, reclaim all pages of this zone back to the memory
 * pool and free the zone. After this, the zone handle is no longer valid to
 * visit.
 *
 * @param zone The handle of zone to be closed.
 */
void scalanative_zone_close(void *zone);

// void scalanative_memorypoolzone_free(void *zone);

#endif // ZONE_H
