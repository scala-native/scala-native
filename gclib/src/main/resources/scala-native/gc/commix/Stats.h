#ifndef IMMIX_STATS_H
#define IMMIX_STATS_H

#include "Constants.h"
#include <stdint.h>
#include <stdio.h>
#include <time.h>

#define MUTATOR_THREAD_ID -1

#ifdef ENABLE_GC_STATS_SYNC
#define ENABLE_GC_STATS_BATCHES
#endif

#ifdef ENABLE_GC_STATS_BATCHES
#define ENABLE_GC_STATS
#endif

#ifdef ENABLE_GC_STATS

typedef enum {
    // mark, nullify and sweep phases on mutator thread
    event_mark = 0x0,
    event_nullify = 0x1,
    event_sweep = 0x2,
    // mark, nullify and sweep phases on GCThreads
    event_concurrent_mark = 0x3,
    event_concurrent_nullify = 0x4,
    event_concurrent_sweep = 0x5,
    // the whole collection from initialization until concurrent sweep stops
    event_collection = 0x6,
    // batches being processed
    event_mark_batch = 0x7,
    event_sweep_batch = 0x8,
    event_coalesce_batch = 0x9,
    // thread is waiting for full packets to be available during mark or nullify
    mark_waiting = 0xA,
    nullify_waiting = 0xB,
    // any synchronization on common concurrent data structures
    event_sync = 0xC
} eventType;

typedef struct {
    FILE *outFile;
    uint64_t events;
    int8_t gc_thread;
    uint8_t event_types[STATS_MEASUREMENTS];
    uint64_t start_ns[STATS_MEASUREMENTS];
    uint64_t time_ns[STATS_MEASUREMENTS];

    uint64_t collection_start_ns;
    uint64_t packet_waiting_start_ns;
    uint64_t packet_waiting_end_ns;
} Stats;

void Stats_Init(Stats *stats, const char *statsFile, int8_t gc_thread);
void Stats_RecordEvent(Stats *stats, eventType eType, uint64_t start_ns,
                       uint64_t end_ns);
void Stats_OnExit(Stats *stats);
void Stats_WriteToFile(Stats *stats);

void Stats_CollectionStarted(Stats *stats);

#define Stats_OrNull(S) S
#define Stats_DefineOrNothing(D, S) Stats *D = S

#else
typedef void *Stats;
typedef void *eventType;
#define mark_waiting 0
#define nullify_waiting 0

#define Stats_RecordEvent(S, E, A, B)
// it is always NULL no need to read the expression
#define Stats_OrNull(S) NULL
#define Stats_DefineOrNothing(D, S)
static inline void Stats_WriteToFile(Stats *stats) {}
static inline void Stats_CollectionStarted(Stats *stats) {}

#endif // ENABLE_GC_STATS

// do { } while(0) allows to write multiline macros and
// make them look like functions by handling ; correctly
#ifdef ENABLE_GC_STATS
#define Stats_RecordTime(S, T)                                                 \
    uint64_t T;                                                                \
    do {                                                                       \
        if (S != NULL) {                                                       \
            T = scalanative_nano_time();                                       \
        }                                                                      \
    } while (0)
#else
#define Stats_RecordTime(S, T)
#endif // ENABLE_GC_STATS

#ifdef ENABLE_GC_STATS_BATCHES
#define Stats_RecordTimeBatch(S, T)                                            \
    uint64_t T;                                                                \
    do {                                                                       \
        if (S != NULL) {                                                       \
            T = scalanative_nano_time();                                       \
        }                                                                      \
    } while (0)
#define Stats_RecordEventBatches(S, E, A, B) Stats_RecordEvent(S, E, A, B)
#else
#define Stats_RecordTimeBatch(S, T)
#define Stats_RecordEventBatches(S, E, A, B)
#endif // ENABLE_GC_STATS_BATCHES

#ifdef ENABLE_GC_STATS_SYNC
#define Stats_RecordTimeSync(S, T)                                             \
    uint64_t T;                                                                \
    do {                                                                       \
        if (S != NULL) {                                                       \
            T = scalanative_nano_time();                                       \
        }                                                                      \
    } while (0)
#define Stats_RecordEventSync(S, E, A, B) Stats_RecordEvent(S, E, A, B)
void Stats_PhaseStarted(Stats *stats);
void Stats_GotNotEmptyPacket(Stats *stats, uint64_t end_ns, eventType event);
void Stats_NoNotEmptyPacket(Stats *stats, uint64_t start_ns, uint64_t end_ns);

#else

#define Stats_RecordTimeSync(S, T)
#define Stats_RecordEventSync(S, E, A, B)
static inline void Stats_PhaseStarted(Stats *stats) {}
#define Stats_GotNotEmptyPacket(S, B, E)
#define Stats_NoNotEmptyPacket(S, A, B)

#endif // ENABLE_GC_STATS_SYNC

#endif // IMMIX_STATS_H