#ifdef _WIN32
#include <stdio.h>
#endif

extern "C" {
#ifdef _WIN32
// we need to create fake uses of functions (snprintf etc) otherwise it will be
// stripped from the file
int scalanative_snprintf(char *s, size_t n, const char *format, va_list args) {
    return snprintf(s, n, format, args);
}

int scalanative_sprintf(char *s, const char *format, va_list args) {
    return sprintf(s, format, args);
}
#endif
}