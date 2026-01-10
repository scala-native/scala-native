#if defined(SCALANATIVE_GC_IMMIX)

#ifdef _WIN32
// sscanf and getEnv is deprecated in WinCRT, disable warnings
#define _CRT_SECURE_NO_WARNINGS
#endif

#include <stdlib.h>
#include "Settings.h"
#include "shared/Parsing.h"
#include "shared/Settings.h"
#include "Constants.h"
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

// =============================================================================
// Settings Initialization
// =============================================================================
void Settings_Init(void) {

    // Initialize sync timeout settings
    SharedSettings_Init();
}

#endif // SCALANATIVE_GC_IMMIX
