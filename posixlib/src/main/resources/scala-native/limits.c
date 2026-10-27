#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_LIMITS)
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
#include <limits.h>
#include <stddef.h>

// The size of a buffer that holds any path (realpath, readlink), with its
// terminating NUL: PATH_MAX, not NAME_MAX, which bounds one path component.
// size_t, as limits.PATH_MAX is a CSize.
size_t scalanative_path_max() { return PATH_MAX; }

#endif // Unix or Mac OS
#endif