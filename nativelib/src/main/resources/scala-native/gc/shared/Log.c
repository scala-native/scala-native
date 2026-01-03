#include "shared/Log.h"
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>

// Global log level - default to WARN
GC_LogLevel GC_logLevel = GC_LOG_LEVEL_WARN;
bool GC_logInitialized = false;

void GC_Log_Init(void) {
    if (GC_logInitialized)
        return;
    GC_logInitialized = true;

    // Default level
    GC_logLevel = GC_LOG_LEVEL_WARN;

    char *levelEnv = getenv("SCALANATIVE_GC_LOG_LEVEL");
    if (levelEnv != NULL) {
        if (strcmp(levelEnv, "debug") == 0 || strcmp(levelEnv, "DEBUG") == 0) {
            GC_logLevel = GC_LOG_LEVEL_DEBUG;
        } else if (strcmp(levelEnv, "warn") == 0 ||
                   strcmp(levelEnv, "WARN") == 0) {
            GC_logLevel = GC_LOG_LEVEL_WARN;
        } else if (strcmp(levelEnv, "error") == 0 ||
                   strcmp(levelEnv, "ERROR") == 0) {
            GC_logLevel = GC_LOG_LEVEL_ERROR;
        } else if (strcmp(levelEnv, "none") == 0 ||
                   strcmp(levelEnv, "NONE") == 0) {
            GC_logLevel = GC_LOG_LEVEL_NONE;
        }
    }
}

void GC_Log_Write(GC_LogLevel level, const char *prefix, const char *format,
                  ...) {
    // Ensure initialized (lazy init for early logging)
    if (!GC_logInitialized) {
        GC_Log_Init();
    }

    // Check level
    if (level < GC_logLevel)
        return;

    va_list args;
    va_start(args, format);

    fprintf(stderr, "%s ", prefix);
    vfprintf(stderr, format, args);
    fprintf(stderr, "\n");
    fflush(stderr);

    va_end(args);
}
