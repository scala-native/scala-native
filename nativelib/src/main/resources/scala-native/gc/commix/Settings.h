#ifndef COMMIX_SETTINGS_H
#define COMMIX_SETTINGS_H

#include <stddef.h>
#include <stdint.h>
#include <stdbool.h>

#include "shared/Settings.h"

#define GC_STATS_FILE_SETTING "GC_STATS_FILE"

// =============================================================================
// Heap Settings API
// =============================================================================
size_t Settings_MinHeapSize(void);
size_t Settings_MaxHeapSize(void);
double Settings_MaxMarkTimeRatio(void);
double Settings_MinFreeRatio(void);
#ifdef ENABLE_GC_STATS
char *Settings_StatsFileName(void);
#endif
int Settings_GCThreadCount(void);

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

#endif // COMMIX_SETTINGS_H
