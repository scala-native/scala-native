#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
#include <limits.h>

int scalanative_path_max() { return NAME_MAX; }

#endif // Unix or Mac OS
