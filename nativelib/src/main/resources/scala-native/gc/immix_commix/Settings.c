#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX)

#ifdef _WIN32
// sscanf and getEnv is deprecated in WinCRT, disable warnings
#define _CRT_SECURE_NO_WARNINGS
#endif

#include <stdlib.h>
#include <stdio.h>
#include "immix_commix/Settings.h"
#include "shared/GCConfig.h"
#include "shared/Parsing.h"
#include "shared/Settings.h"
#include "immix_commix/Constants.h"
#include "shared/Log.h"

// =============================================================================
// Heap Settings
// =============================================================================
size_t Settings_MinHeapSize(void) {
    return Parse_Env_Or_Default(
        "GC_INITIAL_HEAP_SIZE",
        GC_CONFIG_INITIAL_HEAP_SIZE_OR(DEFAULT_MIN_HEAP_SIZE));
}

size_t Settings_MaxHeapSize(void) {
    return Parse_Env_Or_Default(
        "GC_MAXIMUM_HEAP_SIZE",
        GC_CONFIG_MAX_HEAP_SIZE_OR(UNLIMITED_HEAP_SIZE));
}

#ifdef SCALANATIVE_GC_CONFIG_STATS_FILE
static char builtinStatsFile[] = SCALANATIVE_GC_CONFIG_STATS_FILE;
#else
static char *builtinStatsFile = NULL;
#endif

char *Settings_StatsFileName(void) {
    // Statistics are opt-in, see statsEnabled of GarbageCollectorConfig
    if (!SCALANATIVE_GC_CONFIG_STATS_ENABLED)
        return NULL;
    char *env = getenv(GC_STATS_FILE_SETTING);
    if (env == NULL)
        return builtinStatsFile;
    // An empty variable disables the statistics set in the build
    return env[0] != '\0' ? env : NULL;
}

double Settings_Ratio(const char *name, double defaultRatio) {
    char *str = getenv(name);
    if (str == NULL)
        return defaultRatio;
    double ratio;
    // The negated range check also rejects NaN.
    if (sscanf(str, "%lf", &ratio) != 1 || !(ratio >= 0.0 && ratio <= 1.0)) {
        GC_LOG_WARN("Ignoring %s=%s, which is not a number between 0 and 1",
                    name, str);
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
