#include <string.h>
#include <stddef.h>
#include <stdlib.h>
#include <errno.h>

#if defined(__MINGW64__)
#include <mswsock.h>
#include <ws2tcpip.h>
#endif
#ifdef _WIN32
#include <WinSock2.h>
#pragma comment(lib, "Ws2_32.lib")
typedef SSIZE_T ssize_t;
#else
#if defined(__FreeBSD__)
#import <sys/types.h> // u_long & friends. Required by Amazon FreeBSD64 arm64
#endif                // __FreeBSD__
#include <netinet/in.h>
#include <sys/socket.h>
#if !(defined __STDC_VERSION__) || (__STDC_VERSION__ < 201112L)
#ifndef SCALANATIVE_SUPPRESS_STRUCT_CHECK_WARNING
#warning "Size and order of C structures are not checked when -std < c11."
#endif
#else // POSIX
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

// struct msghdr - POSIX 48 byte (padding) on 64 bit machines, 28 on 32 bit.
struct scalanative_msghdr {
    void *msg_name;
    uint32_t msg_namelen;
    struct iovec *msg_iov;
    uint32_t msg_iovlen;
    void *msg_control;
    uint32_t msg_controllen;
    int msg_flags;
};

#if !defined(__LP64__) && !defined(__ILP32__)
#error "Unknown hardware memory model, not __ILP32__, not __LP64__"
#endif

#if defined(__ILP32__)
_Static_assert(sizeof(struct msghdr) == 28,
               "Unexpected size: struct msghdr, expected 28");
_Static_assert(sizeof(struct msghdr) == sizeof(struct scalanative_msghdr),
               "sizeof mismatch: OS & SN msghdr");
#elif defined(__linux__) // __LP64__
// Only do a rough check, will use conversion mapping routines in C code.
_Static_assert(sizeof(struct msghdr) == 56,
               "Unexpected size: struct msghdr, expected 56");
#else                    // 64 bit POSIX - macOS, FreeBSD

// Will use direct passthru to C, so check all fields.
_Static_assert(sizeof(struct msghdr) == 48,
               "Unexpected size: struct msghdr, expected 48");

_Static_assert(sizeof(struct msghdr) == sizeof(struct scalanative_msghdr),
               "sizeof mismatch: OS & SN msghdr");

_Static_assert(offsetof(struct msghdr, msg_name) ==
                   offsetof(struct scalanative_msghdr, msg_name),
               "offset mismatch: OS & SN msg_name expected 0");

_Static_assert(offsetof(struct msghdr, msg_namelen) ==
                   offsetof(struct scalanative_msghdr, msg_namelen),
               "offset mismatch: OS & SN msg_namelen expected 8");

_Static_assert(offsetof(struct msghdr, msg_iov) ==
                   offsetof(struct scalanative_msghdr, msg_iov),
               "offset mismatch: OS & SN msg_iov expected 16");

_Static_assert(offsetof(struct msghdr, msg_iovlen) ==
                   offsetof(struct scalanative_msghdr, msg_iovlen),
               "offset mismatch: OS & SN msg_iovlen expected 24");

_Static_assert(offsetof(struct msghdr, msg_control) ==
                   offsetof(struct scalanative_msghdr, msg_control),
               "offset mismatch: OS & SN msg_control expected 32");

_Static_assert(offsetof(struct msghdr, msg_controllen) ==
                   offsetof(struct scalanative_msghdr, msg_controllen),
               "offset mismatch: OS & SN msg_controllen expected 40");

_Static_assert(offsetof(struct msghdr, msg_flags) ==
                   offsetof(struct scalanative_msghdr, msg_flags),
               "offset mismatch: OS & SN msg_flags expected 44");
#endif                   // POSIX msghdr

// POSIX 2018 & prior 12 byte definition, Linux uses 16 bytes.
struct scalanative_cmsghdr {
    socklen_t cmsg_len;
    int cmsg_level;
    int cmsg_type;
};

#if defined(__ILP32__)
_Static_assert(sizeof(struct cmsghdr) == 12,
               "Unexpected size: struct cmsghdr, expected 12");
#elif defined(__linux__) // __LP64__
// Only do a rough check, developer must pass OS cmsghdr in & expect same back.
_Static_assert(sizeof(struct cmsghdr) == 16,
               "Unexpected size: struct msghdr, expected 16");
#else                    // 64 bit POSIX - macOS, FreeBSD

// Will use direct passthru to C, so check all fields.
_Static_assert(sizeof(struct cmsghdr) == 12,
               "Unexpected size: struct cmsghdr, expected 12");

_Static_assert(sizeof(struct cmsghdr) == sizeof(struct scalanative_cmsghdr),
               "sizeof mismatch: OS & SN cmsghdr");

_Static_assert(offsetof(struct cmsghdr, cmsg_len) ==
                   offsetof(struct scalanative_cmsghdr, cmsg_len),
               "offset mismatch: OS & SN cmsg_len expected 0");

_Static_assert(offsetof(struct cmsghdr, cmsg_level) ==
                   offsetof(struct scalanative_cmsghdr, cmsg_level),
               "offset mismatch: OS & SN cmsg_level expected 4");

