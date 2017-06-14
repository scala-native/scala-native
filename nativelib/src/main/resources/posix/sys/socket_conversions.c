#ifndef _WIN32
#include <sys/socket.h>
#else
#include "../../os_win_winsock2.h"
#endif
#include "../netinet/in.h"
#include "socket_conversions.h"
#include <stdlib.h>
#include <errno.h>

int scalanative_convert_sockaddr_in(struct scalanative_sockaddr_in *in,
                                    struct sockaddr_in **out, socklen_t *size) {
    struct sockaddr_in *s =
        (struct sockaddr_in *)malloc(sizeof(struct sockaddr_in));
    *size = sizeof(struct sockaddr_in);
    s->sin_family = in->sin_family;
    s->sin_port = in->sin_port;
    scalanative_convert_in_addr(&(in->sin_addr), &(s->sin_addr));
    *out = s;
    return 0;
}

int scalanative_convert_sockaddr_in6(struct scalanative_sockaddr_in6 *in,
                                     struct sockaddr_in6 **out,
                                     socklen_t *size) {
    struct sockaddr_in6 *s =
        (struct sockaddr_in6 *)malloc(sizeof(struct sockaddr_in6));
    *size = sizeof(struct sockaddr_in6);
    s->sin6_family = in->sin6_family;
    s->sin6_port = in->sin6_port;
    s->sin6_flowinfo = in->sin6_flowinfo;
    scalanative_convert_in6_addr(&(in->sin6_addr), &(s->sin6_addr));
    s->sin6_scope_id = in->sin6_scope_id;
    *out = s;
    return 0;
}

int scalanative_convert_sockaddr_storage(
    struct scalanative_sockaddr_storage *in, struct sockaddr_storage **out,
    socklen_t *size) {
    struct sockaddr_storage *s =
        (struct sockaddr_storage *)malloc(sizeof(struct sockaddr_storage));
    *size = sizeof(struct sockaddr_storage);
    s->ss_family = in->ss_family;
    *out = s;
    return 0;
}

int scalanative_convert_sockaddr(struct scalanative_sockaddr *raw_in,
                                 struct sockaddr **out, socklen_t *size) {
    int result;
    switch (*size) {
    case sizeof(struct scalanative_sockaddr_in):
        result = scalanative_convert_sockaddr_in(
            (struct scalanative_sockaddr_in *)raw_in,
            (struct sockaddr_in **)out, size);
        break;

    case sizeof(struct scalanative_sockaddr_in6):
        result = scalanative_convert_sockaddr_in6(
            (struct scalanative_sockaddr_in6 *)raw_in,
            (struct sockaddr_in6 **)out, size);
        break;

    case sizeof(struct scalanative_sockaddr_storage):
        result = scalanative_convert_sockaddr_storage(
            (struct scalanative_sockaddr_storage *)raw_in,
            (struct sockaddr_storage **)out, size);
        break;

    default:
        result = EAFNOSUPPORT;
        break;
    }

    return result;
}

int scalanative_convert_scalanative_sockaddr_in(
    struct sockaddr_in *in, struct scalanative_sockaddr_in *out,
    socklen_t *size) {
    *size = sizeof(struct scalanative_sockaddr_in);
    out->sin_family = in->sin_family;
    out->sin_port = in->sin_port;
    scalanative_convert_scalanative_in_addr(&(in->sin_addr), &(out->sin_addr));
    return 0;
}

int scalanative_convert_scalanative_sockaddr_in6(
    struct sockaddr_in6 *in, struct scalanative_sockaddr_in6 *out,
    socklen_t *size) {
    *size = sizeof(struct scalanative_sockaddr_in6);
    out->sin6_family = in->sin6_family;
    out->sin6_port = in->sin6_port;
    out->sin6_flowinfo = in->sin6_flowinfo;
    scalanative_convert_scalanative_in6_addr(&(in->sin6_addr),
                                             &(out->sin6_addr));
    out->sin6_scope_id = in->sin6_scope_id;
    return 0;
}

int scalanative_convert_scalanative_sockaddr_storage(
    struct sockaddr_storage *in, struct scalanative_sockaddr_storage *out,
    socklen_t *size) {
    *size = sizeof(struct scalanative_sockaddr_storage);
    out->ss_family = in->ss_family;
    return 0;
}

int scalanative_convert_scalanative_sockaddr(struct sockaddr *raw_in,
                                             struct scalanative_sockaddr *out,
                                             socklen_t *size) {
    int result;
    switch (*size) {
    case sizeof(struct sockaddr_in):
        result = scalanative_convert_scalanative_sockaddr_in(
            (struct sockaddr_in *)raw_in, (struct scalanative_sockaddr_in *)out,
            size);
        break;

    case sizeof(struct sockaddr_in6):
        result = scalanative_convert_scalanative_sockaddr_in6(
            (struct sockaddr_in6 *)raw_in,
            (struct scalanative_sockaddr_in6 *)out, size);
        break;

    case sizeof(struct sockaddr_storage):
        result = scalanative_convert_scalanative_sockaddr_storage(
            (struct sockaddr_storage *)raw_in,
            (struct scalanative_sockaddr_storage *)out, size);
        break;

    default:
        result = EAFNOSUPPORT;
        break;
    }

    return result;
}
