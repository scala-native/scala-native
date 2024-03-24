#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_NETDB)
#ifdef _WIN32
#include <WinSock2.h>
#include <ws2tcpip.h> // socklen_t
// #include <Winerror.h>
#else // not _WIN32
/* FreeBSD wants AF_INET, which is in <sys/socket.h>
 *
 * Windows can not find the <> form, and suggests the "" form.
 *
 * On linux, macOS, etc. This include should provide AF_INET if it has
 * not been previously defined.
 */

#include <sys/socket.h>
#include <netdb.h>
#endif

#include <stddef.h>

struct scalanative_addrinfo {
    int ai_flags;         /* Input flags.  */
    int ai_family;        /* Protocol family for socket.  */
    int ai_socktype;      /* Socket type.  */
    int ai_protocol;      /* Protocol for socket.  */
    socklen_t ai_addrlen; /* Length of socket address.	*/
    void *ai_addr;        /* Socket address for socket.	 */
    char *ai_canonname;   /* Canonical name for service location.  */
    void *ai_next;        /* Pointer to next in list.  */
};

_Static_assert(sizeof(struct scalanative_addrinfo) == sizeof(struct addrinfo),
               "Unexpected size: os addrinfo");

_Static_assert(offsetof(struct scalanative_addrinfo, ai_flags) ==
                   offsetof(struct addrinfo, ai_flags),
               "Unexpected offset: scalanative_addrinfo.ai_flags");

_Static_assert(offsetof(struct scalanative_addrinfo, ai_family) ==
                   offsetof(struct addrinfo, ai_family),
               "Unexpected offset: scalanative_addrinfo.ai_family");

_Static_assert(offsetof(struct scalanative_addrinfo, ai_socktype) ==
                   offsetof(struct addrinfo, ai_socktype),
               "Unexpected offset: scalanative_addrinfo.ai_socktype");

_Static_assert(offsetof(struct scalanative_addrinfo, ai_protocol) ==
                   offsetof(struct addrinfo, ai_protocol),
               "Unexpected offset: scalanative_addrinfo.ai_protocol");

_Static_assert(offsetof(struct scalanative_addrinfo, ai_addrlen) ==
                   offsetof(struct addrinfo, ai_addrlen),
               "Unexpected offset: scalanative_addrinfo.ai_addrlen");

#if !(defined(__APPLE__) || defined(__FreeBSD__) || defined(__NetBSD__) ||     \
      defined(_WIN32))
// Linux, etc.

_Static_assert(offsetof(struct scalanative_addrinfo, ai_addr) ==
                   offsetof(struct addrinfo, ai_addr),
               "Unexpected offset: scalanative_addrinfo.ai_addr");

_Static_assert(offsetof(struct scalanative_addrinfo, ai_canonname) ==
                   offsetof(struct addrinfo, ai_canonname),
               "Unexpected offset: scalanative_addrinfo.ai_canonname");
#else
_Static_assert(offsetof(struct scalanative_addrinfo, ai_addr) ==
                   offsetof(struct addrinfo, ai_canonname),
               "Unexpected offset: BSD addrinfo ai_addr fixup");

_Static_assert(offsetof(struct scalanative_addrinfo, ai_canonname) ==
                   offsetof(struct addrinfo, ai_addr),
               "Unexpected offset: BSD addrinfo ai_canonname fixup");

#endif // (defined(__APPLE__) || defined(__FreeBSD__) || defined(__NetBSD__) ||
       // defined(_WIN32))

_Static_assert(offsetof(struct scalanative_addrinfo, ai_next) ==
                   offsetof(struct addrinfo, ai_next),
               "Unexpected offset: scalanative_addrinfo.ai_next");

int scalanative_getaddrinfo(char *name, char *service,
                            struct scalanative_addrinfo *hints,
                            struct scalanative_addrinfo **res) {

    // ai_flags, ai_socktype, and ai_protocol and all else will be zero.
    struct addrinfo posixHints = {.ai_flags = AF_UNSPEC};

    struct addrinfo *vettedHints =
        (hints != NULL) ? (struct addrinfo *)hints : &posixHints;

    return getaddrinfo(name, service, vettedHints, (struct addrinfo **)res);
}

// AI_* items are declared in the order of Posix specification

int scalanative_ai_passive() { return AI_PASSIVE; }

int scalanative_ai_canonname() { return AI_CANONNAME; }

int scalanative_ai_numerichost() { return AI_NUMERICHOST; }

int scalanative_ai_numericserv() { return AI_NUMERICSERV; }

int scalanative_ai_v4mapped() {
#ifdef AI_V4MAPPED
    return AI_V4MAPPED;
#else
    return 0;
#endif
}

int scalanative_ai_all() {
#ifdef AI_ALL
    return AI_ALL;
#else
    return 0;
#endif
}

int scalanative_ai_addrconfig() { return AI_ADDRCONFIG; }

// NI_* items are declared in the order of Posix specification

int scalanative_ni_nofqdn() { return NI_NOFQDN; }

int scalanative_ni_numerichost() { return NI_NUMERICHOST; }

int scalanative_ni_namereqd() { return NI_NAMEREQD; }

int scalanative_ni_numericserv() { return NI_NUMERICSERV; }

int scalanative_ni_numericscope() {
#if !defined(NI_NUMERICSCOPE)
    /* Silently return a no-op flag.
     * Do not disturb the tranquility of the vast majority of projects,
     * which have absolutely no interest in NI_NUMERICSCOPE, by issuing the
     * #warning one might expect.
     *
     * NI_NUMERICSCOPE is undefined on Linux and possibly Windows.
     */
    return 0;
#else
    return NI_NUMERICSCOPE;
#endif
}

int scalanative_ni_dgram() { return NI_DGRAM; }

// EAI_* items are declared in the order of Posix specification

#ifndef _WIN32
int scalanative_eai_again() { return EAI_AGAIN; }

int scalanative_eai_badflags() { return EAI_BADFLAGS; }

int scalanative_eai_fail() { return EAI_FAIL; }

int scalanative_eai_family() { return EAI_FAMILY; }

int scalanative_eai_memory() { return EAI_MEMORY; }

int scalanative_eai_noname() { return EAI_NONAME; }

int scalanative_eai_service() { return EAI_SERVICE; }

int scalanative_eai_socktype() { return EAI_SOCKTYPE; }

int scalanative_eai_system() { return EAI_SYSTEM; }

int scalanative_eai_overflow() { return EAI_OVERFLOW; }

#else // _Win32
/* Reference:  https://docs.microsoft.com/en-us/windows/win32/api
 *		   /ws2tcpip/nf-ws2tcpip-getaddrinfo
 */

int scalanative_eai_again() { return WSATRY_AGAIN; }

int scalanative_eai_badflags() { return WSAEINVAL; }

int scalanative_eai_fail() { return WSANO_RECOVERY; }

int scalanative_eai_family() { return WSAEAFNOSUPPORT; }

int scalanative_eai_memory() { return WSA_NOT_ENOUGH_MEMORY; }

int scalanative_eai_noname() { return WSAHOST_NOT_FOUND; }

int scalanative_eai_service() { return WSATYPE_NOT_FOUND; }

int scalanative_eai_socktype() { return WSAESOCKTNOSUPPORT; }

// Windows seems not to have an equivalent, use ubiquitous -1
int scalanative_eai_system() { return -1; }

// Windows seems not to have an equivalent, use ubiquitous -1
int scalanative_eai_overflow() { return -1; }

#endif // _Win32
#endif