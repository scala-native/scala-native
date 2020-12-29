#include <stddef.h>
#include <stdio.h>
#include <sys/time.h>
#include <time.h>

#if !(defined __STDC_VERSION__) || (__STDC_VERSION__ < 201112L)
#ifndef SCALANATIVE_SUPPRESS_STRUCT_CHECK_WARNING
#warning "Size and order of C structures are not checked when -std < c11."
#endif
#else
// Make the reasonable assumption that the order and size of members
// used in link time libc matches that of compilation time .h files.
// This is true with Scala Native default compilation and link options.
//
// It is possible, but not easy, to chose a combination of compile &
// link time options which break this assumption. 

_Static_assert(sizeof(struct tm) <= 56,
               "struct is larger than its declaration in time.scala");

_Static_assert(offsetof(struct tm, tm_sec) == 0, "Unexpected offset");
_Static_assert(offsetof(struct tm, tm_min) == 4, "Unexpected offset");
_Static_assert(offsetof(struct tm, tm_hour) == 8, "Unexpected offset");
_Static_assert(offsetof(struct tm, tm_mday) == 12, "Unexpected offset");
_Static_assert(offsetof(struct tm, tm_mon) == 16, "Unexpected offset");
_Static_assert(offsetof(struct tm, tm_year) == 20, "Unexpected offset");
_Static_assert(offsetof(struct tm, tm_wday) == 24, "Unexpected offset");
_Static_assert(offsetof(struct tm, tm_yday) == 28, "Unexpected offset");
_Static_assert(offsetof(struct tm, tm_isdst) == 32, "Unexpected offset");
#endif

char **scalanative_tzname() { return tzname; }

long scalanative_timezone() { return timezone; }

int scalanative_daylight() { return daylight; }
