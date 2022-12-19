#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))

#include <fnmatch.h>
#endif // Unix or Mac OS

#if defined(_WIN32) // bogus values to keep linker happy
#define FNM_NOMATCH -1
#define FNM_PATHNAME -1
#define FNM_PERIOD -1
#define FNM_NOESCAPE -1
#endif // _WIN32

int scalanative_fnm_nomatch() { return FNM_NOMATCH; };

int scalanative_fnm_pathname() { return FNM_PATHNAME; };

int scalanative_fnm_period() { return FNM_PERIOD; };

int scalanative_fnm_noescape() { return FNM_NOESCAPE; };
