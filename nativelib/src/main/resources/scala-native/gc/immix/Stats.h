#ifndef IMMIX_STATS_H
#define IMMIX_STATS_H

#include "Constants.h"
#include <stdint.h>
#include <stdio.h>
#include <time.h>

typedef struct {
    FILE *outFile;
    uint64_t collections;
    uint64_t mark_time_ns[STATS_MEASUREMENTS];
    uint64_t sweep_time_ns[STATS_MEASUREMENTS];
} Stats;

void Stats_Init(Stats *stats, const char *statsFile);
void Stats_RecordCollection(Stats *stats, uint64_t start_ns,
                            uint64_t sweep_start_ns, uint64_t end_ns);
void Stats_OnExit(Stats *stats);

extern long long scalanative_nano_time();

#endif // IMMIX_STATS_H