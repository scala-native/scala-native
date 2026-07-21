#ifndef GC_LOG_H
#define GC_LOG_H

#include <assert.h>
#include <inttypes.h>
#include <stdio.h>
#include <stdbool.h>

// =============================================================================
// GC Logging Utility
// =============================================================================
// Log levels: DEBUG < INFO < WARN < ERROR
// Controlled by environment variable SCALANATIVE_GC_LOG_LEVEL:
//   - "debug" or "DEBUG" - show all messages (internal operations, allocations)
//   - "info" or "INFO"   - show info, warnings and errors (parsed options,
//   stats)
//   - "warn" or "WARN"   - show warnings and errors (default)
//   - "error" or "ERROR" - show only errors
//   - "none" or "NONE"   - suppress all messages
//
// Log output destination controlled by GC_LOG_FILE:
//   - If not set or empty: logs go to stderr (default)
//   - If set to a file path: logs are appended to that file
//
// Timestamps are included by default in [HH:MM:SS.mmm] format.
// Can be disabled at compile time with -DGC_LOG_SHOW_TIME=0
//
// Process ID and program name are included by default in [progname:pid] format.
// Can be disabled at compile time with -DGC_LOG_SHOW_PID=0

typedef enum {
    GC_LOG_LEVEL_DEBUG = 0,
    GC_LOG_LEVEL_INFO = 1,
    GC_LOG_LEVEL_WARN = 2,
    GC_LOG_LEVEL_ERROR = 3,
    GC_LOG_LEVEL_NONE = 4
} GC_LogLevel;

// Initialize logging from environment variable
void GC_Log_Init(void);

GC_LogLevel GC_Log_GetLevel(void);

// Internal logging function
void GC_Log_Write(GC_LogLevel level, const char *prefix, const char *format,
                  ...);

// Logging macros - check level before formatting to avoid overhead
#define GC_LOG_DEBUG(fmt, ...)                                                 \
    do {                                                                       \
        if (GC_Log_GetLevel() <= GC_LOG_LEVEL_DEBUG) {                         \
            GC_Log_Write(GC_LOG_LEVEL_DEBUG, "[ScalaNative GC|Debug]", fmt,    \
                         ##__VA_ARGS__);                                       \
        }                                                                      \
    } while (0)

#define GC_LOG_INFO(fmt, ...)                                                  \
    do {                                                                       \
        if (GC_Log_GetLevel() <= GC_LOG_LEVEL_INFO) {                          \
            GC_Log_Write(GC_LOG_LEVEL_INFO, "[ScalaNative GC|Info]", fmt,      \
                         ##__VA_ARGS__);                                       \
        }                                                                      \
    } while (0)

#define GC_LOG_WARN(fmt, ...)                                                  \
    do {                                                                       \
        if (GC_Log_GetLevel() <= GC_LOG_LEVEL_WARN) {                          \
            GC_Log_Write(GC_LOG_LEVEL_WARN, "[ScalaNative GC|Warning]", fmt,   \
                         ##__VA_ARGS__);                                       \
        }                                                                      \
    } while (0)

#define GC_LOG_ERROR(fmt, ...)                                                 \
    do {                                                                       \
        if (GC_Log_GetLevel() <= GC_LOG_LEVEL_ERROR) {                         \
            GC_Log_Write(GC_LOG_LEVEL_ERROR, "[ScalaNative GC|Error]", fmt,    \
                         ##__VA_ARGS__);                                       \
        }                                                                      \
    } while (0)

#endif // GC_LOG_H
