#ifndef GC_LOG_H
#define GC_LOG_H

#ifdef GC_ASSERTIONS
#undef NDEBUG
#else
#ifndef NDEBUG
#define NDEBUG
#endif // NDEBUG
#endif // GC_ASSERTIONS

#include <assert.h>
#include <inttypes.h>
#include <stdio.h>
#include <stdbool.h>

// =============================================================================
// GC Logging Utility
// =============================================================================
// Log levels: DEBUG < WARN < ERROR
// Controlled by environment variable SCALANATIVE_GC_LOG_LEVEL:
//   - "debug" or "DEBUG" - show all messages
//   - "warn" or "WARN"   - show warnings and errors (default)
//   - "error" or "ERROR" - show only errors
//   - "none" or "NONE"   - suppress all messages

typedef enum {
    GC_LOG_LEVEL_DEBUG = 0,
    GC_LOG_LEVEL_WARN = 1,
    GC_LOG_LEVEL_ERROR = 2,
    GC_LOG_LEVEL_NONE = 3
} GC_LogLevel;

// Global log level - initialized by GC_Log_Init()
extern GC_LogLevel GC_logLevel;
extern bool GC_logInitialized;

// Initialize logging from environment variable
void GC_Log_Init(void);

// Internal logging function
void GC_Log_Write(GC_LogLevel level, const char *prefix, const char *format,
                  ...);

// Logging macros - check level before formatting to avoid overhead
#define GC_LOG_DEBUG(fmt, ...)                                                 \
    do {                                                                       \
        if (GC_logLevel <= GC_LOG_LEVEL_DEBUG) {                               \
            GC_Log_Write(GC_LOG_LEVEL_DEBUG, "[ScalaNative GC | Debug] ", fmt, \
                         ##__VA_ARGS__);                                       \
        }                                                                      \
    } while (0)

#define GC_LOG_WARN(fmt, ...)                                                  \
    do {                                                                       \
        if (GC_logLevel <= GC_LOG_LEVEL_WARN) {                                \
            GC_Log_Write(GC_LOG_LEVEL_WARN, "[ScalaNative GC | Warning]", fmt, \
                         ##__VA_ARGS__);                                       \
        }                                                                      \
    } while (0)

#define GC_LOG_ERROR(fmt, ...)                                                 \
    do {                                                                       \
        if (GC_logLevel <= GC_LOG_LEVEL_ERROR) {                               \
            GC_Log_Write(GC_LOG_LEVEL_ERROR, "[ScalaNative GC | Error]", fmt,  \
                         ##__VA_ARGS__);                                       \
        }                                                                      \
    } while (0)

#endif // GC_LOG_H
