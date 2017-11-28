#include <ifaddrs.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>

#include "ifaddrs.h"
#include "posix/sys/socket_conversions.h"

void convert_address(struct sockaddr *raw_in, struct scalanative_sockaddr **out) {
    if (raw_in == NULL) {
        *out = NULL;
        return;
    }
    socklen_t len;
    socklen_t alloc_len;
    if (raw_in->sa_family == AF_INET) {
        len = sizeof(struct sockaddr_in);
        alloc_len = sizeof(struct scalanative_sockaddr_in);
    } else {
        len = sizeof(struct sockaddr_in6);
        alloc_len = sizeof(struct scalanative_sockaddr_in6);
    }
    struct scalanative_sockaddr *converted_address = malloc(alloc_len);
    int convert_result = scalanative_convert_scalanative_sockaddr(raw_in, converted_address, &len);
    if (convert_result != 0) {
        errno = convert_result;
        *out = NULL;
        return;
    }
    *out = converted_address;
}

void scalanative_convert_ifaddrs(struct ifaddrs *in,
                                 struct scalanative_ifaddrs *out) {
    out->ifa_name = strdup(in->ifa_name);
    out->ifa_flags = in->ifa_flags;
    convert_address(in->ifa_addr, &out->ifa_addr);
    convert_address(in->ifa_netmask, &out->ifa_netmask);
    convert_address(in->ifa_broadaddr, &out->_ifa_broadaddr);

    struct ifaddrs *next = in->ifa_next;
    while (next != NULL && 
           next->ifa_addr != NULL && 
           next->ifa_addr->sa_family != AF_INET &&
           next->ifa_addr->sa_family != AF_INET6) {
        next = next->ifa_next;
    }
    if (next != NULL) {
        out->ifa_next = malloc(sizeof(struct scalanative_ifaddrs));
        scalanative_convert_ifaddrs(next, out->ifa_next);
    } else {
        out->ifa_next = NULL;
    }
}

int scalanative_getifaddrs(struct scalanative_ifaddrs **ifap) {
    struct ifaddrs *ifaddr;
    if (getifaddrs(&ifaddr) != 0) {
        return -1;
    }

    while (ifaddr != NULL && 
           ifaddr->ifa_addr != NULL && 
           ifaddr->ifa_addr->sa_family != AF_INET &&
           ifaddr->ifa_addr->sa_family != AF_INET6) {
        ifaddr = ifaddr->ifa_next;
    }

    if (ifaddr != NULL) {
        *ifap = malloc(sizeof(struct scalanative_ifaddrs));
        scalanative_convert_ifaddrs(ifaddr, *ifap);
    } else {
        *ifap = NULL;
    }

    return 0;
}

void scalanative_freeifaddrs(struct scalanative_ifaddrs *ifa) {
    if (ifa == NULL) {
        return;
    }
    scalanative_freeifaddrs(ifa->ifa_next);
    free(ifa->ifa_name);
    free(ifa->ifa_addr);
    free(ifa->ifa_netmask);
    free(ifa->_ifa_broadaddr);
    free(ifa);
}
