#ifdef SCALANATIVE_FREESTANDING

#include <errno.h>
#include <stdint.h>
#include <string.h>
#include <sys/time.h>
#include <time.h>
#include <unistd.h>
#include "sn_platform.h"

int clock_gettime(clockid_t clk, struct timespec *ts) {
    int64_t ns = clk == CLOCK_REALTIME ? scalanative_platform_realtime_nanos()
                                       : scalanative_platform_monotonic_nanos();
    ts->tv_sec = (time_t)(ns / 1000000000);
    ts->tv_nsec = (long)(ns % 1000000000);
    return 0;
}

int gettimeofday(struct timeval *tv, void *tz) {
    (void)tz;
    int64_t ns = scalanative_platform_realtime_nanos();
    tv->tv_sec = (time_t)(ns / 1000000000);
    tv->tv_usec = (suseconds_t)(ns % 1000000000 / 1000);
    return 0;
}

time_t time(time_t *out) {
    time_t t = (time_t)(scalanative_platform_realtime_nanos() / 1000000000);
    if (out)
        *out = t;
    return t;
}

// Busy-waits on the monotonic clock.
int nanosleep(const struct timespec *req, struct timespec *rem) {
    int64_t until = scalanative_platform_monotonic_nanos() +
                    (int64_t)req->tv_sec * 1000000000 + req->tv_nsec;
    while (scalanative_platform_monotonic_nanos() < until)
        ;
    if (rem)
        rem->tv_sec = rem->tv_nsec = 0;
    return 0;
}

int usleep(useconds_t us) {
    struct timespec ts = {us / 1000000, (long)(us % 1000000) * 1000};
    return nanosleep(&ts, NULL);
}

// UTC, broken down; there is no time zone database.
struct tm *gmtime_r(const time_t *t, struct tm *tm) {
    int64_t secs = *t, days = secs / 86400, rem = secs % 86400;
    if (rem < 0)
        rem += 86400, days--;
    memset(tm, 0, sizeof *tm);
    tm->tm_hour = (int)(rem / 3600);
    tm->tm_min = (int)(rem % 3600 / 60);
    tm->tm_sec = (int)(rem % 60);
    tm->tm_wday = (int)((days + 4) % 7 + 7) % 7;
    // civil_from_days (Howard Hinnant)
    int64_t z = days + 719468, era = (z >= 0 ? z : z - 146096) / 146097;
    unsigned doe = (unsigned)(z - era * 146097);
    unsigned yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365;
    int64_t y = (int64_t)yoe + era * 400;
    unsigned doy = doe - (365 * yoe + yoe / 4 - yoe / 100);
    unsigned mp = (5 * doy + 2) / 153;
    unsigned d = doy - (153 * mp + 2) / 5 + 1;
    unsigned m = mp < 10 ? mp + 3 : mp - 9;
    tm->tm_year = (int)(y + (m <= 2) - 1900);
    tm->tm_mon = (int)m - 1;
    tm->tm_mday = (int)d;
    return tm;
}

struct tm *gmtime(const time_t *t) {
    static struct tm tm;
    return gmtime_r(t, &tm);
}

struct tm *localtime_r(const time_t *t, struct tm *tm) {
    return gmtime_r(t, tm);
}

struct tm *localtime(const time_t *t) {
    static struct tm tm;
    return gmtime_r(t, &tm);
}

#endif
