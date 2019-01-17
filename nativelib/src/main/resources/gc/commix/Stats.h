#ifndef IMMIX_STATS_H
#define IMMIX_STATS_H

#include "Constants.h"
#include <stdint.h>
#include <stdio.h>
#include <time.h>

#define MUTATOR_THREAD_ID -1

typedef enum {
    event_mark = 0x0,
    event_sweep = 0x1,
    event_concurrent_mark = 0x2,
    event_concurrent_sweep = 0x3,
    event_collection = 0x4,
    event_mark_batch = 0x5,
    event_sweep_batch = 0x6,
    event_coalesce_batch = 0x7
} eventType;

typedef struct {
    FILE *outFile;
    uint64_t events;
    int8_t gc_thread;
    uint8_t event_types[STATS_MEASUREMENTS];
    uint64_t start_ns[STATS_MEASUREMENTS];
    uint64_t time_ns[STATS_MEASUREMENTS];

    uint64_t collection_start_ns;
} Stats;

void Stats_Init(Stats *stats, const char *statsFile, int8_t gc_thread);
void Stats_RecordEvent(Stats *stats, eventType eType,
                       uint64_t start_ns, uint64_t end_ns);
void Stats_OnExit(Stats *stats);

extern long long scalanative_nano_time();

#endif // IMMIX_STATS_H