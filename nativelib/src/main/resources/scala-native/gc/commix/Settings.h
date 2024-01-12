#ifndef IMMIX_SETTINGS_H
#define IMMIX_SETTINGS_H

#define GC_STATS_FILE_SETTING "GC_STATS_FILE"

#include <stddef.h>
#include "Stats.h"

size_t Settings_MinHeapSize();
size_t Settings_MaxHeapSize();
double Settings_MaxMarkTimeRatio();
double Settings_MinFreeRatio();
#ifdef ENABLE_GC_STATS
char *Settings_StatsFileName();
#endif
int Settings_GCThreadCount();

#endif // IMMIX_SETTINGS_H