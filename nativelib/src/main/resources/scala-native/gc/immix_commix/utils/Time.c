#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX)

#include "Time.h"
#include <time.h>
#if defined(_WIN32)
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>

static int winFreqQuadPartValue = 0;
static int winFreqQuadPart(int *quad) {
    int retval = 1; // assume ok for caching
    // check if cache is set
    if (winFreqQuadPartValue == 0) {
        LARGE_INTEGER freq;
        retval = QueryPerformanceFrequency(&freq);
        if (retval != 0) {
            // set cache value
            winFreqQuadPartValue = freq.QuadPart;
        }
    }
    // assign cache value or default 0 on failure
    *quad = winFreqQuadPartValue;

    return retval;
}
#else
#include <sys/time.h>
#endif

long long Time_current_millis() {
    long long current_time_millis = 0LL;
#define NANOS_PER_MILLI 1000000LL

#if defined(_WIN32)
    // Windows epoch is January 1, 1601 (start of Gregorian calendar cycle)
    // Unix epoch is January 1, 1970 (adjustment in "ticks" 100 nanosecond)
#define UNIX_TIME_START 0x019DB1DED53E8000LL
#define NANOS_PER_SEC 1000000000LL

    FILETIME filetime;
    int quad;
    // returns ticks in UTC - no return value
    GetSystemTimeAsFileTime(&filetime);
    if (winFreqQuadPart(&quad) != 0) {
        int ticksPerMilli = NANOS_PER_MILLI / (NANOS_PER_SEC / quad);

        // Copy the low and high parts of FILETIME into a LARGE_INTEGER
        // This is so we can access the full 64-bits as an Int64 without
        // causing an alignment fault
        LARGE_INTEGER li;
        li.LowPart = filetime.dwLowDateTime;
        li.HighPart = filetime.dwHighDateTime;

        current_time_millis = (li.QuadPart - UNIX_TIME_START) / ticksPerMilli;
    }
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

long long Time_current_nanos() {
    long long nano_time = 0LL;
#define NANOS_PER_SEC 1000000000LL

#if defined(_WIN32)
    // return value of 0 is failure
    LARGE_INTEGER count;
    int quad;
    if (QueryPerformanceCounter(&count) != 0) {
        if (winFreqQuadPart(&quad) != 0) {
            int nanosPerCount = NANOS_PER_SEC / quad;
            nano_time = count.QuadPart * nanosPerCount;
        }
    }
#else
#if defined(__FreeBSD__)
    int clock = CLOCK_MONOTONIC_PRECISE; // OS has no CLOCK_MONOTONIC_RAW
#elif defined(__OpenBSD__) || defined(__NetBSD__)
    int clock = CLOCK_MONOTONIC; // OpenBSD and NetBSD has only CLOCK_MONOTONIC
#else  // Linux, macOS
    int clock = CLOCK_MONOTONIC_RAW;
#endif // !FreeBSD || !OpenBSD

    // return value of 0 is success
    struct timespec ts;
    if (clock_gettime(clock, &ts) == 0) {
        nano_time = (ts.tv_sec * NANOS_PER_SEC) + ts.tv_nsec;
    }
#endif // !_WIN32
    return nano_time;
}

#endif // defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX)
