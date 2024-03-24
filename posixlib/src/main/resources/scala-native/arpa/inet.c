#if defined(SCALANATIVE_COMPILE_ALWAYS) ||                                     \
    defined(__SCALANATIVE_POSIX_ARPA_INET)
#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#define _WINSOCK_DEPRECATED_NO_WARNINGS
#pragma comment(lib, "Ws2_32.lib")
#include <WinSock2.h>
#else
#include <arpa/inet.h>
#endif
#include "../netinet/in.h"

char *scalanative_inet_ntoa(struct scalanative_in_addr *in) {
    // _Static_assert code in netinet/in.c allow this transform to be valid.
    return inet_ntoa(*((struct in_addr *)in));
}
#endif
