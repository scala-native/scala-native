#include "netdb.h"
#include "sys/socket_conversions.h"
#include <stddef.h>
#include <stdlib.h>

void scalanative_convert_scalanative_addrinfo(struct scalanative_addrinfo *in,
                                              struct addrinfo *out) {
    out->ai_flags = in->ai_flags;
    out->ai_family = in->ai_family;
    out->ai_socktype = in->ai_socktype;
    out->ai_protocol = in->ai_protocol;
    out->ai_canonname = in->ai_canonname;
    if (in->ai_addr == NULL) {
        out->ai_addr = NULL;
        out->ai_addrlen = in->ai_addrlen;
    } else {
        struct sockaddr *converted_addr = malloc(in->ai_addrlen);
        socklen_t *len = malloc(sizeof(socklen_t));
        *len = in->ai_addrlen;
        scalanative_convert_sockaddr(in->ai_addr, &converted_addr, len);
        out->ai_addr = converted_addr;
        out->ai_addrlen = *len;
    }
    if (in->ai_next != NULL) {
        struct addrinfo *converted = malloc(sizeof(struct addrinfo));
        scalanative_convert_scalanative_addrinfo(
            (struct scalanative_addrinfo *)in->ai_next, converted);
        out->ai_next = converted;
    } else {
        out->ai_next = NULL;
    }
}

void scalanative_convert_addrinfo(struct addrinfo *in,
                                  struct scalanative_addrinfo *out) {
    out->ai_flags = in->ai_flags;
    out->ai_family = in->ai_family;
    out->ai_socktype = in->ai_socktype;
    out->ai_protocol = in->ai_protocol;
    out->ai_addrlen = in->ai_addrlen;
    out->ai_canonname = in->ai_canonname;
    if (in->ai_addr == NULL) {
        out->ai_addr = NULL;
        out->ai_addrlen = in->ai_addrlen;
    } else {
        struct scalanative_sockaddr *converted_addr = malloc(in->ai_addrlen);
        socklen_t *len = malloc(sizeof(socklen_t));
        *len = in->ai_addrlen;
        scalanative_convert_scalanative_sockaddr(in->ai_addr, converted_addr,
                                                 len);
        out->ai_addr = converted_addr;
        out->ai_addrlen = *len;
    }
    if (in->ai_next != NULL) {
        struct scalanative_addrinfo *converted =
            malloc(sizeof(struct scalanative_addrinfo));
        scalanative_convert_addrinfo(in->ai_next, converted);
        out->ai_next = converted;
    } else {
        out->ai_next = NULL;
    }
}

int scalanative_getaddrinfo(char *name, char *service,
                            struct scalanative_addrinfo *hints,
                            struct scalanative_addrinfo *res) {
    struct addrinfo hints_converted;
    struct addrinfo *res_c;
    scalanative_convert_scalanative_addrinfo(hints, &hints_converted);
    int status = getaddrinfo(name, service, &hints_converted, &res_c);
    scalanative_convert_addrinfo(res_c, res);
    free(res_c);
    return status;
}

int scalanative_getnameinfo(struct scalanative_sockaddr *addr,
                            socklen_t addrlen, char *host, socklen_t hostlen,
                            char *serv, socklen_t servlen, int flags) {
    struct sockaddr *converted_addr;
    scalanative_convert_sockaddr(addr, &converted_addr, &addrlen);
    return getnameinfo(converted_addr, addrlen, host, hostlen, serv, servlen,
                       flags);
}
