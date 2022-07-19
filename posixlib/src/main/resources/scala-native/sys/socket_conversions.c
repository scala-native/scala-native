#include "../netinet/in.h"
#include "socket_conversions.h"
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#ifndef _WIN32
#include <sys/socket.h>
#endif

int scalanative_convert_sockaddr_in(struct scalanative_sockaddr_in *in,
                                    struct sockaddr_in **out, socklen_t *size) {
    struct sockaddr_in *s =
        (struct sockaddr_in *)malloc(sizeof(struct sockaddr_in));
    *size = sizeof(struct sockaddr_in);

    void *ignored = memcpy(s, in, sizeof(struct sockaddr_in));

    *out = s;
    return 0;
}

int scalanative_convert_sockaddr_in6(struct scalanative_sockaddr_in6 *in,
                                     struct sockaddr_in6 **out,
                                     socklen_t *size) {
    struct sockaddr_in6 *s =
        (struct sockaddr_in6 *)malloc(sizeof(struct sockaddr_in6));
    *size = sizeof(struct sockaddr_in6);

    void *ignored = memcpy(s, in, sizeof(struct sockaddr_in6));

    *out = s;
    return 0;
}

int scalanative_convert_sockaddr_storage(
    struct scalanative_sockaddr_storage *in, struct sockaddr_storage **out,
    socklen_t *size) {
    struct sockaddr_storage *s =
        (struct sockaddr_storage *)malloc(sizeof(struct sockaddr_storage));
    *size = sizeof(struct sockaddr_storage);

    void *ignored = memcpy(s, in, sizeof(struct sockaddr_storage));

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

    void *ignored = memcpy(out, in, sizeof(struct sockaddr_in));

    return 0;
}

int scalanative_convert_scalanative_sockaddr_in6(
    struct sockaddr_in6 *in, struct scalanative_sockaddr_in6 *out,
    socklen_t *size) {
    *size = sizeof(struct scalanative_sockaddr_in6);

    void *ignored = memcpy(out, in, sizeof(struct sockaddr_in6));

    return 0;
}

int scalanative_convert_scalanative_sockaddr_storage(
    struct sockaddr_storage *in, struct scalanative_sockaddr_storage *out,
    socklen_t *size) {
    struct sockaddr_storage *s =
        (struct sockaddr_storage *)malloc(sizeof(struct sockaddr_storage));
    *size = sizeof(struct scalanative_sockaddr_storage);

    void *ignored = memcpy(s, in, sizeof(struct sockaddr_storage));

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
