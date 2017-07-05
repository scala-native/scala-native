#ifndef _OS_WIN_GRP_H_
#define _OS_WIN_GRP_H_

#ifdef __cplusplus
extern "C" {
#endif

struct group {
    char *gr_name;       /** The name of the group. */
    unsigned int gr_gid; /** Numerical group ID. */
    char **gr_mem;       /** Pointer to a null-terminated array of character
                             pointers to member names. */
};

typedef unsigned int gid_t;
struct group *getgrgid(gid_t gid);
struct group *getgrnam(const char *groupname);

#ifdef __cplusplus
}
#endif

#endif