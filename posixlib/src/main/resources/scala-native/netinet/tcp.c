#include <netinet/tcp.h>

int scalanative_tcp_nodelay() { return TCP_NODELAY; }
