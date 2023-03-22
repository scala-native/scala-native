#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))

#include <dlfcn.h>

int scalanative_rtld_lazy() { return RTLD_LAZY; };

int scalanative_rtld_now() { return RTLD_NOW; };

int scalanative_rtld_global() { return RTLD_GLOBAL; };

int scalanative_rtld_local() { return RTLD_LOCAL; };

#endif // Unix or Mac OS
