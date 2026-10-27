#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX) ||         \
    defined(SCALANATIVE_GC_BOEHM)

#include <stdatomic.h>
#include <stdlib.h>

// GC threads update the totals while other threads read them

// The total (accumulated) number of GC runs
static atomic_size_t GC_STATS_COLLECTION_TOTAL = 0L;

// The total (accumulated) elapsed time in nanos of GC runs
static atomic_size_t GC_STATS_COLLECTION_DURATION_TOTAL = 0L;

size_t jmx_stats_get_collection_total() {
    return atomic_load_explicit(&GC_STATS_COLLECTION_TOTAL,
                                memory_order_relaxed);
}

size_t jmx_stats_get_collection_duration_total() {
    return atomic_load_explicit(&GC_STATS_COLLECTION_DURATION_TOTAL,
                                memory_order_relaxed);
}

void jmx_stats_record_collection(size_t start_ns, size_t end_ns) {
    atomic_fetch_add_explicit(&GC_STATS_COLLECTION_TOTAL, 1,
                              memory_order_relaxed);
    atomic_fetch_add_explicit(&GC_STATS_COLLECTION_DURATION_TOTAL,
                              end_ns - start_ns, memory_order_relaxed);
}

#endif // defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX) ||
       // defined(SCALANATIVE_GC_BOEHM)