#ifdef _WIN32
#include <WinSock2.h>
#include "sys/socket.h" // socklen_t
#define strdup(arg1) _strdup(arg1);
#else
#include <netdb.h>
#endif

struct scalanative_addrinfo {
    int ai_flags;         /* Input flags.  */
    int ai_family;        /* Protocol family for socket.  */
    int ai_socktype;      /* Socket type.  */
    int ai_protocol;      /* Protocol for socket.  */
    socklen_t ai_addrlen; /* Length of socket address.  */
    void *ai_addr;        /* Socket address for socket.  */
    char *ai_canonname;   /* Canonical name for service location.  */
    void *ai_next;        /* Pointer to next in list.  */
};
