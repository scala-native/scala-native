#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
// sscanf and getEnv is deprecated in WinCRT, disable warnings
#define _CRT_SECURE_NO_WARNINGS
#include <windows.h>
#else
#include <unistd.h>
#endif

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "Settings.h"
#include "Constants.h"
#include "Parsing.h"

size_t Settings_MinHeapSize() {
    return Parse_Env_Or_Default("GC_INITIAL_HEAP_SIZE", DEFAULT_MIN_HEAP_SIZE);
}

size_t Settings_MaxHeapSize() {
    return Parse_Env_Or_Default("GC_MAXIMUM_HEAP_SIZE", UNLIMITED_HEAP_SIZE);
}

double Settings_MaxMarkTimeRatio() {
    char *str = getenv("SCALANATIVE_TIME_RATIO");
    if (str == NULL) {
        return DEFAULT_MARK_TIME_RATIO;
    } else {
        double ratio;
        sscanf(str, "%lf", &ratio);
        return ratio;
    }
}

double Settings_MinFreeRatio() {
    char *str = getenv("SCALANATIVE_FREE_RATIO");
    if (str == NULL) {
        return DEFAULT_FREE_RATIO;
    } else {
        double ratio;
        sscanf(str, "%lf", &ratio);
        return ratio;
    }
}

#ifdef ENABLE_GC_STATS
char *Settings_StatsFileName() { return getenv(STATS_FILE_SETTING); }
#endif

int Settings_GCThreadCount() {
    char *str = getenv("GC_NPROCS");
    if (str == NULL) {
        // default is number of cores - 1, but no less than 1 and no more than 8
#ifdef _WIN32
        SYSTEM_INFO sysInfo;
        GetSystemInfo(&sysInfo);
        int processorCount = (int)sysInfo.dwNumberOfProcessors;
#else
        int processorCount = (int)sysconf(_SC_NPROCESSORS_ONLN);
#endif
        int defaultGThreadCount = processorCount - 1;
        if (defaultGThreadCount < 1) {
            defaultGThreadCount = 1;
        } else if (defaultGThreadCount > 8) {
            defaultGThreadCount = 8;
        }
        return defaultGThreadCount;
    } else {
        int count;
        sscanf(str, "%d", &count);
        if (count < 1) {
            count = 1;
        }
        return count;
    }
}
