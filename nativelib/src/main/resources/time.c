#include <time.h>
#include <sys/time.h>
#include <stdio.h>

#ifdef __MACH__
#include <mach/clock.h>
#include <mach/mach.h>
#endif

// https://gist.github.com/jbenet/1087739
long long scalanative_nano_time() {
	long long nano_time;

#define NANOSECONDS_PER_SECOND 1000000000LL

#ifdef __MACH__ // OS X does not have clock_gettime, use clock_get_time
	clock_serv_t cclock;
	mach_timespec_t mts;
	host_get_clock_service(mach_host_self(), CALENDAR_CLOCK, &cclock);
	clock_get_time(cclock, &mts);
	mach_port_deallocate(mach_task_self(), cclock);
	nano_time = mts.tv_sec * NANOSECONDS_PER_SECOND + mts.tv_nsec;
#else
	struct timespec ts;
	clock_gettime(CLOCK_MONOTONIC, &ts);
	nano_time = ts.tv_sec * NANOSECONDS_PER_SECOND + ts.tv_nsec;
#endif

#undef NANOSECONDS_PER_SECOND

  return nano_time;
}
