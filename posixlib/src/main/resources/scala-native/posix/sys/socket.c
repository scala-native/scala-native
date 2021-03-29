#include <netinet/in.h>
#include "../netinet/in.h"
#include <sys/socket.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include "socket_conversions.h"
#include "socket.h"

int scalanative_scm_rights() { return SCM_RIGHTS; }

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

int scalanative_msg_eor() { return MSG_EOR; }

int scalanative_msg_oob() { return MSG_OOB; }

int scalanative_msg_nosignal() {
#ifdef MSG_NOSIGNAL
    return MSG_NOSIGNAL;
#endif
#ifndef MSG_NOSIGNAL
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
    struct sockaddr *converted_address;
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

    free(converted_address);
    return result;
}

int scalanative_socket(int domain, int type, int protocol) {
    return socket(domain, type, protocol);
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

int scalanative_listen(int socket, int backlog) {
    return listen(socket, backlog);
}

int scalanative_accept(int socket, struct scalanative_sockaddr *address,
                       socklen_t *address_len) {
    struct sockaddr *converted_address;
    int convert_result =
        scalanative_convert_sockaddr(address, &converted_address, address_len);

    int result;

    if (convert_result == 0) {
        result = accept(socket, converted_address, address_len);
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

    free(converted_address);
    return result;
}

int scalanative_setsockopt(int socket, int level, int option_name,
                           void *option_value, socklen_t option_len) {
    return setsockopt(socket, level, option_name, option_value, option_len);
}

int scalanative_getsockopt(int socket, int level, int option_name,
                           void *option_value, socklen_t *option_len) {
    return getsockopt(socket, level, option_name, option_value, option_len);
}

int scalanative_recv(int socket, void *buffer, size_t length, int flags) {
    return recv(socket, buffer, length, flags);
}

int scalanative_send(int socket, void *buffer, size_t length, int flags) {
    return send(socket, buffer, length, flags);
}

int scalanative_shutdown(int socket, int how) { return shutdown(socket, how); }
