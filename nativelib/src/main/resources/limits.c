#ifndef _WIN32
#include <limits.h>
#else
#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif

#include <windows.h>

#if !defined(NAME_MAX)
#define NAME_MAX MAX_PATH
#endif
#endif

int scalanative_path_max() { return NAME_MAX; }
