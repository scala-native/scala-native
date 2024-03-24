#if defined(SCALANATIVE_COMPILE_ALWAYS) ||                                     \
    defined(__SCALANATIVE_POSIX_NETINET_IN)
#include <stddef.h>
#include <string.h>
#include "in.h"

#if !(defined __STDC_VERSION__) || (__STDC_VERSION__ < 201112L)
#ifndef SCALANATIVE_SUPPRESS_STRUCT_CHECK_WARNING
#warning "Size and order of C structures are not checked when -std < c11."
#endif
#else
// Posix defines the name and type of required fields. Size of fields
// and any internal or tail padding are left unspecified. This section
// verifies that the C and Scala Native definitions match in each compilation
// environment.

// IPv4
_Static_assert(sizeof(struct scalanative_sockaddr_in) == 16,
               "Unexpected size: scalanative_sockaddr_in");

_Static_assert(sizeof(struct scalanative_sockaddr_in) ==
                   sizeof(struct sockaddr_in),
               "Unexpected size: os sockaddr_in");

// On systems which define/use IETF RFC SIN6_LEN macro, sin_family &
// sin_len are synthesized fields, managed by Ops access routines in in.scala.
// C offsetof() sin_family will be 2 for the OS sockaddr_in, but strictly 0 for
// scalanative_sockaddr_in. Scala access routines will access the
// expected bytes.

_Static_assert(offsetof(struct scalanative_sockaddr_in, sin_family) == 0,
               "Unexpected offset: scalanative_sockaddr_in.sin_family");

_Static_assert(offsetof(struct scalanative_sockaddr_in, sin_port) ==
                   offsetof(struct sockaddr_in, sin_port),
               "Unexpected offset: sockaddr_in.sin_port");

_Static_assert(offsetof(struct scalanative_sockaddr_in, sin_addr) ==
                   offsetof(struct sockaddr_in, sin_addr),
               "Unexpected offset: sockaddr_in.sin_addr");
// IPv6
_Static_assert(sizeof(struct scalanative_sockaddr_in6) == 28,
               "Unexpected size: scalanative_sockaddr_in6");

_Static_assert(sizeof(struct scalanative_sockaddr_in6) ==
                   sizeof(struct sockaddr_in6),
               "Unexpected size: os sockaddr_in");

// For systems which define/use IETF RFC SIN6_LEN macro, sin6_family &
// sin6_len see comment above for corresponding scalanative_sockaddr_in6.

_Static_assert(offsetof(struct scalanative_sockaddr_in6, sin6_family) == 0,
               "Unexpected offset: scalanative_sockaddr_in6.sin6_family");

_Static_assert(offsetof(struct scalanative_sockaddr_in6, sin6_port) ==
                   offsetof(struct sockaddr_in6, sin6_port),
               "Unexpected offset: sockaddr_in6.sin6_port");

_Static_assert(offsetof(struct scalanative_sockaddr_in6, sin6_flowinfo) ==
                   offsetof(struct sockaddr_in6, sin6_flowinfo),
               "Unexpected offset: sockaddr_in6.sin6_flowinfo");

_Static_assert(offsetof(struct scalanative_sockaddr_in6, sin6_addr) ==
                   offsetof(struct sockaddr_in6, sin6_addr),
               "Unexpected offset: sockaddr_in6.sin6_addr");

_Static_assert(offsetof(struct scalanative_sockaddr_in6, sin6_scope_id) ==
                   offsetof(struct sockaddr_in6, sin6_scope_id),
               "Unexpected offset: sockaddr_in6.sin6_scope_id");

#endif // structure checking

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
    return IN6_IS_ADDR_UNSPECIFIED((struct in6_addr *)arg);
}

int scalanative_in6_is_addr_loopback(struct scalanative_in6_addr *arg) {
    return IN6_IS_ADDR_LOOPBACK((struct in6_addr *)arg);
}

int scalanative_in6_is_addr_multicast(struct scalanative_in6_addr *arg) {
    return IN6_IS_ADDR_MULTICAST((struct in6_addr *)arg);
}

int scalanative_in6_is_addr_linklocal(struct scalanative_in6_addr *arg) {
    return IN6_IS_ADDR_LINKLOCAL((struct in6_addr *)arg);
}

int scalanative_in6_is_addr_sitelocal(struct scalanative_in6_addr *arg) {
    return IN6_IS_ADDR_SITELOCAL((struct in6_addr *)arg);
}

int scalanative_in6_is_addr_v4mapped(struct scalanative_in6_addr *arg) {
    return IN6_IS_ADDR_V4MAPPED((struct in6_addr *)arg);
}

int scalanative_in6_is_addr_v4compat(struct scalanative_in6_addr *arg) {
    return IN6_IS_ADDR_V4COMPAT((struct in6_addr *)arg);
}

int scalanative_in6_is_addr_mc_nodelocal(struct scalanative_in6_addr *arg) {
    return IN6_IS_ADDR_MC_NODELOCAL((struct in6_addr *)arg);
}

int scalanative_in6_is_addr_mc_linklocal(struct scalanative_in6_addr *arg) {
    return IN6_IS_ADDR_MC_LINKLOCAL((struct in6_addr *)arg);
}

int scalanative_in6_is_addr_mc_sitelocal(struct scalanative_in6_addr *arg) {
    return IN6_IS_ADDR_MC_SITELOCAL((struct in6_addr *)arg);
}

int scalanative_in6_is_addr_mc_orglocal(struct scalanative_in6_addr *arg) {
    return IN6_IS_ADDR_MC_ORGLOCAL((struct in6_addr *)arg);
}

int scalanative_in6_is_addr_mc_global(struct scalanative_in6_addr *arg) {
    return IN6_IS_ADDR_MC_GLOBAL((struct in6_addr *)arg);
}
#endif