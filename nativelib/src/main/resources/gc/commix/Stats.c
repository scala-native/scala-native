#include "Stats.h"
#include "GCTypes.h"
#include <stdio.h>
#include <inttypes.h>

const char *const Stats_eventNames[] = {"mark", "sweep", "concmark",
                                        "concsweep", "collection",
                                        "mark_batch", "sweep_batch", "coalesce_batch"};

void Stats_writeToFile(Stats *stats);

void Stats_Init(Stats *stats, const char *statsFile, int8_t gc_thread) {
    stats->outFile = fopen(statsFile, "w");
    stats->gc_thread = gc_thread;
    fprintf(stats->outFile, "event_type,gc_thread,start_ns,time_ns\n");
    stats->events = 0;
}

NOINLINE
void Stats_RecordEvent(Stats *stats, eventType eType,
                       uint64_t start_ns, uint64_t end_ns) {
    uint64_t index = stats->events % STATS_MEASUREMENTS;
    stats->start_ns[index] = start_ns;
    stats->time_ns[index] = end_ns - start_ns;
    stats->event_types[index] = eType;
    stats->events += 1;
    if (stats->events % STATS_MEASUREMENTS == 0) {
        Stats_writeToFile(stats);
    }
}

void Stats_writeToFile(Stats *stats) {
    uint64_t events = stats->events;
    uint64_t remainder = events % STATS_MEASUREMENTS;
    if (remainder == 0) {
        remainder = STATS_MEASUREMENTS;
    }
    FILE *outFile = stats->outFile;
    for (uint64_t i = 0; i < remainder; i++) {
        fprintf(outFile, "%s,%" PRId8 ",%" PRIu64 ",%" PRIu64 "\n",
                Stats_eventNames[stats->event_types[i]], stats->gc_thread,
                stats->start_ns[i], stats->time_ns[i]);
    }
    fflush(outFile);
}

void Stats_OnExit(Stats *stats) {
    if (stats != NULL) {
        uint64_t remainder = stats->events % STATS_MEASUREMENTS;
        if (remainder > 0) {
            // there were some measurements not written in the last full batch.
            Stats_writeToFile(stats);
        }
        fclose(stats->outFile);
    }
}