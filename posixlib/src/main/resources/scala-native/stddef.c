#ifdef _WIN32
#error "posixlib is not implemented on Windows."
#else // not _WIN32
#include <stddef.h>

// Macros
void *scalanative_posix_null() { return NULL; }

#endif // _Win32
