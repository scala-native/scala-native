#include "in.h"

// Match declarations in in.scala

_Static_assert(sizeof(in_addr_t) == 4, "size mismatch: in_addr_t");

_Static_assert(sizeof(in_port_t) == 2, "size mismatch: in_port_t");

_Static_assert(sizeof(struct scalanative_in_addr) == sizeof(struct in_addr),
               "size mismatch: struct scalanative_in_addr");

_Static_assert(sizeof(struct scalanative_in6_addr) == sizeof(struct in6_addr),
               "size mismatch: struct scalanative_in_addr");

// Os sockaddr_in may not have sin_zero field, so require only Posix minimum.
_Static_assert(sizeof(struct scalanative_sockaddr_in) >= 8,
               "size mismatch: struct scalanative_sockaddr_in");

_Static_assert(sizeof(struct scalanative_sockaddr_in6) ==
                   sizeof(struct sockaddr_in6),
               "size mismatch: struct scalanative_sockaddr_in6");

_Static_assert(sizeof(struct scalanative_ipv6_mreq) == sizeof(struct ipv6_mreq),
               "size mismatch: struct scalanative_ipv6_mreq");

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
