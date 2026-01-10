#ifndef IMMIX_SETTINGS_H
#define IMMIX_SETTINGS_H

#include <stddef.h>
#include <stdint.h>
#include <stdbool.h>

#include "shared/Settings.h"

// =============================================================================
// Environment Variable Names
// =============================================================================
#define GC_STATS_FILE_SETTING "GC_STATS_FILE"

// =============================================================================
// Heap Settings API
// =============================================================================
size_t Settings_MinHeapSize(void);
size_t Settings_MaxHeapSize(void);
char *Settings_StatsFileName(void);

// =============================================================================
// Settings Initialization
// =============================================================================
// Initialize all settings (call once at GC init)
void Settings_Init(void);

// =============================================================================
// GC Synchronization Timeout Settings API
// Wrapper functions that delegate to Settings
// =============================================================================

// Get the timeout for waiting for threads to reach safepoint (in milliseconds)
// Returns 0 if timeout is disabled
static inline uint64_t Settings_SyncTimeoutMs(void) {
    return SharedSettings_TimeoutMs();
}

// Get the interval for printing warnings while waiting (in milliseconds)
static inline uint64_t Settings_SyncWarningIntervalMs(void) {
    return SharedSettings_WarningIntervalMs();
}

// Check if sync timeout is enabled
static inline bool Settings_SyncTimeoutEnabled(void) {
    return SharedSettings_TimeoutEnabled();
}

#endif // IMMIX_SETTINGS_H
