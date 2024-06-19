#if defined(SCALANATIVE_COMPILE_ALWAYS) ||                                     \
    defined(__SCALANATIVE_JAVALIB_SYS_LINUX_SCHED_H) && defined(__linux__)

/* Define _GNU_SOURCE to make the CPU_* macros available.
 * This file is already GNU Linux specific, so adding more GNU does not
 * add impurity. What is a bit of GNU amoung friends?
 *
 * Those macros greatly ease this implementation.
 */
#define _GNU_SOURCE
#include <sched.h>

int scalanative_sched_cpuset_cardinality() {

    /* On error, return -1. Let caller handle errors.
     *
     * On success, return the number of logical processors available to
     * the process. Due to taskset, cpuset, and such, this may be
     * less than sysconf number of online logical processes
     * (_SC_NPROCESSORS_ONLN) which may be less than the total number of
     * logical processes configured for the system (_SC_NPROCESSORS).
     *
     * Note that OpenMP environment variables are neither consulted nor used.
     * This consistent with Java.
     *
     * Large systems:
     *   This implementation will return -1 if the number of logical processors
     *   available to the process is greater than sizeof[cpu_set_t]
     *   (CPU_SETSIZE == 1024, circa 2024). The caller will probably fall
     *   back to reporting sysconf(_SC_NPROCESSORS_ONLN) as a best guess.
     *
     *   Someday there could be a fallback "slow" path which
     *   tried used CPU_*S macros to dynamically try larger sizes.
     *   Send me a CPU with that many logical processors with a large check to
     *   cover the electricity and air conditioning and I will code that
     *   up right away.
     */

    cpu_set_t cpuset;

    CPU_ZERO(&cpuset);

    int status = sched_getaffinity(0, sizeof(cpu_set_t), &cpuset);

    return (status < 0) ? -1 : CPU_COUNT(&cpuset);
}

#endif // __linux__
