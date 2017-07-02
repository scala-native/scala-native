#include <stdlib.h>
#ifndef _WIN32
#include <grp.h>
#else
#include "os_win_grp.h"
#endif
#include "types.h"

struct scalanative_group {
    const char *gr_name;      /** The name of the group. */
    scalanative_gid_t gr_gid; /** Numerical group ID. */
    const char **gr_mem; /** Pointer to a null-terminated array of character
                       pointers to member names. */
};

void scalanative_group_copy(struct group *group,
                            struct scalanative_group *my_group) {
    my_group->gr_name = group->gr_name;
    my_group->gr_gid = group->gr_gid;
    my_group->gr_mem = group->gr_mem;
}

int scalanative_getgrgid(scalanative_gid_t gid, struct scalanative_group *buf) {
    struct group *group = getgrgid(gid);
    if (group == NULL) {
        return 1;
    } else {
        scalanative_group_copy(group, buf);
        return 0;
    }
}

int scalanative_getgrnam(char *name, struct scalanative_group *buf) {
    struct group *group = getgrnam(name);
    if (group == NULL) {
        return 1;
    } else {
        scalanative_group_copy(group, buf);
        return 0;
    }
}
