#include "netdb.h"
#include "sys/socket_conversions.h"
#include <stddef.h>
#include <stdlib.h>
#include <string.h>

int scalanative_getnameinfo(struct scalanative_sockaddr *addr,
                            socklen_t addrlen, char *host, socklen_t hostlen,
                            char *serv, socklen_t servlen, int flags) {
    struct sockaddr *converted_addr;
    scalanative_convert_sockaddr(addr, &converted_addr, &addrlen);
    int status = getnameinfo(converted_addr, addrlen, host, hostlen, serv,
                             servlen, flags);
    free(converted_addr);
    return status;
}

void scalanative_convert_scalanative_addrinfo(struct scalanative_addrinfo *in,
                                              struct addrinfo *out) {
    // ai_addr and ai_next fields are set to NULL because this function is only
    // used for converting hints parameter for the getaddrinfo function, which
    // doesn't
    // care about them
    out->ai_flags = in->ai_flags;
    out->ai_family = in->ai_family;
    out->ai_socktype = in->ai_socktype;
    out->ai_protocol = in->ai_protocol;
    out->ai_addrlen = in->ai_addrlen;
    if (in->ai_canonname == NULL) {
        out->ai_canonname = NULL;
    } else {
        out->ai_canonname = strdup(in->ai_canonname);
    }
    out->ai_addr = NULL;
    out->ai_next = NULL;
}

void scalanative_convert_addrinfo(struct addrinfo *in,
                                  struct scalanative_addrinfo *out) {
    out->ai_flags = in->ai_flags;
    out->ai_family = in->ai_family;
    out->ai_socktype = in->ai_socktype;
    out->ai_protocol = in->ai_protocol;
    if (in->ai_addr == NULL) {
        out->ai_addr = NULL;
        out->ai_addrlen = in->ai_addrlen;
    } else {
        socklen_t size;
        if (in->ai_addr->sa_family == AF_INET) {
            struct scalanative_sockaddr_in *addr =
                malloc(sizeof(struct scalanative_sockaddr_in));
            scalanative_convert_scalanative_sockaddr_in(
                (struct sockaddr_in *)in->ai_addr, addr, &size);
            out->ai_addr = (struct scalanative_sockaddr *)addr;
        } else {
            struct scalanative_sockaddr_in6 *addr =
                malloc(sizeof(struct scalanative_sockaddr_in6));
            scalanative_convert_scalanative_sockaddr_in6(
                (struct sockaddr_in6 *)in->ai_addr, addr, &size);
            out->ai_addr = (struct scalanative_sockaddr *)addr;
        }
        out->ai_addrlen = size;
    }
    if (in->ai_canonname == NULL) {
        out->ai_canonname = NULL;
    } else {
        out->ai_canonname = strdup(in->ai_canonname);
    }
    if (in->ai_next == NULL) {
        out->ai_next = NULL;
    } else {
        struct scalanative_addrinfo *next_native =
            malloc(sizeof(struct scalanative_addrinfo));
        scalanative_convert_addrinfo(in->ai_next, next_native);
        out->ai_next = next_native;
    }
}

void scalanative_freeaddrinfo(struct scalanative_addrinfo *addr) {
    if (addr != NULL) {
        free(addr->ai_canonname);
        free(addr->ai_addr);
        scalanative_freeaddrinfo((struct scalanative_addrinfo *)addr->ai_next);
        free(addr);
    }
}

int scalanative_getaddrinfo(char *name, char *service,
                            struct scalanative_addrinfo *hints,
                            struct scalanative_addrinfo **res) {
    struct addrinfo hints_c;
    struct addrinfo *res_c;
    scalanative_convert_scalanative_addrinfo(hints, &hints_c);
    int status = getaddrinfo(name, service, &hints_c, &res_c);
    free(hints_c.ai_canonname);
    if (status != 0) {
        return status;
    }
    struct scalanative_addrinfo *res_native =
        malloc(sizeof(struct scalanative_addrinfo));
    scalanative_convert_addrinfo(res_c, res_native);
    freeaddrinfo(res_c);
    *res = res_native;
    return status;
}

int scalanative_AI_NUMERICHOST() { return AI_NUMERICHOST; }

int scalanative_AI_PASSIVE() { return AI_PASSIVE; }

int scalanative_AI_NUMERICSERV() { return AI_NUMERICSERV; }

int scalanative_AI_ADDRCONFIG() { return AI_ADDRCONFIG; }

int scalanative_AI_V4MAPPED() { return AI_V4MAPPED; }

int scalanative_AI_CANONNAME() { return AI_CANONNAME; }
