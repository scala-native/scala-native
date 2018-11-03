#ifndef IMMIX_STATS_H
#define IMMIX_STATS_H

#include "Constants.h"
#include <stdint.h>
#include <stdio.h>
#include <time.h>

typedef enum {
    event_mark = 0x0,
    event_sweep = 0x1
} eventType;

typedef struct {
    FILE *outFile;
    uint64_t events;
    uint8_t event_types[STATS_MEASUREMENTS];
    uint64_t time_ns[STATS_MEASUREMENTS];
} Stats;

void Stats_Init(Stats *stats, const char *statsFile);
void Stats_RecordEvent(Stats *stats, eventType eType, uint64_t start_ns, uint64_t end_ns);
void Stats_OnExit(Stats *stats);

extern long long scalanative_nano_time();

#endif // IMMIX_STATS_H