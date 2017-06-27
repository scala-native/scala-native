#include <netinet/in.h>
#include "../netinet/in.h"
#include <sys/socket.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include "socket_conversions.h"
#include "socket.h"

int scalanative_SCM_RIGHTS() { return SCM_RIGHTS; }

int scalanative_SOCK_DGRAM() { return SOCK_DGRAM; }

int scalanative_SOCK_RAW() { return SOCK_RAW; }

int scalanative_SOCK_SEQPACKET() { return SOCK_SEQPACKET; }

int scalanative_SOCK_STREAM() { return SOCK_STREAM; }

int scalanative_SOL_SOCKET() { return SOL_SOCKET; }

int scalanative_SO_ACCEPTCONN() { return SO_ACCEPTCONN; }

int scalanative_SO_BROADCAST() { return SO_BROADCAST; }

int scalanative_SO_DEBUG() { return SO_DEBUG; }

int scalanative_SO_DONTROUTE() { return SO_DONTROUTE; }

int scalanative_SO_ERROR() { return SO_ERROR; }

int scalanative_SO_KEEPALIVE() { return SO_KEEPALIVE; }

int scalanative_SO_LINGER() { return SO_LINGER; }

int scalanative_SO_OOBINLINE() { return SO_OOBINLINE; }

int scalanative_SO_RCVBUF() { return SO_RCVBUF; }

int scalanative_SO_RCVLOWAT() { return SO_RCVLOWAT; }

int scalanative_SO_RCVTIMEO() { return SO_RCVTIMEO; }

int scalanative_SO_REUSEADDR() { return SO_REUSEADDR; }

int scalanative_SO_SNDBUF() { return SO_SNDBUF; }

int scalanative_SO_SNDLOWAT() { return SO_SNDLOWAT; }

int scalanative_SO_SNDTIMEO() { return SO_SNDTIMEO; }

int scalanative_SO_TYPE() { return SO_TYPE; }

int scalanative_SOMAXCONN() { return SOMAXCONN; }

int scalanative_MSG_CTRUNC() { return MSG_CTRUNC; }

int scalanative_MSG_DONTROUTE() { return MSG_DONTROUTE; }

int scalanative_MSG_EOR() { return MSG_EOR; }

int scalanative_MSG_OOB() { return MSG_OOB; }

// Surprisingly, this doesn't exist on MacOS
// int scalanative_MSG_NOSIGNAL() {
//     return MSG_NOSIGNAL;
// }

int scalanative_MSG_PEEK() { return MSG_PEEK; }

int scalanative_MSG_TRUNC() { return MSG_TRUNC; }

int scalanative_MSG_WAITALL() { return MSG_WAITALL; }

int scalanative_AF_INET() { return AF_INET; }

int scalanative_AF_INET6() { return AF_INET6; }

int scalanative_AF_UNIX() { return AF_UNIX; }

int scalanative_AF_UNSPEC() { return AF_UNSPEC; }

int scalanative_socket(int domain, int type, int protocol) {
    return socket(domain, type, protocol);
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

int scalanative_recv(int socket, void *buffer, size_t length, int flags) {
    return recv(socket, buffer, length, flags);
}

int scalanative_send(int socket, void *buffer, size_t length, int flags) {
    return send(socket, buffer, length, flags);
}
