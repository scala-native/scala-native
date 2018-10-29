#include "Settings.h"
#include <stdlib.h>

char *Settings_StatsFileName() {
    return getenv(STATS_FILE_SETTING);
}