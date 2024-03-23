#if defined(__SCALANATIVE_POSIX_TIME) && !defined(_WIN32)

// X/Open System Interfaces (XSI), also sets _POSIX_C_SOURCE.
// Partial, but useful, implementation of X/Open 7, incorporating Posix 2008.

#define _XOPEN_SOURCE 700

#include <stdbool.h>
#include <stddef.h>

#include <errno.h>
#include <string.h>
#include <signal.h>
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

struct scalanative_timespec {
    long tv_sec;
    long tv_nsec;
};

struct scalanative_itimerspec {
    struct scalanative_timespec it_interval;
    struct scalanative_timespec it_value;
};

#if !(defined __STDC_VERSION__) || (__STDC_VERSION__ < 201112L)
#ifndef SCALANATIVE_SUPPRESS_STRUCT_CHECK_WARNING
#warning "Size and order of C structures are not checked when -std < c11."
#endif
#else

// struct tm
_Static_assert(sizeof(struct scalanative_tm) <= sizeof(struct tm),
               "Unexpected size: struct tm");

_Static_assert(offsetof(struct scalanative_tm, tm_sec) ==
                   offsetof(struct tm, tm_sec),
               "offset mismatch: tm.tm_sec");

_Static_assert(offsetof(struct scalanative_tm, tm_min) ==
                   offsetof(struct tm, tm_min),
               "offset mismatch: tm.tm_min");

_Static_assert(offsetof(struct scalanative_tm, tm_hour) ==
                   offsetof(struct tm, tm_hour),
               "offset mismatch: tm.tm_hour");

_Static_assert(offsetof(struct scalanative_tm, tm_mday) ==
                   offsetof(struct tm, tm_mday),
               "offset mismatch: tm.tm_mday");

_Static_assert(offsetof(struct scalanative_tm, tm_mon) ==
                   offsetof(struct tm, tm_mon),
               "offset mismatch: tm.tm_mon");

_Static_assert(offsetof(struct scalanative_tm, tm_year) ==
                   offsetof(struct tm, tm_year),
               "offset mismatch: tm.tm_year");

_Static_assert(offsetof(struct scalanative_tm, tm_wday) ==
                   offsetof(struct tm, tm_wday),
               "offset mismatch: tm.tm_wday");

_Static_assert(offsetof(struct scalanative_tm, tm_yday) ==
                   offsetof(struct tm, tm_yday),
               "offset mismatch: tm.tm_yday");

_Static_assert(offsetof(struct scalanative_tm, tm_isdst) ==
                   offsetof(struct tm, tm_isdst),
               "offset mismatch: tm.tm_isdst");

// struct timespec
_Static_assert(sizeof(struct scalanative_timespec) == sizeof(struct timespec),
               "Unexpected size: struct timespec");

_Static_assert(offsetof(struct scalanative_timespec, tv_sec) ==
                   offsetof(struct timespec, tv_sec),
               "offset mismatch: timespec.tv_sec");

_Static_assert(offsetof(struct scalanative_timespec, tv_nsec) ==
                   offsetof(struct timespec, tv_nsec),
               "offset mismatch: timespec.tv_nsec");

// struct itimer

#if !defined(__APPLE__) // no itimer on Apple
_Static_assert(sizeof(struct scalanative_itimerspec) ==
                   sizeof(struct itimerspec),
               "Unexpected size: struct itimer");

_Static_assert(offsetof(struct scalanative_itimerspec, it_interval) ==
                   offsetof(struct itimerspec, it_interval),
               "offset mismatch: itimer.it_interval");

_Static_assert(offsetof(struct scalanative_itimerspec, it_value) ==
                   offsetof(struct itimerspec, it_value),
               "offset mismatch: itimer.it_value");
#endif
#endif // __STDC_VERSION__

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

int scalanative_clock_nanosleep(clockid_t clockid, int flags,
                                struct timespec *request,
                                struct timespec *remain) {
#if !defined(__APPLE__) && !defined(__OpenBSD__)
    return clock_nanosleep(clockid, flags, request, remain);
#else
    errno = ENOTSUP; // No clock_nanosleep() on Apple or OpenBSD.
    return ENOTSUP;
#endif
}

size_t scalanative_strftime(char *buf, size_t maxsize, const char *format,
                            struct scalanative_tm *scala_tm) {

    /* The operating system struct tm can be larger than
     * the scalanative tm.  On 64 bit GNU or _BSD_SOURCE Linux this
     * usually is true and beyond easy control.
     *
     * The C designated initializer idiom will clear any fields not explicitly
     * named, such as tm_zone and anything beyond the end of scalanative tm.
     *
     * strftime() in Scala Native release mode is particularly sensitive
     * to garbage beyond the end of the scalanative tm.
     */
    struct tm tm = {.tm_sec = scala_tm->tm_sec,
                    .tm_min = scala_tm->tm_min,
                    .tm_hour = scala_tm->tm_hour,
                    .tm_mday = scala_tm->tm_mday,
                    .tm_mon = scala_tm->tm_mon,
                    .tm_year = scala_tm->tm_year,
                    .tm_wday = scala_tm->tm_wday,
                    .tm_yday = scala_tm->tm_yday,
                    .tm_isdst = scala_tm->tm_isdst};

    return strftime(buf, maxsize, format, &tm);
}

