#include <sys/utsname.h>
#include <string.h>

#define NAMELEN 256
struct scalanative_utsname {
    char sysname[NAMELEN];
    char nodename[NAMELEN];
    char release[NAMELEN];
    char version[NAMELEN];
    char machine[NAMELEN];
};
#undef NAMELEN
#define SET_FIELD(x, y)                                                        \
    do {                                                                       \
        int len = strlen(y);                                                   \
        memcpy(x, y, len);                                                     \
    } while (0);

int scalanative_uname(struct scalanative_utsname *scalanative_utsname) {
    struct utsname utsname;
    int res = uname(&utsname);
    if (res == 0) {
        SET_FIELD(&scalanative_utsname->sysname, utsname.sysname)
        SET_FIELD(&scalanative_utsname->nodename, utsname.nodename)
        SET_FIELD(&scalanative_utsname->release, utsname.release)
        SET_FIELD(&scalanative_utsname->version, utsname.version)
        SET_FIELD(&scalanative_utsname->machine, utsname.machine)
    }
    return res;
}
