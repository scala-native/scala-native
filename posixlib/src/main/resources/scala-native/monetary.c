#ifdef _WIN32
// No Windows support
#else
#if !(defined __STDC_VERSION__) || (__STDC_VERSION__ < 201112L)
#ifndef SCALANATIVE_SUPPRESS_STRUCT_CHECK_WARNING
#warning "Size and order of C structures are not checked when -std < c11."
#endif
#else // POSIX
#include <stddef.h>
#include <monetary.h>

#include <locale.h> // FIXME

#ifdef __APPLE__
#include <xlocale.h>
#endif // __APPLE__

ssize_t scalanative_strfmon_10(char *restrict str, size_t max,
                               const char *restrict format, double arg0,
                               double arg1, double arg2, double arg3,
                               double arg4, double arg5, double arg6,
                               double arg7, double arg8, double arg9) {

    return strfmon(str, max, format, arg0, arg1, arg2, arg3, arg4, arg5, arg6,
                   arg7, arg8, arg9);
}

ssize_t scalanative_strfmon_l_10(char *restrict str, size_t max,
                                 locale_t locale, const char *restrict format,
                                 double arg0, double arg1, double arg2,
                                 double arg3, double arg4, double arg5,
                                 double arg6, double arg7, double arg8,
                                 double arg9) {

    return strfmon_l(str, max, locale, format, arg0, arg1, arg2, arg3, arg4,
                     arg5, arg6, arg7, arg8, arg9);
}

#endif // POSIX
#endif // ! _WIN32
