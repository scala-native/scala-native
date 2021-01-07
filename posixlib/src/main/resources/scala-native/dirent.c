#include <dirent.h>
#include <string.h>
#include <stdio.h>
#include <errno.h>

#define NAME_MAX 255

struct scalanative_dirent {
    unsigned long long d_ino;  /** file serial number */
    char d_name[NAME_MAX + 1]; /** name of entry */
    short d_type;
};

int scalanative_dt_unknown() { return DT_UNKNOWN; }
int scalanative_dt_fifo() { return DT_FIFO; }
int scalanative_dt_chr() { return DT_CHR; }
int scalanative_dt_dir() { return DT_DIR; }
int scalanative_dt_blk() { return DT_BLK; }
int scalanative_dt_reg() { return DT_REG; }
int scalanative_dt_lnk() { return DT_LNK; }
int scalanative_dt_sock() { return DT_SOCK; }
int scalanative_dt_wht() { return DT_WHT; }

DIR *scalanative_opendir(const char *name) { return opendir(name); }

void scalanative_dirent_init(struct dirent *dirent,
                             struct scalanative_dirent *my_dirent) {
    my_dirent->d_ino = dirent->d_ino;
    strncpy(my_dirent->d_name, dirent->d_name, NAME_MAX);
    my_dirent->d_name[NAME_MAX] = '\0';
    my_dirent->d_type = dirent->d_type;
}

int scalanative_readdir(DIR *dirp, struct scalanative_dirent *buf) {
    errno = 0;
    struct dirent *orig_buf = readdir(dirp);
    if (orig_buf != NULL) {
        scalanative_dirent_init(orig_buf, buf);
        return 0;
    } else {
        switch (errno) {
        case EBADF:
            return EBADF;
        case EFAULT:
            return EFAULT;
        case EIO:
            return EIO;
        }
        return -1;
    }
}

int scalanative_closedir(DIR *dirp) { return closedir(dirp); }
