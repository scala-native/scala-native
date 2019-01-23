#include "Stats.h"
#include "GCTypes.h"
#include <stdio.h>
#include <inttypes.h>

#ifdef ENABLE_GC_STATS
const char *const Stats_eventNames[] = {"mark", "sweep", "concmark",
                                        "concsweep", "collection",
                                        "mark_batch", "sweep_batch", "coalesce_batch",
                                        "mark_waiting", "sync"};

void Stats_Init(Stats *stats, const char *statsFile, int8_t gc_thread) {
    stats->outFile = fopen(statsFile, "w");
    stats->gc_thread = gc_thread;
    fprintf(stats->outFile, "event_type,gc_thread,start_ns,time_ns\n");
    stats->events = 0;
}

NOINLINE
void Stats_RecordEvent(Stats *stats, eventType eType,
                       uint64_t start_ns, uint64_t end_ns) {
    uint64_t index = stats->events;
    stats->start_ns[index] = start_ns;
    stats->time_ns[index] = end_ns - start_ns;
    stats->event_types[index] = eType;
    stats->events += 1;
    if (stats->events == STATS_MEASUREMENTS) {
        Stats_WriteToFile(stats);
    }
}

void Stats_WriteToFile(Stats *stats) {
    uint64_t events = stats->events;
    FILE *outFile = stats->outFile;
    for (uint64_t i = 0; i < events; i++) {
        fprintf(outFile, "%s,%" PRId8 ",%" PRIu64 ",%" PRIu64 "\n",
                Stats_eventNames[stats->event_types[i]], stats->gc_thread,
                stats->start_ns[i], stats->time_ns[i]);
    }
    fflush(outFile);
    stats->events = 0;
}

void Stats_OnExit(Stats *stats) {
    if (stats != NULL) {
        if (stats->events > 0) {
            // there were some measurements not written in the last full batch.
            Stats_WriteToFile(stats);
        }
        fclose(stats->outFile);
    }
}
#endif //ENABLE_GC_STATS