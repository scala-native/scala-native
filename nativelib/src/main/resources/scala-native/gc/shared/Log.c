#include "shared/Log.h"
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>

// Global log level - default to WARN
static GC_LogLevel GC_logLevel = GC_LOG_LEVEL_WARN;
static bool GC_logInitialized = false;

// Output stream - default to stderr, can be redirected to file
static FILE *GC_logOutput = NULL;

void GC_Log_Init(void) {
    if (GC_logInitialized)
        return;
    GC_logInitialized = true;

    // Default output to stderr
    GC_logOutput = stderr;

    // Check for log file redirection
    char *fileEnv = getenv("SCALANATIVE_GC_LOG_FILE");
    if (fileEnv != NULL && fileEnv[0] != '\0') {
        FILE *logFile = fopen(fileEnv, "a");
        if (logFile != NULL) {
            GC_logOutput = logFile;
        } else {
            // Fall back to stderr and warn about the failure
            fprintf(stderr,
                    "[ScalaNative GC | Warning] Failed to open log file: %s\n",
                    fileEnv);
        }
    }

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

GC_LogLevel GC_Log_GetLevel(void) { return GC_logLevel; }

void GC_Log_Write(GC_LogLevel level, const char *prefix, const char *format,
                  ...) {
    // Ensure initialized (lazy init for early logging)
    if (!GC_logInitialized) {
        GC_Log_Init();
    }

    // Check level
    if (level < GC_logLevel)
        return;

    // Build complete message in buffer to avoid interleaved output from
    // multiple threads
    char buffer[1024];
    int offset = 0;
    int remaining = sizeof(buffer) - 1;

    // Write prefix
    int written = snprintf(buffer + offset, remaining, "%s ", prefix);
    if (written > 0 && written < remaining) {
        offset += written;
        remaining -= written;
    }

    // Write formatted message
    va_list args;
    va_start(args, format);
    written = vsnprintf(buffer + offset, remaining, format, args);
    va_end(args);

    if (written > 0 && written < remaining) {
        offset += written;
        remaining -= written;
    }

    // Add newline
    if (remaining > 1) {
        buffer[offset++] = '\n';
    }
    buffer[offset] = '\0';

    // Single atomic write
    fputs(buffer, GC_logOutput);
    fflush(GC_logOutput);
}
