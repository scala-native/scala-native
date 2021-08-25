#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
// X/Open System Interfaces (XSI), also sets _POSIX_C_SOURCE.
// Partial, but useful, implementation of X/Open 7, incorporating Posix 2008.

#define _XOPEN_SOURCE 700

#include <string.h>
#include <sys/time.h>
#include <time.h>

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

char *scalanative_asctime_r(struct scalanative_tm *scala_tm, char *buf) {
    struct tm tm;
    tm_init(&tm, scala_tm);
    return asctime_r(&tm, buf);
}

char *scalanative_asctime(struct scalanative_tm *scala_tm) {
    struct tm tm;
    tm_init(&tm, scala_tm);
    return asctime(&tm);
}

struct scalanative_tm *scalanative_gmtime_r(const time_t *clock,
                                            struct scalanative_tm *result) {
    struct tm tm;
    gmtime_r(clock, &tm);
    scalanative_tm_init(result, &tm);
    return result;
}

struct scalanative_tm *scalanative_gmtime(const time_t *clock) {
    return scalanative_gmtime_r(clock, &scalanative_shared_tm_buf);
}

struct scalanative_tm *scalanative_localtime_r(const time_t *clock,
                                               struct scalanative_tm *result) {
    struct tm tm;
    localtime_r(clock, &tm);
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

// XSI
char *scalanative_strptime(const char *s, const char *format,
                           struct scalanative_tm *scala_tm) {
    // Note Well:
    //
    // Reference: "The Open Group Base Specifications Issue 7, 2018 edition".
    // A long comment for a deceptively complicated standard and implementation
    // thereof.
    //
    // 1) Hazard Alert! Booby trap ahead.
    //
    //    Only the fields in the "scalanative_tm" argument with explicit
    //    conversion specifiers in the format argument are reliably
    //    and portably set. Other fields may or may not be written.
    //
    //    The "APPLICATION USAGE" section of the specification says
    //    that the contents of a second call to this method with the
    //    same "struct tm" are unspecified (implementation dependent).
    //    The "struct tm" may be updated (leaving some fields untouched)
    //    or completely overwritten. If the structure is overwritten,
    //    the value used to overwrite fields not in the format is
    //    also specified.
    //
    //    The implies, but does not state, that the value of fields
    //    not in the format may stay the same or change.
    //
    //    There is no specifier for the is_dst field. The non-binding example
    //    describes that field as not set by strptime(). This supports, but
    //    does not specify, the idea that fields not in the format are
    //    untouched. Caveat Utilitor (user beware)!
    //
    //
    // 2) This implementation is slightly nonconforming, but useful,
    //    in that the format argument is passed directly to the underlying
    //    libc. This means that conversions specifiers such as "%Z"
    //    supported by Posix strftime(), glibc, and macOS will will not
    //    be reported as parse errors at this level.

    struct tm tm;

    char *result = strptime(s, format, &tm);
    scalanative_tm_init(scala_tm, &tm);
    return result;
}

char **scalanative_tzname() { return tzname; }

// XSI
long scalanative_timezone() {
#if defined(__FreeBSD__)
    return 0;
#else
    return timezone;
#endif
}

// XSI
int scalanative_daylight() {
#if defined(__FreeBSD__)
    return 0;
#else
    return daylight;
#endif
}

#endif // Unix or Mac OS
