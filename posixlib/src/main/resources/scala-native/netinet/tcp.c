#if defined(SCALANATIVE_COMPILE_ALWAYS) ||                                     \
    defined(__SCALANATIVE_POSIX_NETINET_TCP)
#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <winsock2.h>
#else
#ifdef __OpenBSD__
#include <sys/types.h>
#endif
#include <netinet/tcp.h>
#endif

int scalanative_tcp_nodelay() { return TCP_NODELAY; }
#endif