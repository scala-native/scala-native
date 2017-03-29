#include <grp.h>
#include <gc.h>
#include "types.h"

struct scalanative_group {
    char *gr_name;            /** The name of the group. */
    scalanative_gid_t gr_gid; /** Numerical group ID. */
    char **gr_mem;            /** Pointer to a null-terminated array of character
                                  pointers to member names. */
};

void scalanative_group_init(struct group *group, struct scalanative_group *my_group) {
    my_group->gr_name = group->gr_name;
    my_group->gr_gid  = group->gr_gid;
    my_group->gr_mem  = group->gr_mem;
}

struct scalanative_group *scalanative_group_copy(struct group *group) {
    struct scalanative_group *my_group =
        (struct scalanative_group *) GC_malloc(sizeof(struct scalanative_group));
    scalanative_group_init(group, my_group);
    return my_group;
}

struct scalanative_group *scalanative_getgrgid(scalanative_gid_t gid) {
    // TODO: scalanative_gid_t may be different from gid_t!!!
    return scalanative_group_copy(getgrgid(gid));
}

struct scalanative_group *scalanative_getgrnam(char *name) {
    return scalanative_group_copy(getgrnam(name));
}
