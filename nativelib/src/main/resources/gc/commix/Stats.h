#ifndef IMMIX_STATS_H
#define IMMIX_STATS_H

#include "Constants.h"
#include <stdint.h>
#include <stdio.h>
#include <time.h>
#include <pthread.h>

#define MUTATOR_THREAD_ID -1

typedef enum {
    event_mark = 0x0,
    event_sweep = 0x1,
    event_concurrent_mark = 0x2,
    event_concurrent_sweep = 0x3,
    event_collection = 0x4
} eventType;

typedef struct {
    FILE *outFile;
    pthread_mutex_t mutex;
    uint64_t events;
    uint8_t event_types[STATS_MEASUREMENTS];
    int8_t gc_threads[STATS_MEASUREMENTS];
    uint64_t start_ns[STATS_MEASUREMENTS];
    uint64_t time_ns[STATS_MEASUREMENTS];

    uint64_t collection_start_ns;
} Stats;

void Stats_Init(Stats *stats, const char *statsFile);
void Stats_RecordEvent(Stats *stats, eventType eType, int8_t gc_thread,
                       uint64_t start_ns, uint64_t end_ns);
void Stats_OnExit(Stats *stats);

extern long long scalanative_nano_time();

#endif // IMMIX_STATS_H