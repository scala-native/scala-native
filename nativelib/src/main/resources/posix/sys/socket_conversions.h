#ifndef __SYS_SOCKET_CONVERSIONS_H
#define __SYS_SOCKET_CONVERSIONS_H

#ifndef _WIN32
#include <sys/socket.h>
#include <netinet/in.h>
#else
#include "../../os_win_winsock2.h"
#endif
#include "../netinet/in.h"
#include "socket.h"

struct scalanative_sockaddr {
    scalanative_sa_family_t sa_family;
    char sa_data[14];
};

struct scalanative_sockaddr_storage {
    scalanative_sa_family_t ss_family;
};

int scalanative_convert_sockaddr_in(struct scalanative_sockaddr_in *in,
                                    struct sockaddr_in **out, socklen_t *size);

int scalanative_convert_sockaddr_in6(struct scalanative_sockaddr_in6 *in,
                                     struct sockaddr_in6 **out,
                                     socklen_t *size);

int scalanative_convert_sockaddr_storage(
    struct scalanative_sockaddr_storage *in, struct sockaddr_storage **out,
    socklen_t *size);

int scalanative_convert_sockaddr(struct scalanative_sockaddr *raw_in,
                                 struct sockaddr **out, socklen_t *size);

int scalanative_convert_scalanative_sockaddr_in(
    struct sockaddr_in *in, struct scalanative_sockaddr_in *out,
    socklen_t *size);

int scalanative_convert_scalanative_sockaddr_in6(
    struct sockaddr_in6 *in, struct scalanative_sockaddr_in6 *out,
    socklen_t *size);

int scalanative_convert_scalanative_sockaddr_storage(
    struct sockaddr_storage *in, struct scalanative_sockaddr_storage *out,
    socklen_t *size);

int scalanative_convert_scalanative_sockaddr(struct sockaddr *raw_in,
                                             struct scalanative_sockaddr *out,
                                             socklen_t *size);

#endif // __SYS_SOCKET_CONVERSIONS_H
