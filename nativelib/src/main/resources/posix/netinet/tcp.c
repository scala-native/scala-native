#ifndef _WIN32
#include <netinet/tcp.h>
#else
#include "../../os_win_winsock2.h"
#endif

int scalanative_TCP_NODELAY() { return TCP_NODELAY; }
