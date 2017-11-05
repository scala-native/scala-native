#include <stdio.h>
#ifndef _WIN32
#include <sys/time.h>
#else
#include "os_win_time.h"
#endif
#include <time.h>

long long scalanative_current_time_millis() {
    long long current_time_millis;

#define MILLIS_PER_SEC 1000
#define MICROS_PER_MILLI 1000

    struct timeval tv;
    gettimeofday(&tv, NULL);
    current_time_millis =
        tv.tv_sec * MILLIS_PER_SEC + tv.tv_usec / MICROS_PER_MILLI;

#undef MILLIS_PER_SEC
#undef MICROS_PER_MILLI

    return current_time_millis;
}
