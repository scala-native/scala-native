#if defined(_POSIX_VERSION)
#include <limits.h>

int scalanative_path_max() { return NAME_MAX; }

#endif //_POSIX_VERSION
