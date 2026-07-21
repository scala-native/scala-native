// Time utilities for GC - uses monotonic clocks for elapsed time measurement

#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX) ||         \
    defined(SCALANATIVE_GC_BOEHM)

#include "Time.h"
#include <time.h>

#if defined(_WIN32)
#define WIN32_LEAN_AND_MEAN
#include <windows.h>

// Cached frequency value for QueryPerformanceCounter
static int winFreqQuadPart(int *quad) {
    static int winFreqQuadPartValue = 0;
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
#endif

long long Time_current_millis(void) {
#if defined(_WIN32)
    // GetTickCount64 returns monotonic time in milliseconds
    return (long long)GetTickCount64();
#else
    // Use CLOCK_MONOTONIC for elapsed time measurement
    struct timespec ts = {};
    if (clock_gettime(CLOCK_MONOTONIC, &ts) == 0) {
        return (long long)ts.tv_sec * 1000 + (long long)ts.tv_nsec / 1000000;
    }
    return 0;
#endif
}

long long Time_current_nanos(void) {
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

#endif // defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX) ||
       // defined(SCALANATIVE_GC_BOEHM)
