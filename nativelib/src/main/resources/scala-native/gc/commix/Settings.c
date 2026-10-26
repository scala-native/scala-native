#if defined(SCALANATIVE_GC_COMMIX)

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
#include <stdbool.h>
#include "Settings.h"
#include "Constants.h"
#include "shared/Parsing.h"
#include "shared/Log.h"
#include "shared/Settings.h"

// =============================================================================
// Heap Settings
// =============================================================================
size_t Settings_MinHeapSize(void) {
    return Parse_Env_Or_Default("GC_INITIAL_HEAP_SIZE", DEFAULT_MIN_HEAP_SIZE);
}

size_t Settings_MaxHeapSize(void) {
    return Parse_Env_Or_Default("GC_MAXIMUM_HEAP_SIZE", UNLIMITED_HEAP_SIZE);
}

double Settings_MaxMarkTimeRatio(void) {
    char *str = getenv("GC_TIME_RATIO");
    if (str == NULL) {
        return DEFAULT_MARK_TIME_RATIO;
    } else {
        double ratio;
        sscanf(str, "%lf", &ratio);
        return ratio;
    }
}

double Settings_MinFreeRatio(void) {
    char *str = getenv("GC_FREE_RATIO");
    if (str == NULL) {
        return DEFAULT_FREE_RATIO;
    } else {
        double ratio;
        sscanf(str, "%lf", &ratio);
        return ratio;
    }
}

#ifdef ENABLE_GC_STATS
char *Settings_StatsFileName(void) { return getenv(GC_STATS_FILE_SETTING); }
#endif

// False if str is NULL or not a number. Counts below 1 become 1.
static bool Settings_parseThreadCount(const char *source, const char *str,
                                      int *count) {
    if (str == NULL)
        return false;
    if (sscanf(str, "%d", count) != 1) {
        GC_LOG_WARN("Ignoring %s=%s, which is not a number", source, str);
        return false;
    }
    if (*count < 1)
        *count = 1;
    return true;
}

static int Settings_otherProcessors(void) {
#ifdef _WIN32
    SYSTEM_INFO sysInfo;
    GetSystemInfo(&sysInfo);
    int processorCount = (int)sysInfo.dwNumberOfProcessors;
#else
    int processorCount = (int)sysconf(_SC_NPROCESSORS_ONLN);
#endif
    return processorCount > 1 ? processorCount - 1 : 1;
}

// GC_NPROCS from the environment, else -DGC_NPROCS capped at the processor
// count - 1, else the processor count - 1 capped at 8. At least 1.
int Settings_GCThreadCount(void) {
    int count;
    const char *env = getenv("GC_NPROCS");
    if (Settings_parseThreadCount("GC_NPROCS", env, &count)) {
        GC_LOG_INFO("Found GC_NPROCS=%s, using %d GC threads", env, count);
        return count;
    }
    int others = Settings_otherProcessors();
    const char *defined = get_defined("GC_NPROCS");
    if (Settings_parseThreadCount("-DGC_NPROCS", defined, &count)) {
        if (count > others)
            count = others;
        GC_LOG_INFO("GC_NPROCS not set, using %d GC threads (built with "
                    "GC_NPROCS=%s)",
                    count, defined);
        return count;
    }
    count = others > 8 ? 8 : others;
    GC_LOG_INFO("GC_NPROCS not set, using %d GC threads", count);
    return count;
}

// =============================================================================
// Settings Initialization
// =============================================================================
void Settings_Init(void) {

    // Initialize sync timeout settings
    SharedSettings_Init();
}

#endif // SCALANATIVE_GC_COMMIX
