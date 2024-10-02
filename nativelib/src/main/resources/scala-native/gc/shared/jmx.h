#ifndef GC_JMX_H
#define GC_JMX_H

#include <stdlib.h>

size_t jmx_stats_get_collection_total();
size_t jmx_stats_get_collection_duration_total();
void jmx_stats_record_collection(size_t start_ns, size_t end_ns);

#endif