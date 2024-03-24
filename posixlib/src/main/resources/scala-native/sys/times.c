
#if !defined(_WIN32) && defined(SCALANATIVE_COMPILE_ALWAYS) ||                 \
    defined(__SCALANATIVE_POSIX_SYS_TIMES)
#if !(defined __STDC_VERSION__) || (__STDC_VERSION__ < 201112L)
#ifndef SCALANATIVE_SUPPRESS_STRUCT_CHECK_WARNING
#warning "Size and order of C structures are not checked when -std < c11."
#endif
#else // POSIX
#include <stddef.h>
#include <sys/times.h>

// 2023-01-03 FIXME -- need to fuss with timesOps in times.scala
// 2023-01-03 FIXME -- need to explain here the useful lie.

#if !defined(__FreeBSD__) && !defined(__NetBSD__)
// C long will mirror machine architecture: 64 bits or 32 bit.
typedef long scalanative_clock_t;
#else // __FreeBSD
// See comments in corresponding times.scala.
/* There is a bit of "person behind the curtain" "sufficiently advance
 * technology" magic happening here.
 *
 * Using the names in timesOps below is recommended on both 32 & 64 bit
 * architectures. On FreeBSD 64 bit machines using timeOps names rather than
 * the _N idiom is required in order to extract correct & proper 32 bit values.
 */
#import <sys/types.h>
typedef __int32_t scalanative_clock_t;
#endif // __FreeBSD__ || __NetBSD__

struct scalanative_tms {
    scalanative_clock_t tms_utime;  // User CPU time
    scalanative_clock_t tms_stime;  // System CPU time
    scalanative_clock_t tms_cutime; // User CPU time terminated children
    scalanative_clock_t tms_cstime; // System CPU time of terminated children
};

_Static_assert(sizeof(struct scalanative_tms) <= sizeof(struct tms),
               "size mismatch: scalanative_tms");

_Static_assert(offsetof(struct scalanative_tms, tms_utime) ==
                   offsetof(struct tms, tms_utime),
               "offset mismatch: tms tms_utime");

_Static_assert(offsetof(struct scalanative_tms, tms_stime) ==
                   offsetof(struct tms, tms_stime),
               "offset mismatch: tms tms_stime");

_Static_assert(offsetof(struct scalanative_tms, tms_cutime) ==
                   offsetof(struct tms, tms_cutime),
               "offset mismatch: tms tms_cutime");

_Static_assert(offsetof(struct scalanative_tms, tms_cstime) ==
                   offsetof(struct tms, tms_cstime),
               "offset mismatch: tms tms_cstime");
#endif // POSIX
#endif // ! _WIN32