_Static_assert(offsetof(struct cmsghdr, cmsg_type) ==
                   offsetof(struct scalanative_cmsghdr, cmsg_type),
               "offset mismatch: OS & SN cmsg_type expected 8");
#endif                   // POSIX cmsghdr
#endif                   // structure size checking
#endif                   // !_WIN32

// Symbolic constants

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

int scalanative_so_reuseport() {
#ifdef SO_REUSEPORT
    return SO_REUSEPORT;
#else
    return 0;
#endif
}

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

int scalanative_shut_rd() {
#ifdef SHUT_RD
    return SHUT_RD;
#else // _WIN32
    return 0;
#endif
}

int scalanative_shut_rdwr() {
#ifdef SHUT_RDWR
    return SHUT_RDWR;
#else // _WIN32
    return 0;
#endif
}

int scalanative_shut_wr() {
#ifdef SHUT_WR
    return SHUT_WR;
#else // _WIN32
    return 0;
#endif
}

// Macros
#ifdef _WIN32
void *scalanative_cmsg_data(void *cmsg) { return NULL; }
#else
unsigned char *scalanative_cmsg_data(struct cmsghdr *cmsg) {
    return CMSG_DATA(cmsg);
}
#endif

#ifdef _WIN32
void *scalanative_cmsg_nxthdr(void *mhdr, void *cmsg) { return NULL; }
#else
struct cmsghdr *scalanative_cmsg_nxthdr(struct msghdr *mhdr,
                                        struct cmsghdr *cmsg) {
    return CMSG_NXTHDR(mhdr, cmsg);
}
#endif

#ifdef _WIN32
void *scalanative_cmsg_firsthdr(void *mhdr) { return NULL; }
#else
struct cmsghdr *scalanative_cmsg_firsthdr(struct msghdr *mhdr) {
    return CMSG_FIRSTHDR(mhdr);
}
#endif

// Functions
#ifdef _WIN32
long scalanative_recvmsg(int socket, void *msg, int flags) {
    errno = ENOTSUP;
    return -1;
}
#else // unix
long scalanative_recvmsg(int socket, struct msghdr *msg, int flags) {
#if !defined(__linux__) || !defined(__LP64__)
    return recvmsg(socket, (struct msghdr *)msg, flags);
#else // Linux 64 bits
    /* BEWARE: Embedded control messages are not converted!
     *	       Caller must send non-POSIX linux64 ctlhdr structures
     *	       and expect such to be returned by OS.
     */

    int status = -1;

    if (msg == NULL) {
        errno = EINVAL;
    } else {

        struct msghdr cMsg = {.msg_name = msg->msg_name,
                              .msg_namelen = msg->msg_namelen,
                              .msg_iov = msg->msg_iov,
                              .msg_iovlen = msg->msg_iovlen,
                              .msg_control = msg->msg_control,
                              .msg_controllen = msg->msg_controllen,
                              .msg_flags = msg->msg_flags};

        status = recvmsg(socket, &cMsg, flags);

        // recvmsg can alter some of these fields, so copy everything back.
        if (status > -1) {
            msg->msg_name = cMsg.msg_name;
            msg->msg_namelen = cMsg.msg_namelen;
            msg->msg_iov = cMsg.msg_iov;
            msg->msg_iovlen = cMsg.msg_iovlen;
            msg->msg_control = cMsg.msg_control;
            msg->msg_controllen = cMsg.msg_controllen;
            msg->msg_flags = cMsg.msg_flags;
        }
    }

    return status;
#endif
}
#endif // unix

#ifdef _WIN32
long scalanative_sendmsg(int socket, void *msg, int flags) {
    errno = ENOTSUP;
    return -1;
}
#else // unix
long scalanative_sendmsg(int socket, struct msghdr *msg, int flags) {
#if !defined(__linux__) || !defined(__LP64__)
    return sendmsg(socket, (struct msghdr *)msg, flags);
#else // Linux 64 bits
    /* BEWARE: Embedded control messages are not converted!
     *	       Caller must send non-POSIX linux64 ctlhdr structures
     *	       and expect such to be returned by OS.
     */

    int status = -1;

    if (msg == NULL) {
        errno = EINVAL;
    } else {
        struct msghdr cMsg = {.msg_name = msg->msg_name,
                              .msg_namelen = msg->msg_namelen,
                              .msg_iov = msg->msg_iov,
                              .msg_iovlen = msg->msg_iovlen,
                              .msg_control = msg->msg_control,
                              .msg_controllen = msg->msg_controllen,
                              .msg_flags = msg->msg_flags};

        // cMsg is read-only, so no need to copy data back to Scala
        status = sendmsg(socket, &cMsg, flags);
    }

    return status;
#endif
}
#endif // unix

int scalanative_sockatmark(int socket) {
#if defined(_WIN32)
    errno = ENOTSUP;
    return -1;
#else
    return sockatmark(socket);
#endif
}

int scalanative_socketpair(int domain, int type, int protocol, int *sv) {
#if defined(_WIN32)
    errno = ENOTSUP;
    return -1;
#else
    return socketpair(domain, type, protocol, sv);
#endif
}
