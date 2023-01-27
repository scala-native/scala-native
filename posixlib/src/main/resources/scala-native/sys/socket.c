#include <string.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include "socket_conversions.h"

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
#else
// Posix defines the name and type of required fields. Size of fields
// and any internal or tail padding are left unspecified. This section
// verifies that the C and Scala Native definitions match in each compilation
// environment.
//
// The first sockaddr field in C has had size 2 and no padding after it
// since time immemorial. Verify that the Scala Native field has the same.

_Static_assert(offsetof(struct scalanative_sockaddr, sa_data) == 2,
               "Unexpected size: scalanative_sockaddr sa_family");

_Static_assert(offsetof(struct scalanative_sockaddr, sa_data) ==
                   offsetof(struct sockaddr, sa_data),
               "offset mismatch: sockaddr sa_data");
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

int scalanative_getsockname(int socket, struct scalanative_sockaddr *address,
                            socklen_t *address_len) {
    struct sockaddr *converted_address = NULL;
    int convert_result =
        scalanative_convert_sockaddr(address, &converted_address, address_len);

    int result;

    if (convert_result == 0) {
        result = getsockname(socket, converted_address, address_len);
        convert_result = scalanative_convert_scalanative_sockaddr(
            converted_address, address, address_len);

        if (convert_result != 0) {
            errno = convert_result;
            result = -1;
        }
    } else {
        errno = convert_result;
        result = -1;
    }

    if (converted_address != NULL)
        free(converted_address);

    return result;
}

int scalanative_getpeername(int socket, struct scalanative_sockaddr *address,
                            socklen_t *address_len) {
    struct sockaddr *converted_address = NULL;
    int convert_result =
        scalanative_convert_sockaddr(address, &converted_address, address_len);

    int result;

    if (convert_result == 0) {
        result = getpeername(socket, converted_address, address_len);
        convert_result = scalanative_convert_scalanative_sockaddr(
            converted_address, address, address_len);

        if (convert_result != 0) {
            errno = convert_result;
            result = -1;
        }
    } else {
        errno = convert_result;
        result = -1;
    }

    if (converted_address != NULL)
        free(converted_address);

    return result;
}

int scalanative_bind(int socket, struct scalanative_sockaddr *address,
                     socklen_t address_len) {
    struct sockaddr *converted_address;
    int convert_result =
        scalanative_convert_sockaddr(address, &converted_address, &address_len);

    int result;

    if (convert_result == 0) {
        result = bind(socket, converted_address, address_len);
    } else {
        errno = convert_result;
        result = -1;
    }

    free(converted_address);
    return result;
}

int scalanative_connect(int socket, struct scalanative_sockaddr *address,
                        socklen_t address_len) {
    struct sockaddr *converted_address;
    int convert_result =
        scalanative_convert_sockaddr(address, &converted_address, &address_len);

    int result;

    if (convert_result == 0) {
        result = connect(socket, converted_address, address_len);
    } else {
        errno = convert_result;
        result = -1;
    }
    free(converted_address);
    return result;
}

int scalanative_accept(int socket, struct scalanative_sockaddr *address,
                       socklen_t *address_len) {
    struct sockaddr *converted_address;
    int convert_result = address != NULL ? // addr and addr_len can be NULL
                             scalanative_convert_sockaddr(
                                 address, &converted_address, address_len)
                                         : 0;

    int result;

    if (convert_result == 0) {
        result = accept(socket, converted_address, address_len);
        convert_result = address != NULL
                             ? scalanative_convert_scalanative_sockaddr(
                                   converted_address, address, address_len)
                             : 0;

        if (convert_result != 0) {
            errno = convert_result;
            result = -1;
        }
    } else {
        errno = convert_result;
        result = -1;
    }

    if (address != NULL)
        free(converted_address);
    return result;
}
