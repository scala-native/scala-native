#ifndef __IFADDRS_H
#define __IFADDRS_H

#include "sys/socket.h"

struct scalanative_ifaddrs {
    void* ifa_next;
    char *ifa_name;
    unsigned int ifa_flags;
    struct scalanative_sockaddr *ifa_addr;
    struct scalanative_sockaddr *ifa_netmask;
    struct scalanative_sockaddr *_ifa_broadaddr;
};

#endif
