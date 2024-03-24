#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_FNMATCH)
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))

#include <fnmatch.h>

int scalanative_fnm_nomatch() { return FNM_NOMATCH; };

int scalanative_fnm_pathname() { return FNM_PATHNAME; };

int scalanative_fnm_period() { return FNM_PERIOD; };

int scalanative_fnm_noescape() { return FNM_NOESCAPE; };

#endif // Unix or Mac OS
#endif