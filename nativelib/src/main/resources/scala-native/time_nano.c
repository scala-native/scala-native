#if defined(_WIN32)
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#include "win_freq.h"
#else
#include <time.h>
#if defined(__APPLE__)
#if (__MAC_OS_X_VERSION_MIN_REQUIRED < __MAC_10_12)
#error "macOS version must be 10.12 or greater"
#endif
#endif // defined(__APPLE__)
#endif // defined(_WIN32)

/* Refer to javadoc for System.nanoTime() */
long long scalanative_nano_time() {
    long long nano_time;
#define NANOS_PER_SEC 1000000000

#if defined(_WIN32)
    LARGE_INTEGER count;
    QueryPerformanceCounter(&count);
    int nanosPerCount = NANOS_PER_SEC / winFreqQuadPart();
    nano_time = count.QuadPart * nanosPerCount;
#else
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC_RAW, &ts);
    nano_time = (ts.tv_sec * NANOS_PER_SEC) + ts.tv_nsec;
#endif

    return nano_time;
}