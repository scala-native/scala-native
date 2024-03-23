#if defined(__SCALANATIVE_POSIX_NL_TYPES)
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))

#include <nl_types.h>

int scalanative_nl_setd() { return NL_SETD; };
int scalanative_nl_cat_locale() { return NL_CAT_LOCALE; };
#endif // Unix or Mac OS
#endif