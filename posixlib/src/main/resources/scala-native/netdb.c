#ifdef _WIN32
#include <WinSock2.h>
#else
#include <netdb.h>
#endif

#include <stddef.h>
#include "netdb.h"

_Static_assert(sizeof(struct scalanative_addrinfo) == sizeof(struct addrinfo),
               "size mismatch: struct scalanative_addrinfo");

// One could, and arguably should, check the offset of ever field.
// ai_addrlen is the only field whose size is likely to vary across
// operating systems. If the overall size and the offest of
// ai_addr just after ai_addrlen are as expected, the rest will vary
// on only the most perverse system.

_Static_assert(offsetof(struct scalanative_addrinfo, ai_addr) ==
                   offsetof(struct addrinfo, ai_addr),
               "offset mismatch: addrinfo ai_addr");

int scalanative_ai_numerichost() { return AI_NUMERICHOST; }

int scalanative_ai_passive() { return AI_PASSIVE; }

int scalanative_ai_numericserv() { return AI_NUMERICSERV; }

int scalanative_ai_addrconfig() { return AI_ADDRCONFIG; }

int scalanative_ai_v4mapped() { return AI_V4MAPPED; }

int scalanative_ai_canonname() { return AI_CANONNAME; }
