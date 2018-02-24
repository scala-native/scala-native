#include <stdio.h>
#include <sys/time.h>
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

// There is an argument that these three should be in resources/wrap.c or
// resources resources/posix.c. tzname() is declared in time.h, so
// place here, in time.c.

char **scalanative_time_tzname() { return tzname; }
long scalanative_time_timezone() { return timezone; }
int scalanative_time_daylight() { return daylight; }
