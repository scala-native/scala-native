// _GNU_SOURCE must be defined before any includes for
// program_invocation_short_name
#if defined(__linux__)
#define _GNU_SOURCE
#endif

// Disable MSVC deprecation warnings for standard C functions
#ifdef _WIN32
#define _CRT_SECURE_NO_WARNINGS
#endif

#include "shared/Log.h"
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <time.h>

// Portable case-insensitive string comparison, time and process functions
#ifdef _WIN32
#include <string.h>
#include <windows.h>
#include <process.h>
#define strcasecmp _stricmp
#define getpid _getpid
#else
#include <strings.h>
#include <sys/time.h>
#include <unistd.h>
#endif

// Platform-specific program name access
#if defined(__APPLE__)
// getprogname() is in stdlib.h (already included)
#elif defined(__linux__)
#include <errno.h>
extern char *program_invocation_short_name;
#endif

// Whether to include timestamps in log messages (enabled by default)
// Set to 0 to disable timestamps at compile time
#ifndef GC_LOG_SHOW_TIME
#define GC_LOG_SHOW_TIME 1
#endif

// Whether to include process ID in log messages (enabled by default)
// Set to 0 to disable PID at compile time
#ifndef GC_LOG_SHOW_PID
#define GC_LOG_SHOW_PID 1
#endif

#if GC_LOG_SHOW_PID
// Get the program name (platform-specific)
static const char *GC_Log_GetProgName(void) {
#if defined(_WIN32)
    static char progName[MAX_PATH] = {0};
    if (progName[0] == '\0') {
        GetModuleFileNameA(NULL, progName, MAX_PATH);
        // Extract just the filename
        char *lastSlash = strrchr(progName, '\\');
        if (lastSlash != NULL) {
            memmove(progName, lastSlash + 1, strlen(lastSlash));
        }
    }
    return progName;
#elif defined(__APPLE__)
    return getprogname();
#elif defined(__linux__)
    return program_invocation_short_name;
#else
    return "unknown";
#endif
}
#endif

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
    char *fileEnv = getenv("GC_LOG_FILE");
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
        if (strcasecmp(levelEnv, "debug") == 0) {
            GC_logLevel = GC_LOG_LEVEL_DEBUG;
        } else if (strcasecmp(levelEnv, "info") == 0) {
            GC_logLevel = GC_LOG_LEVEL_INFO;
        } else if (strcasecmp(levelEnv, "warn") == 0) {
            GC_logLevel = GC_LOG_LEVEL_WARN;
        } else if (strcasecmp(levelEnv, "error") == 0) {
            GC_logLevel = GC_LOG_LEVEL_ERROR;
        } else if (strcasecmp(levelEnv, "none") == 0) {
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
    int written;

#if GC_LOG_SHOW_TIME
    // Write timestamp
#ifdef _WIN32
    SYSTEMTIME st;
    GetLocalTime(&st);
    written = snprintf(buffer + offset, remaining, "[%02d:%02d:%02d.%03d] ",
                       st.wHour, st.wMinute, st.wSecond, st.wMilliseconds);
#else
    struct timeval tv;
    gettimeofday(&tv, NULL);
    struct tm *tm_info = localtime(&tv.tv_sec);
    written = snprintf(buffer + offset, remaining, "[%02d:%02d:%02d.%03d] ",
                       tm_info->tm_hour, tm_info->tm_min, tm_info->tm_sec,
                       (int)(tv.tv_usec / 1000));
#endif
    if (written > 0 && written < remaining) {
        offset += written;
        remaining -= written;
    }
#endif

#if GC_LOG_SHOW_PID
    // Write process name and PID
    written = snprintf(buffer + offset, remaining, "[%s:%d] ",
                       GC_Log_GetProgName(), (int)getpid());
    if (written > 0 && written < remaining) {
        offset += written;
        remaining -= written;
    }
#endif

    // Write prefix
    written = snprintf(buffer + offset, remaining, "%s ", prefix);
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
