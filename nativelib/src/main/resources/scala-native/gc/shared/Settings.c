#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX)

#include "Settings.h"
#include "Parsing.h"
#include "Log.h"

// =============================================================================
// Static state for cached settings
// =============================================================================
static bool syncSettingsInitialized = false;
static uint64_t syncTimeoutMs = GC_SYNC_TIMEOUT_MS_DEFAULT;
static uint64_t syncWarningIntervalMs = GC_SYNC_WARNING_INTERVAL_MS_DEFAULT;

// =============================================================================
// GC Synchronization Settings Implementation
// =============================================================================
void SharedSettings_Init(void) {
    if (syncSettingsInitialized)
        return;
    syncSettingsInitialized = true;

    // Parse sync timeout (0 means disabled - will wait forever)
    syncTimeoutMs = Parse_Env_Or_Default_Long(GC_SYNC_TIMEOUT_MS_SETTING,
                                              GC_SYNC_TIMEOUT_MS_DEFAULT);

    // Parse warning interval (must be > 0)
    uint64_t warningVal =
        Parse_Env_Or_Default_Long(GC_SYNC_WARNING_INTERVAL_MS_SETTING,
                                  GC_SYNC_WARNING_INTERVAL_MS_DEFAULT);
    if (warningVal > 0) {
        syncWarningIntervalMs = warningVal;
    }

    GC_LOG_DEBUG("GC sync timeout: %llu ms, warning interval: %llu ms",
                 (unsigned long long)syncTimeoutMs,
                 (unsigned long long)syncWarningIntervalMs);
}

uint64_t SharedSettings_TimeoutMs(void) { return syncTimeoutMs; }

uint64_t SharedSettings_WarningIntervalMs(void) {
    return syncWarningIntervalMs;
}

bool SharedSettings_TimeoutEnabled(void) {
    return SharedSettings_TimeoutMs() > 0;
}

#endif // SCALANATIVE_GC_IMMIX || SCALANATIVE_GC_COMMIX
