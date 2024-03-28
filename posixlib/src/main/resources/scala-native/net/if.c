#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_NET_IF)
#ifdef _WIN32
#include <winsock2.h>
#pragma comment(lib, "Ws2_32.lib")
#include <netioapi.h>
#include <iphlpapi.h>
#pragma comment(lib, "Iphlpapi.lib")
#else
#include <net/if.h>

#include <stddef.h>

struct scalanative_if_nameindex {
    unsigned int if_index;
    char *if_name;
};

#if !(defined __STDC_VERSION__) || (__STDC_VERSION__ < 201112L)
#ifndef SCALANATIVE_SUPPRESS_STRUCT_CHECK_WARNING
#warning "Size and order of C structures are not checked when -std < c11."
#endif
#else

// struct if_nameindex
_Static_assert(sizeof(struct scalanative_if_nameindex) <=
                   sizeof(struct if_nameindex),
               "Unexpected size: struct if_nameindex");

_Static_assert(offsetof(struct scalanative_if_nameindex, if_index) ==
                   offsetof(struct if_nameindex, if_index),
               "Unexpected offset: scalanative_if_nameindex.if_index");

_Static_assert(offsetof(struct scalanative_if_nameindex, if_name) ==
                   offsetof(struct if_nameindex, if_name),
               "Unexpected offset: scalanative_if_nameindex.if_name");

#endif // __STDC_VERSION__
#endif

// Symbolic constants

/* POSIX 2018 says:
 *   The <net/if.h> header shall define the following symbolic constant for
 *   the length of a buffer containing an interface name (including the
 *   terminating NULL character)
 *
 * Windows appears to define the constant without space for that NUL.
 * Be ultra-conservative and allocate one extra location. It is more
 * economical to do that than to spend time debugging strange Windows-only
 * buffer overrun defects.
 */
int scalanative_if_namesize() { return IF_NAMESIZE + 1; }
#endif