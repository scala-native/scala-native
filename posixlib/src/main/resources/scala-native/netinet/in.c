#include <string.h>
#include "in.h"

void scalanative_convert_in_addr(struct scalanative_in_addr *in,
                                 struct in_addr *out) {
    out->s_addr = in->so_addr;
}

void scalanative_convert_in6_addr(struct scalanative_in6_addr *in,
                                  struct in6_addr *out) {
    void *ignored = memcpy(out->s6_addr, in->_s6_addr, 16);
}

void scalanative_convert_scalanative_in_addr(struct in_addr *in,
                                             struct scalanative_in_addr *out) {
    out->so_addr = in->s_addr;
}

void scalanative_convert_scalanative_in6_addr(
    struct in6_addr *in, struct scalanative_in6_addr *out) {
    void *ignored = memcpy(out->_s6_addr, in->s6_addr, 16);
}

int scalanative_ipproto_ip() { return IPPROTO_IP; }

int scalanative_ipproto_ipv6() { return IPPROTO_IPV6; }

int scalanative_ipproto_icmp() { return IPPROTO_ICMP; }

int scalanative_ipproto_raw() { return IPPROTO_RAW; }

int scalanative_ipproto_tcp() { return IPPROTO_TCP; }

int scalanative_ipproto_udp() { return IPPROTO_UDP; }

uint32_t scalanative_inaddr_any() { return INADDR_ANY; }

uint32_t scalanative_inaddr_broadcast() { return INADDR_BROADCAST; }

int scalanative_inet6_addrstrlen() { return INET6_ADDRSTRLEN; }

int scalanative_inet_addrstrlen() { return INET_ADDRSTRLEN; }

int scalanative_ipv6_join_group() { return IPV6_JOIN_GROUP; }

int scalanative_ipv6_leave_group() { return IPV6_LEAVE_GROUP; }

int scalanative_ipv6_multicast_hops() { return IPV6_MULTICAST_HOPS; }

int scalanative_ipv6_multicast_if() { return IPV6_MULTICAST_IF; }

int scalanative_ipv6_multicast_loop() { return IPV6_MULTICAST_LOOP; }

int scalanative_ipv6_unicast_hops() { return IPV6_UNICAST_HOPS; }

int scalanative_ipv6_v6only() { return IPV6_V6ONLY; }

int scalanative_ip_multicast_if() { return IP_MULTICAST_IF; }

int scalanative_ip_multicast_loop() { return IP_MULTICAST_LOOP; }

int scalanative_ip_tos() { return IP_TOS; }

int scalanative_in6_is_addr_unspecified(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_UNSPECIFIED(&converted);
}

int scalanative_in6_is_addr_loopback(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_LOOPBACK(&converted);
}

int scalanative_in6_is_addr_multicast(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_MULTICAST(&converted);
}

int scalanative_in6_is_addr_linklocal(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_LINKLOCAL(&converted);
}

int scalanative_in6_is_addr_sitelocal(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_SITELOCAL(&converted);
}

int scalanative_in6_is_addr_v4mapped(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_V4MAPPED(&converted);
}

int scalanative_in6_is_addr_v4compat(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_V4COMPAT(&converted);
}

int scalanative_in6_is_addr_mc_nodelocal(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_MC_NODELOCAL(&converted);
}

int scalanative_in6_is_addr_mc_linklocal(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_MC_LINKLOCAL(&converted);
}

int scalanative_in6_is_addr_mc_sitelocal(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_MC_SITELOCAL(&converted);
}

int scalanative_in6_is_addr_mc_orglocal(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_MC_ORGLOCAL(&converted);
}

int scalanative_in6_is_addr_mc_global(struct scalanative_in6_addr *arg) {
    struct in6_addr converted;
    scalanative_convert_in6_addr(arg, &converted);
    return IN6_IS_ADDR_MC_GLOBAL(&converted);
}
