#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
#include <sys/utsname.h>
#include <stddef.h>
#include <string.h>

#ifdef SCALANATIVE_UTSNAMELEN
#error "Conflicting prior definition of SCALANATIVE_UTSNAMELEN"
#endif

// For origin of the 256 "magic" number, see comments in utsname.scala
#define SCALANATIVE_UTSNAMELEN 256

struct scalanative_utsname {
    char sysname[SCALANATIVE_UTSNAMELEN];
    char nodename[SCALANATIVE_UTSNAMELEN];
    char release[SCALANATIVE_UTSNAMELEN];
    char version[SCALANATIVE_UTSNAMELEN];
    char machine[SCALANATIVE_UTSNAMELEN];
};

#define SIZEOF_FIELD(t, f) (sizeof(((t *)0)->f))

_Static_assert(SIZEOF_FIELD(struct utsname, sysname) <= SCALANATIVE_UTSNAMELEN,
               "Unexpected size: OS utsname.sysname");

_Static_assert(SIZEOF_FIELD(struct utsname, nodename) <= SCALANATIVE_UTSNAMELEN,
               "Unexpected size: OS utsname.nodename");

_Static_assert(SIZEOF_FIELD(struct utsname, release) <= SCALANATIVE_UTSNAMELEN,
               "Unexpected size: OS utsname.release");

_Static_assert(SIZEOF_FIELD(struct utsname, version) <= SCALANATIVE_UTSNAMELEN,
               "Unexpected size: OS utsname.version");

_Static_assert(SIZEOF_FIELD(struct utsname, machine) <= SCALANATIVE_UTSNAMELEN,
               "Unexpected size: OS utsname.machine");

#define SET_FIELD(dst, src) memccpy(dst, src, 0, SCALANATIVE_UTSNAMELEN)

int scalanative_uname(struct scalanative_utsname *scalanative_utsname) {
    struct utsname utsname;
    int res = uname(&utsname);
    if (res == 0) {
        SET_FIELD(&scalanative_utsname->sysname, utsname.sysname);
        SET_FIELD(&scalanative_utsname->nodename, utsname.nodename);
        SET_FIELD(&scalanative_utsname->release, utsname.release);
        SET_FIELD(&scalanative_utsname->version, utsname.version);
        SET_FIELD(&scalanative_utsname->machine, utsname.machine);
    }
    return res;
}

#undef SCALANATIVE_UTSNAMELEN
#undef SET_FIELD
#undef SIZEOF_FIELD

#endif // Unix or Mac OS
