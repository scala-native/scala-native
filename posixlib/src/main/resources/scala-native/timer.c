#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_TIMER)
#if (defined(__unix__) || defined(__unix) || defined(unix)) &&                 \
    !defined(__OpenBSD__)
#define _XOPEN_SOURCE 700
#include <time.h>

/* Kept out of time.c: timer_* need librt on some systems, so only programs
 * using posix.timer reference them. Calls the header-selected symbols,
 * e.g. NetBSD's __timer_gettime50.
 */
int scalanative_timer_gettime(timer_t timerid, struct itimerspec *curr_value) {
    return timer_gettime(timerid, curr_value);
}

int scalanative_timer_settime(timer_t timerid, int flags,
                              const struct itimerspec *new_value,
                              struct itimerspec *old_value) {
    return timer_settime(timerid, flags, new_value, old_value);
}

#endif
#endif
