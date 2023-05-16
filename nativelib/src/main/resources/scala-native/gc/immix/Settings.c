#ifdef _WIN32
// sscanf and getEnv is deprecated in WinCRT, disable warnings
#define _CRT_SECURE_NO_WARNINGS
#endif

#include <stdlib.h>
#include "Settings.h"
#include "Parsing.h"
#include "Constants.h"

size_t Settings_MinHeapSize() {
    return Parse_Env_Or_Default("GC_INITIAL_HEAP_SIZE", DEFAULT_MIN_HEAP_SIZE);
}

size_t Settings_MaxHeapSize() {
    return Parse_Env_Or_Default("GC_MAXIMUM_HEAP_SIZE", UNLIMITED_HEAP_SIZE);
}

char *Settings_StatsFileName() { return getenv(STATS_FILE_SETTING); }
