#ifndef _WIN32
#include <arpa/inet.h>
#else
#include "../../os_win_winsock2.h"
#endif
#include "../netinet/in.h"

uint32_t scalanative_htonl(uint32_t arg) { return htonl(arg); }

uint16_t scalanative_htons(uint16_t arg) { return htons(arg); }

uint32_t scalanative_ntohl(uint32_t arg) { return ntohl(arg); }

uint16_t scalanative_ntohs(uint16_t arg) { return ntohs(arg); }

char *scalanative_inet_ntoa(struct scalanative_in_addr *in) {
    struct in_addr converted;
    scalanative_convert_in_addr(in, &converted);
#ifndef _WIN32
    return inet_ntoa(converted);
#else
    return os_win_inet_ntoa(AF_INET, &converted);
#endif
}

in_addr_t scalanative_inet_addr(char *in) {
#ifndef _WIN32
    return inet_addr(in);
#else
    return os_win_inet_addr4(in);
#endif
}
