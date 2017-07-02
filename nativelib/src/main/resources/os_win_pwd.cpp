#ifdef _WIN32

#include "os_win_pwd.h"
#include <string>
#include <vector>

namespace {
struct passwdInternal {
    std::string pw_name;  /** User's login name. */
    uid_t pw_uid;         /** Numerical user ID. */
    gid_t pw_gid;         /** Numerical group ID. */
    std::string pw_dir;   /** Initial working directory. */
    std::string pw_shell; /** Program to use as shell. */

    passwd *getPasswd() {
        p.pw_name = pw_name.data();
        p.pw_uid = pw_uid;
        p.pw_gid = pw_gid;
        p.pw_dir = pw_dir.data();
        p.pw_shell = pw_shell.data();
        return &p;
    }

  private:
    passwd p;
};

std::vector<passwdInternal> &users() {
    static std::vector<passwdInternal> db;
    return db;
}

passwdInternal &getInternalUser(uid_t idx) {
    auto &db = users();
    if (idx >= db.size()) {
        const bool first = db.size() == 0;
        db.resize(idx + 1);
        if (first) {
            db[0].pw_name = "anone";
            db[0].pw_uid = 0;
            db[0].pw_gid = 0;
        }
    }
    return db[idx];
}

passwdInternal &findInternalUser(const char *name) {
    auto &db = users();
    for (auto &u : db) {
        if (u.pw_name == name) {
            return u;
        }
    }
    return db[0];
}
}

extern "C" struct passwd *getpwuid(uid_t uid) {
    return getInternalUser(uid).getPasswd();
}

extern "C" struct passwd *getpwnam(const char *name) {
    return findInternalUser(name).getPasswd();
}

#endif