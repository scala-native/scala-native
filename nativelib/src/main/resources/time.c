#include <stdio.h>
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

long long scalanative_current_time_millis() {
    long long current_time_millis;

#define MILLIS_PER_SEC 1000LL
#define MICROS_PER_MILLI 1000LL

    struct timeval tv;
    gettimeofday(&tv, NULL);
    current_time_millis =
        tv.tv_sec * MILLIS_PER_SEC + tv.tv_usec / MICROS_PER_MILLI;

#undef MILLIS_PER_SEC
#undef MICROS_PER_MILLI

    return current_time_millis;
}

char **scalanative_tzname() { return tzname; }

long scalanative_timezone() { return timezone; }

int scalanative_daylight() { return daylight; }
