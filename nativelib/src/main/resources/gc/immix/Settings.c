#include "Settings.h"
#include <stdlib.h>

char *Settings_GC_StatsFileName() {
    return getenv(GC_STATS_FILE_SETTING);
}