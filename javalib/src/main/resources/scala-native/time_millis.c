#include <time.h>
#if defined(_WIN32)
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#else
#include <stdio.h>
#include <sys/time.h>
#endif

/**
 * Refer to javadoc for System.currentTimeMillis()
 *
 * Failure is unlikely and there currently is no consensus on handling the
 * failure.
 *
 * @return milliseconds from the UNIX epoch - 0 if it fails
 */
long long scalanative_current_time_millis() {
    long long current_time_millis = 0LL;
#define NANOS_PER_MILLI 1000000LL

#if defined(_WIN32)
    // Windows epoch is January 1, 1601 (start of Gregorian calendar cycle)
    // Unix epoch is January 1, 1970 (adjustment in "ticks" 100 nanosecond)
#define UNIX_TIME_START 0x019DB1DED53E8000LL
    // FILETIME ticks are always 100 ns; do not use QueryPerformanceFrequency
    // (its value is platform-dependent and breaks the conversion on WoA).
#define FILETIME_TICKS_PER_MILLI 10000LL

    FILETIME filetime;
    // returns ticks in UTC - no return value
    GetSystemTimeAsFileTime(&filetime);

    // Copy the low and high parts of FILETIME into a LARGE_INTEGER
    // This is so we can access the full 64-bits as an Int64 without
    // causing an alignment fault
    LARGE_INTEGER li;
    li.LowPart = filetime.dwLowDateTime;
    li.HighPart = filetime.dwHighDateTime;

    current_time_millis =
        (li.QuadPart - UNIX_TIME_START) / FILETIME_TICKS_PER_MILLI;
#else
#define MILLIS_PER_SEC 1000LL

    struct timespec ts;
    if (clock_gettime(CLOCK_REALTIME, &ts) == 0) {
        current_time_millis =
            (ts.tv_sec * MILLIS_PER_SEC) + (ts.tv_nsec / NANOS_PER_MILLI);
    }
#endif
    return current_time_millis;
}
