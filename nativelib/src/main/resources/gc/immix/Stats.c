#include "Stats.h"
#include <stdio.h>

void Stats_writeStatsToFile(Stats *stats);

void Stats_Init(Stats *stats, const char *statsFile) {
    stats->outFile = fopen(statsFile, "a");
    fprintf(stats->outFile, "timestamp_us,collection,mark_time_us,sweep_time_us\n");
    stats->collections = 0;
}

void Stats_RecordCollection(Stats *stats, struct timespec *start, struct timespec *sweep_start, struct timespec *end) {
    uint64_t start_us = start->tv_sec * 1000000 + start->tv_nsec / 1000;
    uint64_t sweep_start_us = sweep_start->tv_sec * 1000000 + sweep_start->tv_nsec / 1000;
    uint64_t end_us = end->tv_sec * 1000000 + end->tv_nsec / 1000;

    uint64_t index = stats->collections % GC_STATS_MEASUREMENTS;
    stats->timestamp_us[index] = start_us;
    stats->mark_time_us[index] = sweep_start_us - start_us;
    stats->sweep_time_us[index] = end_us - sweep_start_us;
    stats->collections += 1;
    if (stats->collections % GC_STATS_MEASUREMENTS == 0) {
        Stats_writeStatsToFile(stats);
    }
}

void Stats_writeStatsToFile(Stats *stats) {
    uint64_t collections = stats->collections;
    uint64_t remainder = collections % GC_STATS_MEASUREMENTS;
    if (remainder == 0) {
        remainder = GC_STATS_MEASUREMENTS;
    }
    uint64_t base = collections - remainder;
    FILE *outFile = stats->outFile;
    for (uint64_t i = 0; i < remainder; i++) {
        fprintf(outFile, "%lu,%lu,%lu,%lu\n",
                stats->timestamp_us[i], base + i, stats->mark_time_us[i], stats->sweep_time_us[i]);
    }
    fflush(outFile);
}

void Stats_Close(Stats *stats) {
    uint64_t remainder = stats->collections % GC_STATS_MEASUREMENTS;
    if (remainder > 0) {
        // there were some measurements not written in the last full batch.
        Stats_writeStatsToFile(stats);
    }
    fclose(stats->outFile);
}