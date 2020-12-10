// date.c - helper functions for javalib java.util.Date class

#include <stddef.h>
#include <time.h>

char *scalanative_ju_secondsToLocaltime(const time_t *seconds,
                                        const char *format,
                                        const char *fallback, char *buf,
                                        size_t maxsize) {
    char *result = (char *)fallback;

    if (maxsize > 0) {
        struct tm tm;

        tzset();
        struct tm *tmPtr = localtime_r(seconds, &tm);

        if (tmPtr != NULL) {
            int n = strftime(buf, maxsize, format, tmPtr);
            if (n > 0) {
                result = buf; // only on known success.
            }
        }
    }

    return result;
}
