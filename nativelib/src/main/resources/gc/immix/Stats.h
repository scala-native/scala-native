#ifndef IMMIX_STATS_H
#define IMMIX_STATS_H

#include "Constants.h"
#include <stdint.h>
#include <stdio.h>
#include <time.h>

typedef struct {
    FILE *outFile;
    uint64_t collections;
    uint64_t timestamp_us[GC_STATS_MEASUREMENTS];
    uint64_t mark_time_us[GC_STATS_MEASUREMENTS];
    uint64_t sweep_time_us[GC_STATS_MEASUREMENTS];
} Stats;

void Stats_Init(Stats *stats, const char *statsFile);
void Stats_RecordCollection(Stats *stats, struct timespec *start, struct timespec *sweep_start, struct timespec *end);
void Stats_Close(Stats *stats);

#endif // IMMIX_STATS_H