#ifndef _OS_WIN_PWD_H_
#define _OS_WIN_PWD_H_

#ifdef __cplusplus
extern "C" {
#endif

typedef unsigned int uid_t;
typedef unsigned int gid_t;

struct passwd {
    const char *pw_name;  /** User's login name. */
    uid_t pw_uid;         /** Numerical user ID. */
    gid_t pw_gid;         /** Numerical group ID. */
    const char *pw_dir;   /** Initial working directory. */
    const char *pw_shell; /** Program to use as shell. */
};

struct passwd *getpwuid(uid_t uid);
struct passwd *getpwnam(const char *name);

#ifdef __cplusplus
}
#endif

#endif