#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_FNMATCH)
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))

#include <stddef.h>
#include <glob.h>

// Note Well: see corresponding comments in glob.scala
struct scalanative_glob_t {
    size_t gl_pathc; // count of total paths so far
    int gl_matchc;   // count of paths matching pattern
    size_t gl_offs;  // Slots to reserve at the beginning of gl_pathv.
    int gl_flags;    // returned flags
    char **gl_pathv; // Pointer to a list of matched pathnames.
    char filler[56]; // macOS non-POSIX fields
};

#if !(defined __STDC_VERSION__) || (__STDC_VERSION__ < 201112L)
#ifndef SCALANATIVE_SUPPRESS_STRUCT_CHECK_WARNING
#warning "Size and order of C structures are not checked when -std < c11."
#endif
#else

_Static_assert(sizeof(struct scalanative_glob_t) >= sizeof(glob_t),
               "size mismatch: glob_t");

_Static_assert(offsetof(struct scalanative_glob_t, gl_pathc) ==
                   offsetof(glob_t, gl_pathc),
               "offset mismatch: glob_t gl_pathc");

_Static_assert(offsetof(struct scalanative_glob_t, gl_offs) ==
                   offsetof(glob_t, gl_offs),
               "offset mismatch: glob_t gl_offs");

#if defined(__linux__)
// gl_pathv is second element on Linux.
_Static_assert(sizeof(((struct scalanative_glob_t *)0)->gl_pathc) ==
                   offsetof(glob_t, gl_pathv),
               "offset mismatch: glob_t gl_pathv");
#else  // __APPLE__
_Static_assert(offsetof(struct scalanative_glob_t, gl_pathv) ==
                   offsetof(glob_t, gl_pathv),
               "offset mismatch: glob_t gl_pathv");
#endif // __APPLE__
#endif // __STDC_VERSION__

// flags
int scalanative_glob_append() { return GLOB_APPEND; };

int scalanative_glob_dooffs() { return GLOB_DOOFFS; };

int scalanative_glob_err() { return GLOB_ERR; };

int scalanative_glob_mark() { return GLOB_MARK; };

int scalanative_glob_nocheck() { return GLOB_NOCHECK; };

int scalanative_glob_noescape() { return GLOB_NOESCAPE; };

int scalanative_glob_nosort() { return GLOB_NOSORT; };

// error returns
int scalanative_glob_aborted() { return GLOB_ABORTED; };

int scalanative_glob_nomatch() { return GLOB_NOMATCH; };

int scalanative_glob_nospace() { return GLOB_NOSPACE; };

#endif // Unix or Mac OS
#endif