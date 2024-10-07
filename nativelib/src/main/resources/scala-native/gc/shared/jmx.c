#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX) ||         \
    defined(SCALANATIVE_GC_BOEHM)

#include <stdlib.h>

// The total (accumulated) number of GC runs
static size_t GC_STATS_COLLECTION_TOTAL = 0L;

// The total (accumulated) elapsed time in nanos of GC runs
static size_t GC_STATS_COLLECTION_DURATION_TOTAL = 0L;

size_t jmx_stats_get_collection_total() { return GC_STATS_COLLECTION_TOTAL; }

size_t jmx_stats_get_collection_duration_total() {
    return GC_STATS_COLLECTION_DURATION_TOTAL;
}

void jmx_stats_record_collection(size_t start_ns, size_t end_ns) {
    GC_STATS_COLLECTION_TOTAL++;
    GC_STATS_COLLECTION_DURATION_TOTAL += (end_ns - start_ns);
}

#endif // defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX) ||
       // defined(SCALANATIVE_GC_BOEHM)