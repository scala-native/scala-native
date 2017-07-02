#include <stdlib.h>
#ifndef _WIN32
#include <pwd.h>
#else
#include "os_win_pwd.h"
#endif
#include "types.h"

struct scalanative_passwd {
    const char *pw_name;      /** User's login name. */
    scalanative_uid_t pw_uid; /** Numerical user ID. */
    scalanative_gid_t pw_gid; /** Numerical group ID. */
    const char *pw_dir;       /** Initial working directory. */
    const char *pw_shell;     /** Program to use as shell. */
};

void scalanative_passwd_copy(struct passwd *passwd,
                             struct scalanative_passwd *my_passwd) {
    my_passwd->pw_name = passwd->pw_name;
    my_passwd->pw_uid = passwd->pw_uid;
    my_passwd->pw_gid = passwd->pw_gid;
    my_passwd->pw_dir = passwd->pw_dir;
    my_passwd->pw_shell = passwd->pw_shell;
}
int scalanative_getpwuid(scalanative_uid_t uid,
                         struct scalanative_passwd *buf) {
    struct passwd *passwd = getpwuid(uid);

    if (passwd == NULL) {
        return 1;
    } else {
        scalanative_passwd_copy(passwd, buf);
        return 0;
    }
}

int scalanative_getpwnam(char *name, struct scalanative_passwd *buf) {
    struct passwd *passwd = getpwnam(name);

    if (passwd == NULL) {
        return 1;
    } else {
        scalanative_passwd_copy(passwd, buf);
        return 0;
    }
}
