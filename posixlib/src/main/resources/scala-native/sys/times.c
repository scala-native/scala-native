#ifdef _WIN32
// No Windows support
#else
#if !(defined __STDC_VERSION__) || (__STDC_VERSION__ < 201112L)
#ifndef SCALANATIVE_SUPPRESS_STRUCT_CHECK_WARNING
#warning "Size and order of C structures are not checked when -std < c11."
#endif
#else // POSIX
#include <stddef.h>
#include <sys/times.h>

typedef long scalanative_clock_t;

struct scalanative_tms {
    scalanative_clock_t tms_utime;  //  User CPU time
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
