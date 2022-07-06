#if defined(_WIN32)
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#else
#include <time.h>
#if defined(__APPLE__)
#if (__MAC_OS_X_VERSION_MIN_REQUIRED <= __MAC_10_12)
#error "macOS version must be greater than 10.12"
#endif
#endif // defined(__APPLE__)
#endif // defined(_WIN32)

long long scalanative_nano_time() {
    long long nano_time;
#define NANOSECONDS_PER_SECOND 1000000000

#if defined(_WIN32)
    LARGE_INTEGER count;
    LARGE_INTEGER freq;
    QueryPerformanceCounter(&count);
    QueryPerformanceFrequency(&freq);
    int nanosPerCount = NANOSECONDS_PER_SECOND / freq.QuadPart;
    nano_time = count.QuadPart * nanosPerCount;
#else
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC_RAW, &ts);
    nano_time = ts.tv_sec * NANOSECONDS_PER_SECOND + ts.tv_nsec;
#endif

    return nano_time;
}