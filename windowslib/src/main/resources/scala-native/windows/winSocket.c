#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include "Windows.h"
#include <winsock2.h>

#pragma comment(lib, "Ws2_32.lib")

DWORD scalanative_winsock_wsadata_size() { return sizeof(WSADATA); }

DWORD scalanative_winsocket_fionbio() { return FIONBIO; }

SOCKET scalanative_winsock_invalid_socket() { return INVALID_SOCKET; }

SHORT scalanative_winsock_poll_pollin() { return POLLIN; }
SHORT scalanative_winsock_poll_pollout() { return POLLOUT; }

#endif // defined(_WIN32)
