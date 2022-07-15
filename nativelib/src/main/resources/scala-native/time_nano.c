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

/**
 * Refer to javadoc for System.nanoTime()
 *
 * Function return values are ignored. Failure is unlikely and
 * there currently is no consensus on handling the failure.
 *
 * @return nanoseconds of uptime - presumably 0 if it fails
 */
long long scalanative_nano_time() {
    long long nano_time;
#define NANOS_PER_SEC 1000000000LL

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
