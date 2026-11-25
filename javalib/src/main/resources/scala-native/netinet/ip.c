#if defined(SCALANATIVE_COMPILE_ALWAYS) ||                                     \
    defined(__SCALANATIVE_JAVALIB_NETINET_IP)
#ifdef _WIN32
#include <winsock2.h>
#include <ws2tcpip.h>
#else
#include <netinet/in.h>
#endif

int scalanative_ip_multicast_ttl() { return IP_MULTICAST_TTL; }

int scalanative_ip_add_membership() { return IP_ADD_MEMBERSHIP; }

int scalanative_ip_drop_membership() { return IP_DROP_MEMBERSHIP; }
#endif
