#ifndef GC_SHARED_SYNC_SETTINGS_H
#define GC_SHARED_SYNC_SETTINGS_H

#include <stdint.h>
#include <stdbool.h>

// =============================================================================
// Environment Variable Names
// =============================================================================
#define GC_SYNC_TIMEOUT_MS_SETTING "SCALANATIVE_GC_SYNC_TIMEOUT_MS"
#define GC_SYNC_WARNING_INTERVAL_MS_SETTING                                    \
    "SCALANATIVE_GC_SYNC_WARNING_INTERVAL_MS"

// =============================================================================
// Default Values for GC Synchronization Timeout
// =============================================================================
// These can be overridden via environment variables
#define GC_SYNC_TIMEOUT_MS_DEFAULT 60000          // 60 seconds
#define GC_SYNC_WARNING_INTERVAL_MS_DEFAULT 10000 // 10 seconds

// =============================================================================
// GC Synchronization Timeout Settings API
// =============================================================================

// Initialize sync settings (call once at GC init, after GC_Log_Init)
void SharedSettings_Init(void);

// Get the timeout for waiting for threads to reach safepoint (in milliseconds)
// Returns 0 if timeout is disabled
uint64_t SharedSettings_TimeoutMs(void);

// Get the interval for printing warnings while waiting (in milliseconds)
uint64_t SharedSettings_WarningIntervalMs(void);

// Check if sync timeout is enabled
bool SharedSettings_TimeoutEnabled(void);

#endif // GC_SHARED_SYNC_SETTINGS_H
