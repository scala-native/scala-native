#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
#include <stddef.h>
#include <stdlib.h>

// Call the header-selected symbols, e.g. NetBSD's __putenv50.

int scalanative_putenv(char *string) { return putenv(string); }

int scalanative_unsetenv(const char *name) { return unsetenv(name); }

char *scalanative_initstate(unsigned int seed, char *state, size_t size) {
    return initstate(seed, state, size);
}

void scalanative_srandom(unsigned int seed) { srandom(seed); }

#endif // Unix or Mac OS
