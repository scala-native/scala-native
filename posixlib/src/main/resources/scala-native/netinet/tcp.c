#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <WinSock2.h>
#else
#ifdef __OpenBSD__
#include <sys/types.h>
#endif
#include <netinet/tcp.h>
#endif

int scalanative_tcp_nodelay() { return TCP_NODELAY; }
