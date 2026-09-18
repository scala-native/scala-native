#if defined(SCALANATIVE_COMPILE_ALWAYS) ||                                     \
    defined(__SCALANATIVE_JAVALIB_SOCKET_HELPERS)
#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#pragma comment(lib, "ws2_32.lib")
#include <winsock2.h>
#include <ws2tcpip.h> // struct sockaddr_in6
#else
#include <arpa/inet.h>
#include <netinet/in.h>
#endif
#include <stdint.h>

void scalanative_sockaddr_in_set_port(void *addr, uint32_t port) {
    ((struct sockaddr_in *)addr)->sin_port = htons((uint16_t)port);
}

void scalanative_sockaddr_in6_set_port(void *addr, uint32_t port) {
    ((struct sockaddr_in6 *)addr)->sin6_port = htons((uint16_t)port);
}
#endif
