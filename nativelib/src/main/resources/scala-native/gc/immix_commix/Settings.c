#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX)

#ifdef _WIN32
// sscanf and getEnv is deprecated in WinCRT, disable warnings
#define _CRT_SECURE_NO_WARNINGS
#endif

#include <stdlib.h>
#include <stdio.h>
#include "immix_commix/Settings.h"
#include "shared/Parsing.h"
#include "shared/Settings.h"
#include "immix_commix/Constants.h"
#include "shared/Log.h"

// =============================================================================
// Heap Settings
// =============================================================================
size_t Settings_MinHeapSize(void) {
    return Parse_Env_Or_Default("GC_INITIAL_HEAP_SIZE", DEFAULT_MIN_HEAP_SIZE);
}

size_t Settings_MaxHeapSize(void) {
    return Parse_Env_Or_Default("GC_MAXIMUM_HEAP_SIZE", UNLIMITED_HEAP_SIZE);
}

char *Settings_StatsFileName(void) { return getenv(GC_STATS_FILE_SETTING); }

double Settings_Ratio(const char *name, double defaultRatio) {
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

// =============================================================================
// Settings Initialization
// =============================================================================
void Settings_Init(void) {

    // Initialize sync timeout settings
    SharedSettings_Init();
}

#endif // SCALANATIVE_GC_IMMIX || SCALANATIVE_GC_COMMIX
