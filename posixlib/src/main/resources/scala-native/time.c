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

static struct scalanative_tm scalanative_gmtime_buf;
static struct scalanative_tm scalanative_localtime_buf;

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

    if (sizeof(struct tm) > sizeof(struct scalanative_tm)) {
      // The operating system struct tm can be larger than
      // the scalanative tm.  On 64 bit GNU or _BSD_SOURCE Linux this
      // usually is true and beyond easy control.
      //
      // Clear any fields not known to scalanative, such as tm_zone,
      // so they are zero/NULL, not J-Random garbage.
      // strftime() in Scala Native release mode is particularly sensitive
      // to garbage beyond the end of the scalanative tm.
      // Assume all excess size is at bottom of C tm, not internal padding.

      char *start = (char *) tm + sizeof(struct scalanative_tm);
      size_t count = sizeof(struct tm) - sizeof(struct scalanative_tm);
      memset(start, 0, count);
    }
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
    return scalanative_gmtime_r(clock, &scalanative_gmtime_buf);
}

struct scalanative_tm *scalanative_localtime_r(const time_t *clock,
                                               struct scalanative_tm *result) {
    struct tm tm;
    localtime_r(clock, &tm);
    scalanative_tm_init(result, &tm);
    return result;
}

struct scalanative_tm *scalanative_localtime(const time_t *clock) {
    return scalanative_localtime_r(clock, &scalanative_localtime_buf);
}

time_t scalanative_mktime(struct scalanative_tm *result) {
    struct tm tm;
    tm_init(&tm, result);
    return mktime(&tm);
}

size_t scalanative_strftime(char *buf, size_t maxsize, const char *format,
                            struct scalanative_tm *scala_tm) {
    struct tm tm;
    tm_init(&tm, scala_tm);
    return strftime(buf, maxsize, format, &tm);
}

// XSI
char *scalanative_strptime(const char *s, const char *format,
                           struct scalanative_tm *scala_tm) {
  // strptime is known to not set is_dst field for %Z format.
  // Take runtime hit of clearing entire structure to be robust
  // to that and undiscovered corner cases where strptime does not fill
  // a field under some condition.
    struct tm tm = {0};

    char *result = strptime(s, format, &tm);
    scalanative_tm_init(scala_tm, &tm);
    return result;
}

char **scalanative_tzname() { return tzname; }

// XSI
long scalanative_timezone() { return timezone; }

// XSI
int scalanative_daylight() { return daylight; }
