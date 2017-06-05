#ifdef _WIN32

#include "os_win_time.h"

/*
*  The code below with modifications was taken from:
*  https://gist.github.com/ikhramts/717651/6104436a367667220432ec3a4993d9e9c7fcfd60
*
*/

#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#include <windows.h>
#include <time.h>

#if defined(_MSC_VER) || defined(_MSC_EXTENSIONS)
#define DELTA_EPOCH_IN_MICROSECS 11644473600000000Ui64
#else
#define DELTA_EPOCH_IN_MICROSECS 11644473600000000ULL
#endif

extern "C" int gettimeofday(struct timeval *tv, struct timezone *tz) {
    FILETIME ft;
    unsigned __int64 tmpres = 0;
    static int tzflag = 0;

    if (NULL != tv) {
        GetSystemTimeAsFileTime(&ft);

        tmpres |= ft.dwHighDateTime;
        tmpres <<= 32;
        tmpres |= ft.dwLowDateTime;

        tmpres /= 10; /*convert into microseconds*/
        /*converting file time to unix epoch*/
        tmpres -= DELTA_EPOCH_IN_MICROSECS;
        tv->tv_sec = (long)(tmpres / 1000000UL);
        tv->tv_usec = (long)(tmpres % 1000000UL);
    }

    if (NULL != tz) {
        if (!tzflag) {
            _tzset();
            tzflag++;
        }
        long timezone;
        _get_timezone(&timezone);
        tz->tz_minuteswest = timezone / 60;
        _get_daylight(&tz->tz_dsttime);
    }

    return 0;
}

#endif /* !_TIME_H_ */
