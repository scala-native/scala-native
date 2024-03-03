#if defined(_WIN32)
// No Windows support. These are dummies for linking.
int getifaddrs(void *dummy) { return -1; };
void freeifaddrs(void *dummy){};
#else
#include <ifaddrs.h>
#include <stddef.h>

#if !(defined __STDC_VERSION__) || (__STDC_VERSION__ < 201112L)
#ifndef SCALANATIVE_SUPPRESS_STRUCT_CHECK_WARNING
#warning "Size and order of C structures are not checked when -std < c11."
#endif
#else
/* Check that the fields defined by Scala Native match "closely enough" those
 * defined by the operating system.
 */

/* Reference: man getifaddrs
 *	      #include <ifaddrs.h>
 */

/* type ifaddrs = CStruct7[
 *   Ptr[Byte], // Ptr[ifaddrs] ifa_next: Next item in list
 *   CString, // ifa_name: Name of interface
 *   CUnsignedInt, // ifa_flags: Flags from SIOCGIFFLAGS
 *   Ptr[sockaddr], // ifa_addr: Address of interface
 *   Ptr[sockaddr], // ifa_netmask: Netmask of interface
 *   Ptr[sockaddr], // union:
 *   // ifu_broadaddr: Broadcast address of interface
 *   // ifu_dstaddr: Point-to-point destination address
 *   Ptr[Byte] // ifa_data: Address-specific data
 * ]
 */

struct scalanative_ifaddrs {
    struct ifaddrs *ifa_next;     /* Next item in list */
    char *ifa_name;               /* Name of interface */
    unsigned int ifa_flags;       /* Flags from SIOCGIFFLAGS */
    struct sockaddr *ifa_addr;    /* Address of interface */
    struct sockaddr *ifa_netmask; /* Netmask of interface */
#ifndef __linux__
    struct sockaddr *ifa_dstaddr; // macOS/BSD #define's ifa_broadcast to this.
#else
    union {
        struct sockaddr *ifu_broadaddr;
        /* Broadcast address of interface */
        struct sockaddr *ifu_dstaddr;
        /* Point-to-point destination address */
    } ifa_ifu;
#endif
    void *ifa_data; /* Address-specific data */
};

_Static_assert(sizeof(struct scalanative_ifaddrs) <= sizeof(struct ifaddrs),
               "unexpected size for ifaddrs");

_Static_assert(offsetof(struct scalanative_ifaddrs, ifa_next) ==
                   offsetof(struct ifaddrs, ifa_next),
               "Unexpected offset: ifaddrs ifa_next");

_Static_assert(offsetof(struct scalanative_ifaddrs, ifa_name) ==
                   offsetof(struct ifaddrs, ifa_name),
               "Unexpected offset: ifaddrs ifa_name");

_Static_assert(offsetof(struct scalanative_ifaddrs, ifa_flags) ==
                   offsetof(struct ifaddrs, ifa_flags),
               "Unexpected offset: ifaddrs ifa_flags");

_Static_assert(offsetof(struct scalanative_ifaddrs, ifa_addr) ==
                   offsetof(struct ifaddrs, ifa_addr),
               "Unexpected offset: ifaddrs ifa_addr");

_Static_assert(offsetof(struct scalanative_ifaddrs, ifa_netmask) ==
                   offsetof(struct ifaddrs, ifa_netmask),
               "Unexpected offset: ifaddrs ifa_netmask");

_Static_assert(offsetof(struct scalanative_ifaddrs, ifa_broadaddr) ==
                   offsetof(struct ifaddrs, ifa_broadaddr),
               "Unexpected offset: ifaddrs ifa_broadaddr");

_Static_assert(offsetof(struct scalanative_ifaddrs, ifa_data) ==
                   offsetof(struct ifaddrs, ifa_data),
               "Unexpected offset: ifaddrs ifa_data");

#endif
#endif // not _WIN32
