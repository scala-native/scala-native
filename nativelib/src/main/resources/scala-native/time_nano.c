#if defined(_WIN32)
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#include "win_freq.h"
#else
#include <time.h>
#endif // defined(_WIN32)

/**
 * Refer to javadoc for System.nanoTime()

 * Note: For UNIX based systems this uses CLOCK_MONOTONIC_RAW which
 * has no NTP adjustments to match how Windows works. Systems tested
 * have this non-standard feature but CLOCK_MONOTONIC would need
 * to be used otherwise, perhaps with a conditional compilation
 * block.
 *
 * Failure is unlikely and there currently is no consensus on handling
 * failure by the caller.
 *
 * @return nanoseconds of uptime - 0 if it fails
 */
long long scalanative_nano_time() {
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
#else  // Linux, macOS
    int clock = CLOCK_MONOTONIC_RAW;
#endif // !FreeBSD

    // return value of 0 is success
    struct timespec ts;
    if (clock_gettime(clock, &ts) == 0) {
        nano_time = (ts.tv_sec * NANOS_PER_SEC) + ts.tv_nsec;
    }
#endif // !_WIN32
    return nano_time;
}
