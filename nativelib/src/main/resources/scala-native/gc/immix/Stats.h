#ifndef IMMIX_STATS_H
#define IMMIX_STATS_H

#include "Constants.h"
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <time.h>

typedef struct {
    FILE *outFile;
    uint64_t collections;
    uint64_t mark_time_ns[STATS_MEASUREMENTS];
    uint64_t nullify_time_ns[STATS_MEASUREMENTS];
    uint64_t sweep_time_ns[STATS_MEASUREMENTS];
    // From the collection request until all other mutators are stopped.
    uint64_t sync_time_ns[STATS_MEASUREMENTS];
    uint8_t kind[STATS_MEASUREMENTS];
} Stats;

// What a collection traced. Without Sticky Immix every collection is full.
typedef enum {
    STATS_COLLECTION_FULL = 0,
    STATS_COLLECTION_MINOR = 1,
    // A minor collection that could not free enough and collected the whole
    // heap as well; the times cover both.
    STATS_COLLECTION_MINOR_THEN_FULL = 2,
} StatsCollectionKind;

// False if the file could not be opened, the stats must not be used then.
bool Stats_Init(Stats *stats, const char *statsFile);
void Stats_RecordCollection(Stats *stats, uint64_t request_ns,
                            uint64_t start_ns, uint64_t nullify_start_ns,
                            uint64_t sweep_start_ns, uint64_t end_ns,
                            StatsCollectionKind kind);
void Stats_OnExit(Stats *stats);

#endif // IMMIX_STATS_H