// XSI
char *scalanative_strptime(const char *s, const char *format,
                           struct scalanative_tm *scala_tm) {
    /* Note Well:
     *
     * Reference: "The Open Group Base Specifications Issue 7, 2018 edition".
     * A long comment for a deceptively complicated standard and implementation
     * thereof.
     *
     * 1) Hazard Alert! Booby trap ahead.
     *
     *    Only the fields in the "scalanative_tm" argument with explicit
     *    conversion specifiers in the format argument are reliably
     *    and portably set. Other fields may or may not be written.
     *
     *    The "APPLICATION USAGE" section of the specification says
     *    that the contents of a second call to this method with the
     *    same "struct tm" are unspecified (implementation dependent).
     *    The "struct tm" may be updated (leaving some fields untouched)
     *    or completely overwritten. If the structure is overwritten,
     *    the value used to overwrite fields not in the format is
     *    also specified.
     *
     *    The implies, but does not state, that the value of fields
     *    not in the format may stay the same or change.
     *
     *    There is no specifier for the is_dst field. The non-binding example
     *    describes that field as not set by strptime(). This supports, but
     *    does not specify, the idea that fields not in the format are
     *    untouched. Caveat Utilitor (user beware)!
     *
     *
     * 2) This implementation is slightly nonconforming, but useful,
     *    in that the format argument is passed directly to the underlying
     *    libc. This means that conversions specifiers such as "%Z"
     *    supported by Posix strftime(), glibc, and macOS will will not
     *    be reported as parse errors at this level.
     */

    /* tm and the copy back to scala_tm are needed because strptime()
     * could and does write beyond the end of a scala_tm.
     * Initialize all of tm to zero. What strptime() does with fields not in
     * the format may be implementation dependent.
     */

    struct tm tm = {0};

    char *result = strptime(s, format, &tm);
    scalanative_tm_init(scala_tm, &tm);
    return result;
}

char **scalanative_tzname() { return tzname; }

#if defined(__FreeBSD__)

/* The synthesized 'timezone' and 'daylight' may or may not match
 * the behavior of Linux/macOS when tzset() has not been previouly call.
 * Only acceptance tests are likely to notice the difference.
 *
 * Linux reports 'timezone' as zero and both tzname[0] and tzname[1]
 * as 'GMT'.
 *
 * Depending on if daylight savings are in effect and the behavior
 * of localtime_r(), the synthesized 'timezone' can match or not.
 * Calling when daylight saving time is not in effect should yield a
 * match. Calling when daylight saving time is in effect should act
 * as if tzset() had been called. In the latter case, mktime() eventually
 * gets called, and that acts as if it calls tzset().
 *
 * Usually, not having called tzset, implicitly or explicitly, is a
 * bug and giving the 'expected', after tzset(), result is better for
 * the user.
 */

// Rely upon FreeBSD tm_isdst to be more reliable than POSIX version.

static bool find_offset(int year, int month, struct tm *tm) {
    tm->tm_year = year;
    tm->tm_mon = month;

    time_t t = mktime(tm);
    localtime_r(&t, tm);

    return tm->tm_isdst == 0;
}

static long synthesize_timezone() {
    // An expensive routine, especially if called frequently.

    long tm_gmtoff = 0;
    time_t t = time(NULL);
    struct tm lt = {0};

    localtime_r(&t, &lt);

    if (lt.tm_isdst == 0) { // It is Standard time now.
        tm_gmtoff = lt.tm_gmtoff;
    } else {
        struct tm tmJan = {0};

        // Is Standard time in January?
        if (find_offset(lt.tm_year, 0, &tmJan)) {
            tm_gmtoff = tmJan.tm_gmtoff;
        } else {
            struct tm tmJuly = {0};

            // Current location must be south of Equator.
            if (find_offset(lt.tm_year, 6, &tmJuly)) {
                tm_gmtoff = tmJuly.tm_gmtoff;
            }
        }
    }
    // Posix 'timezone' is + WEST of prime meridian, tm_gmtoff is + EAST.
    return -tm_gmtoff;
}
#endif

// XSI
long scalanative_timezone() {
#if !defined(__FreeBSD__)
    return timezone;
#else
    return synthesize_timezone();
#endif
}

// XSI
int scalanative_daylight() {
#if !defined(__FreeBSD__)
    return daylight;
#else
    return (tzname[0] != NULL) && (tzname[1] != NULL) &&
           (strcmp(tzname[0], tzname[1]) != 0);
#endif
}

// Symbolic constants

int scalanative_clock_monotonic() { return CLOCK_MONOTONIC; }
int scalanative_clock_process_cputime_id() { return CLOCK_PROCESS_CPUTIME_ID; }
int scalanative_clock_realtime() { return CLOCK_REALTIME; }
int scalanative_clock_thread_cputime_id() { return CLOCK_THREAD_CPUTIME_ID; }

int scalanative_timer_abstime() {
#if !defined(__APPLE__)
    return TIMER_ABSTIME;
#else
    return 1; // Fake it, using value "known" on some systems.
#endif
}

#endif // Unix or Mac OS
