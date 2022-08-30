#include <string.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>

#ifdef _WIN32
#include <WinSock2.h>
#pragma comment(lib, "Ws2_32.lib")
typedef SSIZE_T ssize_t;
#else
#include <netinet/in.h>
#include <sys/socket.h>
#if !(defined __STDC_VERSION__) || (__STDC_VERSION__ < 201112L)
#ifndef SCALANATIVE_SUPPRESS_STRUCT_CHECK_WARNING
#warning "Size and order of C structures are not checked when -std < c11."
#endif
#else
/* POSIX defines the name and type of required fields. Size of fields
 * and any internal or tail padding are left unspecified. This section
 * verifies that the C and Scala Native definitions match in each compilation
 * environment.
 *
 * With such assurance, Scala Native code can call directly into C or
 * C like code without an expensive conversion layer.
 *
 * The first sockaddr field in C has had size 2 and no padding after it
 * since time immemorial.
 *
 * BSD operating systems changed. macOS & FreeBSD kept the two byte prologue
 * by shortening sa_family to one byte and adding a one byte
 * sin_len/sin6_len field (editorial snark deleted).
 *
 * Here the traditional 2 bytes are declared. On BSD systems, code in
 * Socket.scala handles reading and writing the "short" sa_family and
 * synthesizes the sin*_len fields.
 *
 * If scalanative_sa_family_t _ever_ changes here, keep in sync with
 * netinet/in.h.
 */

typedef unsigned short scalanative_sa_family_t;

struct scalanative_sockaddr {
    scalanative_sa_family_t sa_family;
    char sa_data[14];
};

struct scalanative_sockaddr_storage {
    scalanative_sa_family_t ss_family;
    unsigned short __opaquePadTo32;
    unsigned int __opaquePadTo64;
    unsigned long long __opaqueAlignStructure[15];
};

// Also verifies that Scala Native sa_family field has the traditional size.
_Static_assert(offsetof(struct scalanative_sockaddr, sa_data) == 2,
               "Unexpected size: scalanative_sockaddr sa_family");

_Static_assert(offsetof(struct scalanative_sockaddr, sa_data) ==
                   offsetof(struct sockaddr, sa_data),
               "offset mismatch: sockaddr sa_data");

_Static_assert(sizeof(struct sockaddr_storage) == 128,
               "unexpected size for sockaddr_storage");
#endif
#endif

int scalanative_scm_rights() {
#ifdef SCM_RIGHTS
    return SCM_RIGHTS;
#else
    return 0;
#endif
}

int scalanative_sock_dgram() { return SOCK_DGRAM; }

int scalanative_sock_raw() { return SOCK_RAW; }

int scalanative_sock_seqpacket() { return SOCK_SEQPACKET; }

int scalanative_sock_stream() { return SOCK_STREAM; }

int scalanative_sol_socket() { return SOL_SOCKET; }

int scalanative_so_acceptconn() { return SO_ACCEPTCONN; }

int scalanative_so_broadcast() { return SO_BROADCAST; }

int scalanative_so_debug() { return SO_DEBUG; }

int scalanative_so_dontroute() { return SO_DONTROUTE; }

int scalanative_so_error() { return SO_ERROR; }

int scalanative_so_keepalive() { return SO_KEEPALIVE; }

int scalanative_so_linger() { return SO_LINGER; }

int scalanative_so_oobinline() { return SO_OOBINLINE; }

int scalanative_so_rcvbuf() { return SO_RCVBUF; }

int scalanative_so_rcvlowat() { return SO_RCVLOWAT; }

int scalanative_so_rcvtimeo() { return SO_RCVTIMEO; }

int scalanative_so_reuseaddr() { return SO_REUSEADDR; }

int scalanative_so_sndbuf() { return SO_SNDBUF; }

int scalanative_so_sndlowat() { return SO_SNDLOWAT; }

int scalanative_so_sndtimeo() { return SO_SNDTIMEO; }

int scalanative_so_type() { return SO_TYPE; }

int scalanative_somaxconn() { return SOMAXCONN; }

int scalanative_msg_ctrunc() { return MSG_CTRUNC; }

int scalanative_msg_dontroute() { return MSG_DONTROUTE; }

int scalanative_msg_eor() {
#ifdef MSG_EOR
    return MSG_EOR;
#else
    return 0;
#endif
}

int scalanative_msg_oob() { return MSG_OOB; }

int scalanative_msg_nosignal() {
#ifdef MSG_NOSIGNAL
    return MSG_NOSIGNAL;
#else
    return 0;
#endif
}

int scalanative_msg_peek() { return MSG_PEEK; }

int scalanative_msg_trunc() { return MSG_TRUNC; }

int scalanative_msg_waitall() { return MSG_WAITALL; }

int scalanative_af_inet() { return AF_INET; }

int scalanative_af_inet6() { return AF_INET6; }

int scalanative_af_unix() { return AF_UNIX; }

int scalanative_af_unspec() { return AF_UNSPEC; }
