#if defined(_WIN32)
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <wchar.h>
#else
// #define _GNU_SOURCE /* for tm_gmtoff and tm_zone if needed */
#include <stdio.h>
#include <time.h>
#endif

long long scalanative_time_zone_offset() {
    long long time_zone_offset_secs;

#if defined(_WIN32)
    TIME_ZONE_INFORMATION tzi = {0};
    int r = GetTimeZoneInformation(&tzi);
    if (r == TIME_ZONE_ID_INVALID) {
        // If failed return 0 - default for UTC
        time_zone_offset_secs = 0L;
    } else {
        // Bias is in minutes
        time_zone_offset_secs = tzi.Bias * 60;
    }
#else
    time_t t = time(NULL);
    struct tm lt = {0};
    localtime_r(&t, &lt);
    time_zone_offset_secs = lt.tm_gmtoff;
#endif
    return time_zone_offset_secs;
}
