#if !defined(_WIN32) && defined(SCALANATIVE_COMPILE_ALWAYS) ||                 \
    defined(__SCALANATIVE_POSIX_SYS_UN)
#include <sys/socket.h>
#if !(defined __STDC_VERSION__) || (__STDC_VERSION__ < 201112L)
#ifndef SCALANATIVE_SUPPRESS_STRUCT_CHECK_WARNING
#warning "Size and order of C structures are not checked when -std < c11."
#endif
#else // POSIX
#include <stddef.h>
#include <sys/un.h>

typedef unsigned short scalanative_sa_family_t;

// 108 for sun_path is the Linux value is >= macOS value of 104. Checked below.
struct scalanative_sockaddr_un {
    scalanative_sa_family_t sun_family;
    char sun_path[108];
};

// Also verifies that Scala Native sun_family field has the traditional size.
_Static_assert(offsetof(struct scalanative_sockaddr_un, sun_path) == 2,
               "Unexpected size: scalanative_sockaddr_un sun_family");

_Static_assert(offsetof(struct scalanative_sockaddr_un, sun_path) ==
                   offsetof(struct sockaddr_un, sun_path),
               "offset mismatch: sockaddr_un sun_path");

_Static_assert(sizeof(struct sockaddr_un) <=
                   sizeof(struct scalanative_sockaddr_un),
               "size mismatch: sockaddr_un sun_path");

#endif // POSIX
#endif // ! _WIN32
