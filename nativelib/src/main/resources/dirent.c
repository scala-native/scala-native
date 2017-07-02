#ifndef _WIN32
#include <dirent.h>
#else
#include "os_win_dirent.h"
#endif
#include <string.h>
#include "types.h"

#define NAME_MAX 255

struct scalanative_dirent {
    scalanative_uint64_t d_ino; /** file serial number */
    char d_name[NAME_MAX + 1];  /** name of entry */
};

DIR *scalanative_opendir(const char *name) { return opendir(name); }

void scalanative_dirent_init(struct dirent *dirent,
                             struct scalanative_dirent *my_dirent) {
    my_dirent->d_ino = dirent->d_ino;
#ifndef _WIN32
    strncpy(my_dirent->d_name, dirent->d_name, NAME_MAX);
#else
    int nameLength = strlen(dirent->d_name);
    strncpy_s(my_dirent->d_name, NAME_MAX, dirent->d_name, nameLength);
#endif
    my_dirent->d_name[NAME_MAX] = '\0';
}

int scalanative_readdir(DIR *dirp, struct scalanative_dirent *buf) {
    struct dirent *orig_buf = readdir(dirp);
    if (orig_buf != NULL) {
        scalanative_dirent_init(orig_buf, buf);
        return 0;
    } else {
        return 1;
    }
}

int scalanative_closedir(DIR *dirp) { return closedir(dirp); }

int scalanative_gettempdir(char *buffer, size_t length) {
#ifndef _WIN32
    const char *tmpdir = "/tmp";
    strncpy(buffer, tmpdir, length);
    return 0;
#else
    return getWinTempDir(buffer, length);
#endif
}
