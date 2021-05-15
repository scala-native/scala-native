#ifndef __SYS_SOCKET_H
#define __SYS_SOCKET_H

typedef unsigned short scalanative_sa_family_t;

struct scalanative_sockaddr {
    scalanative_sa_family_t sa_family;
    char sa_data[14];
};

struct scalanative_sockaddr_storage {
    scalanative_sa_family_t ss_family;
};

struct scalanative_linger {
    int l_onoff;
    int l_linger;
};
#endif // __SYS_SOCKET_H
