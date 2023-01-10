#ifdef _WIN32
// NO Windows support
#elif defined(__linux__)
// Does not exist on Linux, so no check
#else // macOS, FreeBSD, etc.

#if defined(__FreeBSD__)
// Make u_* types required/used by FreeBSD net/if_dl.h available
#undef __BSD_VISIBLE
#define __BSD_VISIBLE 1
#include <sys/types.h> // size_t
#endif

#include <net/if_dl.h>
#include <stddef.h>

#if !(defined __STDC_VERSION__) || (__STDC_VERSION__ < 201112L)
#ifndef SCALANATIVE_SUPPRESS_STRUCT_CHECK_WARNING
#warning "Size and order of C structures are not checked when -std < c11."
#endif
#else
/* Check that the fields defined by Scala Native match "closely enough" those
 * defined by the operating system.
 */

/* Reference: macOs: man sockaddr_dl
 *	#include <net/if_dl.h>
 */

/* type sockaddr_dl = CStruct7[
 *   CShort, // sdl_family;	// address family
 *   CShort, // sdl_index
 *   Byte, // sdl_type
 *   Byte, // sdl_nlen
 *   Byte, // sdl_alen
 *   Byte, // sdl_slen
 *   CArray[CChar, _46] // sdl_data, max(macOs == 12, FreeBsd == 46)
 * ]
 */

struct scalanative_sockaddr_dl {
    unsigned char sdl_len;    //  Total length of sockaddr
    unsigned char sdl_family; // address family
    unsigned short sdl_index; // if != 0, system interface index
    unsigned char sdl_type;   // interface type
    unsigned char sdl_nlen;   // interface name length
    unsigned char sdl_alen;   // link level address length
    unsigned char sdl_slen;   // link layer selector length
    char sdl_data[46];        // contains both if name and ll address
                              // sdl_data, max(macOs == 12, FreeBsd == 46)
};

/* SN >= os because macOS declares sdl_data to have size 12 but uses
 * it as a longer variable length buffer.
 * SN uses the FreeBSD 46 to make it easier to avoid array index errors.
 */
_Static_assert(sizeof(struct scalanative_sockaddr_dl) >=
                   sizeof(struct sockaddr_dl),
               "unexpected size for sockaddr_dl");

_Static_assert(offsetof(struct scalanative_sockaddr_dl, sdl_family) ==
                   offsetof(struct sockaddr_dl, sdl_family),
               "Unexpected offset: ifaddrs sdl_family");

_Static_assert(offsetof(struct scalanative_sockaddr_dl, sdl_index) ==
                   offsetof(struct sockaddr_dl, sdl_index),
               "Unexpected offset: ifaddrs sdl_index");

_Static_assert(offsetof(struct scalanative_sockaddr_dl, sdl_type) ==
                   offsetof(struct sockaddr_dl, sdl_type),
               "Unexpected offset: ifaddrs sdl_type");

_Static_assert(offsetof(struct scalanative_sockaddr_dl, sdl_nlen) ==
                   offsetof(struct sockaddr_dl, sdl_nlen),
               "Unexpected offset: ifaddrs sdl_nlen");

_Static_assert(offsetof(struct scalanative_sockaddr_dl, sdl_alen) ==
                   offsetof(struct sockaddr_dl, sdl_alen),
               "Unexpected offset: ifaddrs sdl_alen");

_Static_assert(offsetof(struct scalanative_sockaddr_dl, sdl_slen) ==
                   offsetof(struct sockaddr_dl, sdl_slen),
               "Unexpected offset: ifaddrs sdl_slen");

_Static_assert(offsetof(struct scalanative_sockaddr_dl, sdl_data) ==
                   offsetof(struct sockaddr_dl, sdl_data),
               "Unexpected offset: ifaddrs sdl_data");
#endif
#endif // not _WIN32
