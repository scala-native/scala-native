#include <time.h>
#if defined(_WIN32)
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#include "win_freq.h"
#else
#include <stdio.h>
#include <sys/time.h>
#endif

/**
 * Refer to javadoc for System.currentTimeMillis()
 *
 * Function return values are ignored. Failure is unlikely and
 * there currently is no consensus on handling the failure.
 *
 * @return milliseconds from the UNIX epoch - presumably 0 if it fails
 */
long long scalanative_current_time_millis() {
    long long current_time_millis;
#define NANOS_PER_MILLI 1000000LL

#if defined(_WIN32)
    // January 1, 1970 (start of Unix epoch) in "ticks"
#define UNIX_TIME_START 0x019DB1DED53E8000LL
#define NANOS_PER_SEC 1000000000LL

    FILETIME filetime;
    GetSystemTimeAsFileTime(&filetime); // returns ticks in UTC

    int ticksPerMilli = NANOS_PER_MILLI / (NANOS_PER_SEC / winFreqQuadPart());

    // Copy the low and high parts of FILETIME into a LARGE_INTEGER
    // This is so we can access the full 64-bits as an Int64 without causing
    // an alignment fault
    LARGE_INTEGER li;
    li.LowPart = filetime.dwLowDateTime;
    li.HighPart = filetime.dwHighDateTime;

    current_time_millis = (li.QuadPart - UNIX_TIME_START) / ticksPerMilli;
#else
#define MILLIS_PER_SEC 1000LL

    struct timespec ts;
    clock_gettime(CLOCK_REALTIME, &ts);
    current_time_millis =
        (ts.tv_sec * MILLIS_PER_SEC) + (ts.tv_nsec / NANOS_PER_MILLI);
#endif
    return current_time_millis;
}
