#ifndef _TIME_H_
#define _TIME_H_

#include <time.h>

#ifdef __cplusplus
extern "C" {
#endif

struct timeval {
    time_t tv_sec; /* seconds */
    int tv_usec;   /* microseconds */
};

struct timezone {
    int tz_minuteswest; /* minutes W of Greenwich */
    int tz_dsttime;     /* type of dst correction */
};

const int CLOCK_MONOTONIC = 0;

int clock_gettime(int X, struct timespec *tv);

int gettimeofday(struct timeval *tv, struct timezone *tz);

#ifdef __cplusplus
}
#endif

#endif /* !_TIME_H_ */
