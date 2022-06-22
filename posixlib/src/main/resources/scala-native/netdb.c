#include "netdb.h"

#ifdef _WIN32
#include <Winerror.h>
#else // not _WIN32
// FreeBSD wants AF_INET, which is in <sys/socket.h> but not in the local
// "sys/socket.h".
//
// Windows can not find the <> form, and suggests the "" form. However,
// the later is a local copy which does not define AF_INET.
// Including that file prevents the system copy with AF_INET from
// being included.
//
// On linux, macOS, etc. the include should provide AF_INET if it has
// not been previously defined.

#include <sys/socket.h>
#endif

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
    // doesn't care about them
    if (in == NULL) {
        // Use of Posix spec of ai_flags being 0, not GNU extension value.
        memset(out, 0, sizeof(struct addrinfo));
        out->ai_family = AF_UNSPEC;
    } else {
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

int scalanative_ai_numerichost() { return AI_NUMERICHOST; }

int scalanative_ai_passive() { return AI_PASSIVE; }

int scalanative_ai_numericserv() { return AI_NUMERICSERV; }

int scalanative_ai_addrconfig() { return AI_ADDRCONFIG; }

int scalanative_ai_v4mapped() { return AI_V4MAPPED; }

int scalanative_ai_canonname() { return AI_CANONNAME; }

// EAI_* items are declared in the order of Posix specification

#ifndef _WIN32
int scalanative_eai_again() { return EAI_AGAIN; }

int scalanative_eai_badflags() { return EAI_BADFLAGS; }

int scalanative_eai_fail() { return EAI_FAIL; }

int scalanative_eai_family() { return EAI_FAMILY; }

int scalanative_eai_memory() { return EAI_MEMORY; }

int scalanative_eai_noname() { return EAI_NONAME; }

int scalanative_eai_service() { return EAI_SERVICE; }

int scalanative_eai_socktype() { return EAI_SOCKTYPE; }

int scalanative_eai_system() { return EAI_SYSTEM; }

int scalanative_eai_overflow() { return EAI_OVERFLOW; }

#else // _Win32
/* Reference:  https://docs.microsoft.com/en-us/windows/win32/api
 *                 /ws2tcpip/nf-ws2tcpip-getaddrinfo
 */

int scalanative_eai_again() { return WSATRY_AGAIN; }

int scalanative_eai_badflags() { return WSAEINVAL; }

int scalanative_eai_fail() { return WSANO_RECOVERY; }

int scalanative_eai_family() { return WSAEAFNOSUPPORT; }

int scalanative_eai_memory() { return WSA_NOT_ENOUGH_MEMORY; }

int scalanative_eai_noname() { return WSAHOST_NOT_FOUND; }

int scalanative_eai_service() { return WSATYPE_NOT_FOUND; }

int scalanative_eai_socktype() { return WSAESOCKTNOSUPPORT; }

// Windows seems not to have an equivalent, use ubiquitous -1
int scalanative_eai_system() { return -1; }

// Windows seems not to have an equivalent, use ubiquitous -1
int scalanative_eai_overflow() { return -1; }

#endif // _Win32
