#include <netinet/in.h>
#include <string.h>
#include "in.h"

void scalanative_convert_in_addr(struct scalanative_in_addr *in,
                                 struct in_addr *out) {
    out->s_addr = in->s_addr;
}

void scalanative_convert_in6_addr(struct scalanative_in6_addr *in,
                                  struct in6_addr *out) {
    void *ignored = memcpy(out->s6_addr, in->_s6_addr, 16);
}

void scalanative_convert_scalanative_in_addr(struct in_addr *in,
                                             struct scalanative_in_addr *out) {
    out->s_addr = in->s_addr;
}

void scalanative_convert_scalanative_in6_addr(
    struct in6_addr *in, struct scalanative_in6_addr *out) {
    void *ignored = memcpy(out->_s6_addr, in->s6_addr, 16);
}

int scalanative_IPPROTO_IP() { return IPPROTO_IP; }

int scalanative_IPPROTO_IPV6() { return IPPROTO_IPV6; }

int scalanative_IPPROTO_ICMP() { return IPPROTO_ICMP; }

int scalanative_IPPROTO_RAW() { return IPPROTO_RAW; }

int scalanative_IPPROTO_TCP() { return IPPROTO_TCP; }

int scalanative_IPPROTO_UDP() { return IPPROTO_UDP; }

uint32_t scalanative_INADDR_ANY() { return INADDR_ANY; }

uint32_t scalanative_INADDR_BROADCAST() { return INADDR_BROADCAST; }

int scalanative_INET6_ADDRSTRLEN() { return INET6_ADDRSTRLEN; }

int scalanative_INET_ADDRSTRLEN() { return INET_ADDRSTRLEN; }

int scalanative_IPV6_JOIN_GROUP() { return IPV6_JOIN_GROUP; }

int scalanative_IPV6_LEAVE_GROUP() { return IPV6_LEAVE_GROUP; }

int scalanative_IPV6_MULTICAST_HOPS() { return IPV6_MULTICAST_HOPS; }

int scalanative_IPV6_MULTICAST_IF() { return IPV6_MULTICAST_IF; }

int scalanative_IPV6_MULTICAST_LOOP() { return IPV6_MULTICAST_LOOP; }

int scalanative_IPV6_UNICAST_HOPS() { return IPV6_UNICAST_HOPS; }

int scalanative_IPV6_V6ONLY() { return IPV6_V6ONLY; }

int scalanative_IP_MULTICAST_IF() { return IP_MULTICAST_IF; }

int scalanative_IP_MULTICAST_LOOP() { return IP_MULTICAST_LOOP; }

int scalanative_IP_TOS() { return IP_TOS; }

int scalanative_IN6_IS_ADDR_UNSPECIFIED(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_UNSPECIFIED(&converted);
}

int scalanative_IN6_IS_ADDR_LOOPBACK(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_LOOPBACK(&converted);
}

int scalanative_IN6_IS_ADDR_MULTICAST(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_MULTICAST(&converted);
}

int scalanative_IN6_IS_ADDR_LINKLOCAL(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_LINKLOCAL(&converted);
}

int scalanative_IN6_IS_ADDR_SITELOCAL(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_SITELOCAL(&converted);
}

int scalanative_IN6_IS_ADDR_V4MAPPED(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_V4MAPPED(&converted);
}

int scalanative_IN6_IS_ADDR_V4COMPAT(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_V4COMPAT(&converted);
}

int scalanative_IN6_IS_ADDR_MC_NODELOCAL(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_MC_NODELOCAL(&converted);
}

int scalanative_IN6_IS_ADDR_MC_LINKLOCAL(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_MC_LINKLOCAL(&converted);
}

int scalanative_IN6_IS_ADDR_MC_SITELOCAL(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_MC_SITELOCAL(&converted);
}

int scalanative_IN6_IS_ADDR_MC_ORGLOCAL(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_MC_ORGLOCAL(&converted);
}

int scalanative_IN6_IS_ADDR_MC_GLOBAL(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_MC_GLOBAL(&converted);
}
