// Windows specific copy of posixlib/time.h
// Uses *_s variants of methods instead of *_r
#if defined(_WIN32)
#include <string.h>
#include <time.h>
#include <errno.h>

#if defined(_MSC_VER)
#define _CRT_SECURE_NO_WARNINGS
#define daylight _daylight
#define timezone _timezone
#define tzname _tzname
void tzset() { _tzset(); }
#else
// — Non-MSVC Windows (MinGW, Cygwin, TDM-GCC, Clang-mingw, etc.) —
// these CRTs already offer:
//    asctime(), asctime_r(), gmtime_r(), localtime_r(), tzset()
// so we just alias the “secure” names to the POSIX names
// and provide asctime_s() if needed.
#ifndef gmtime_s
#define gmtime_s(dst, clk) gmtime_r((clk), (dst))
#endif

#ifndef localtime_s
#define localtime_s(dst, clk) localtime_r((clk), (dst))
#endif

static errno_t asctime_s(char *buf, size_t size, const struct tm *tm) {
    char *s = asctime(tm);
    size_t len = strlen(s) + 1;
    if (len > size)
        return ERANGE;
    memcpy(buf, s, len);
    return 0;
}
#endif

struct scalanative_tm {
    int tm_sec;
    int tm_min;
    int tm_hour;
    int tm_mday;
    int tm_mon;
    int tm_year;
    int tm_wday;
    int tm_yday;
    int tm_isdst;
};

static struct scalanative_tm scalanative_shared_tm_buf;

static void scalanative_tm_init(struct scalanative_tm *scala_tm,
                                struct tm *tm) {
    scala_tm->tm_sec = tm->tm_sec;
    scala_tm->tm_min = tm->tm_min;
    scala_tm->tm_hour = tm->tm_hour;
    scala_tm->tm_mday = tm->tm_mday;
    scala_tm->tm_mon = tm->tm_mon;
    scala_tm->tm_year = tm->tm_year;
    scala_tm->tm_wday = tm->tm_wday;
    scala_tm->tm_yday = tm->tm_yday;
    scala_tm->tm_isdst = tm->tm_isdst;
}

static void tm_init(struct tm *tm, struct scalanative_tm *scala_tm) {
    *tm = (struct tm){0}; // zero everything to avoid garbage
    tm->tm_sec = scala_tm->tm_sec;
    tm->tm_min = scala_tm->tm_min;
    tm->tm_hour = scala_tm->tm_hour;
    tm->tm_mday = scala_tm->tm_mday;
    tm->tm_mon = scala_tm->tm_mon;
    tm->tm_year = scala_tm->tm_year;
    tm->tm_wday = scala_tm->tm_wday;
    tm->tm_yday = scala_tm->tm_yday;
    tm->tm_isdst = scala_tm->tm_isdst;
    // On BSD-like systems or with glibc sizeof(tm) is greater than
    // sizeof(scalanative_tm), so contents of rest of tm is left undefined.
    // asctime, asctime_r, mktime, gmtime, & gmtime_r are robust to this.
    // strftime is _NOT_ and must zero the excess fields itself.
}

errno_t scalanative_asctime_s(struct scalanative_tm *scala_tm, size_t size,
                              char *buf) {
    struct tm tm;
    tm_init(&tm, scala_tm);
    return asctime_s(buf, size, &tm);
}

char *scalanative_asctime(struct scalanative_tm *scala_tm) {
    struct tm tm;
    tm_init(&tm, scala_tm);
    return asctime(&tm);
}

struct scalanative_tm *scalanative_gmtime_s(const time_t *clock,
                                            struct scalanative_tm *result) {
    struct tm tm;
    gmtime_s(&tm, clock);
    scalanative_tm_init(result, &tm);
    return result;
}

struct scalanative_tm *scalanative_gmtime(const time_t *clock) {
    return scalanative_gmtime_s(clock, &scalanative_shared_tm_buf);
}

struct scalanative_tm *scalanative_localtime_s(const time_t *clock,
                                               struct scalanative_tm *result) {
    struct tm tm;
    localtime_s(&tm, clock);
    scalanative_tm_init(result, &tm);
    return result;
}

struct scalanative_tm *scalanative_localtime(const time_t *clock) {
    // Calling localtime() ensures that tzset() has been called.
    scalanative_tm_init(&scalanative_shared_tm_buf, localtime(clock));
    return &scalanative_shared_tm_buf;
}

time_t scalanative_mktime(struct scalanative_tm *result) {
    struct tm tm;
    tm_init(&tm, result);
    return mktime(&tm);
}

size_t scalanative_strftime(char *buf, size_t maxsize, const char *format,
                            struct scalanative_tm *scala_tm) {

    // The operating system struct tm can be larger than
    // the scalanative tm.  On 64 bit GNU or _BSD_SOURCE Linux this
    // usually is true and beyond easy control.
    //
    // Clear any fields not known to scalanative, such as tm_zone,
    // so they are zero/NULL, not J-Random garbage.
    // strftime() in Scala Native release mode is particularly sensitive
    // to garbage beyond the end of the scalanative tm.

    // Initializing all of tm when part of it will be immediately overwritten
    // is _slightly_ inefficient but short, simple, and easy to get right.

    struct tm tm = {0};
    tm_init(&tm, scala_tm);
    return strftime(buf, maxsize, format, &tm);
}

char **scalanative_tzname() { return tzname; }
long scalanative_timezone() { return timezone; }
int scalanative_daylight() { return daylight; }
#endif // defined(_WIN32)
