#include "Stats.h"
#include "GCTypes.h"
#include <stdio.h>
#include <inttypes.h>

extern long long scalanative_nano_time();

#ifdef ENABLE_GC_STATS
const char *const Stats_eventNames[] = {
    "mark",       "sweep",       "concmark",       "concsweep",    "collection",
    "mark_batch", "sweep_batch", "coalesce_batch", "mark_waiting", "sync"};

void Stats_Init(Stats *stats, const char *statsFile, int8_t gc_thread) {
    stats->outFile = fopen(statsFile, "w");
    stats->gc_thread = gc_thread;
    fprintf(stats->outFile, "event_type,gc_thread,start_ns,time_ns\n");
    stats->events = 0;
}

void Stats_CollectionStarted(Stats *stats) {
    if (stats != NULL) {
        stats->collection_start_ns = scalanative_nano_time();
    }
}

INLINE
void Stats_RecordEvent(Stats *stats, eventType eType, uint64_t start_ns,
                       uint64_t end_ns) {
    if (stats != NULL && start_ns != 0) {
        uint64_t index = stats->events;
        stats->start_ns[index] = start_ns;
        stats->time_ns[index] = end_ns - start_ns;
        stats->event_types[index] = eType;
        stats->events += 1;
        if (stats->events == STATS_MEASUREMENTS) {
            Stats_WriteToFile(stats);
        }
    }
}

INLINE
void Stats_WriteToFile(Stats *stats) {
    if (stats != NULL) {
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
#endif // ENABLE_GC_STATS

#ifdef ENABLE_GC_STATS_SYNC
void Stats_MarkStarted(Stats *stats) {
    if (stats != NULL) {
        stats->mark_waiting_start_ns = 0;
        stats->mark_waiting_end_ns = 0;
    }
}
void Stats_MarkerGotFullPacket(Stats *stats, uint64_t end_ns) {
    if (stats != NULL) {
        if (stats->mark_waiting_start_ns != 0) {
            Stats_RecordEventSync(stats, mark_waiting,
                                  stats->mark_waiting_start_ns, end_ns);
            stats->mark_waiting_start_ns = 0;
        }
    }
}
void Stats_MarkerNoFullPacket(Stats *stats, uint64_t start_ns,
                              uint64_t end_ns) {
    if (stats != NULL) {
        if (stats->mark_waiting_start_ns == 0) {
            stats->mark_waiting_start_ns = start_ns;
        }
        stats->mark_waiting_end_ns = end_ns;
    }
}
#endif // ENABLE_GC_STATS_SYNC