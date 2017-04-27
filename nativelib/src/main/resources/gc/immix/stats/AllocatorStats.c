#include <stdio.h>
#include <stdlib.h>
#include "AllocatorStats.h"

AllocatorStats* allocatorStats_create() {
    AllocatorStats* stats = malloc(sizeof(AllocatorStats));
    allocatorStats_reset(stats);
    return stats;
}

void allocatorStats_reset(AllocatorStats* stats) {
    stats->blockCount = 0;
    stats->unavailableBlockCount = 0;
    stats->availableBlockCount = 0;
    stats->recyclableBlockCount = 0;
    stats->totalBytesAllocated = 0;
    stats->bytesAllocated = 0;
    stats->liveObjectCount = 0;
    stats->totalAllocatedObjectCount = 0;
}

void allocatorStats_resetBlockDistribution(AllocatorStats* stats) {
    stats->unavailableBlockCount = 0;
    stats->availableBlockCount = 0;
    stats->recyclableBlockCount = 0;
}

void allocatorStats_print(AllocatorStats* stats) {
    printf("############\n");
    printf("Unavailable block count: %llu/%llu\n", stats->unavailableBlockCount, stats->blockCount);
    printf("Free block count: %llu/%llu\n", stats->availableBlockCount, stats->blockCount);
    printf("Recyclable block count: %llu/%llu\n", stats->recyclableBlockCount, stats->blockCount);
    printf("Total bytes allocated: %llu\n", stats->totalBytesAllocated);
    printf("Bytes allocated since last collect: %llu\n", stats->bytesAllocated);
    printf("Live object count: %llu\n", stats->liveObjectCount);
    printf("Total objects allocated: %llu\n", stats->totalAllocatedObjectCount);
    printf("############\n");
    fflush(stdout);
}