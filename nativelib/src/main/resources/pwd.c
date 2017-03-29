#include <pwd.h>
#include <gc.h>
#include "types.h"

struct scalanative_passwd {
    char *pw_name;            /** User's login name. */
    scalanative_uid_t pw_uid; /** Numerical user ID. */
    scalanative_gid_t pw_gid; /** Numerical group ID. */
    char *pw_dir;             /** Initial working directory. */
    char *pw_shell;           /** Program to use as shell. */
};

void scalanative_passwd_init(struct passwd *passwd, struct scalanative_passwd *my_passwd) {
    my_passwd->pw_name  = passwd->pw_name;
    my_passwd->pw_uid   = passwd->pw_uid;
    my_passwd->pw_gid   = passwd->pw_gid;
    my_passwd->pw_dir   = passwd->pw_dir;
    my_passwd->pw_shell = passwd->pw_shell;
}

struct scalanative_passwd *scalanative_passwd_copy(struct passwd *passwd) {
    struct scalanative_passwd *my_passwd =
        (struct scalanative_passwd *) GC_malloc(sizeof(struct scalanative_passwd));
    scalanative_passwd_init(passwd, my_passwd);
    return my_passwd;
}

struct scalanative_passwd *scalanative_getpwuid(scalanative_uid_t uid) {
    // TODO: scalanative_uid_t may be different from uid_t!!!
    return scalanative_passwd_copy(getpwuid(uid));
}

struct scalanative_passwd *scalanative_getpwnam(char *name) {
    return scalanative_passwd_copy(getpwnam(name));
}

