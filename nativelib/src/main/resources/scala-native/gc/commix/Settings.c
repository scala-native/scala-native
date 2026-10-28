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

static double Settings_ratio(const char *name, double defaultRatio) {
    char *str = getenv(name);
    if (str == NULL)
        return defaultRatio;
    double ratio;
    if (sscanf(str, "%lf", &ratio) != 1) {
        GC_LOG_WARN("Ignoring %s=%s, which is not a number", name, str);
        return defaultRatio;
    }
    return ratio;
}

double Settings_MaxMarkTimeRatio(void) {
    return Settings_ratio("GC_TIME_RATIO", DEFAULT_MARK_TIME_RATIO);
}

double Settings_MinFreeRatio(void) {
    return Settings_ratio("GC_FREE_RATIO", DEFAULT_FREE_RATIO);
}

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

#endif // SCALANATIVE_GC_COMMIX
