#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_DIRENT)
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
#include <dirent.h>
#include <string.h>
#include <stdio.h>
#include <errno.h>

// Check consistency of Scala Native and operating system definitions.

_Static_assert(sizeof(ino_t) <= 64,
               "os ino_t does not match Scala Native dirent");

_Static_assert(sizeof(((struct dirent *)0)->d_type) <= 2,
               "os dirent.d_type does not match Scala Native dirent");

#ifdef NAME_MAX
/* TL;DR - The Scala Native d_name definition should be changed to CString
 *         as soon as breaking changes are allowed. That punts allocation
 *         of storage with sufficient size for a file name to the
 *         operating system, which knows best.
 *
 *         Scala Native can always to a strlen/strnlen of the returned
 *         string and allocate, at its level, the exact required size.
 *         Scala Native code while almost always be copying the os d_name
 *         to a Scala 'String'. fromCString(os_d_name) can be done
 *         directly, and does not require an intermediate allocation and
 *         copy
 *
 * In 2024 and possibly much earlier, linux & FreeBSD  "getconf NAME_MAX /"
 * return 255 for the file systems the most commonly used on those operating
 * systems. They may or may not define NAME_MAX and readdir() may or
 * may not return strings longer that that; long story.
 *
 * macOS limits need to be explored. It specifies a max of 255 UTF-8
 * characters. Since each UTF-8 character can be up to 5 bytes long,
 * a 255 UTF-8 characters could be (255 * 5 == 1275) bytes.
 *
 * Then again, there are file systems, such as Amazon S3 which has a
 * file name length limit of 1024 characters)
 *
 */
_Static_assert(NAME_MAX <= 255, "NAME_MAX does not match Scala Native dirent");
#else
#define NAME_MAX 255 // manually match dirent.scala definition
#endif

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
int scalanative_dt_wht() {
#ifdef DW_WHT
    return DT_WHT;
#else
    return 0;
#endif
}

DIR *scalanative_opendir(const char *name) { return opendir(name); }

void scalanative_dirent_init(struct dirent *dirent,
                             struct scalanative_dirent *my_dirent) {
    my_dirent->d_ino = dirent->d_ino;

    // Note: code will _silently_ truncate any long d_name returned by OS.
    strncpy(my_dirent->d_name, dirent->d_name, NAME_MAX);
    my_dirent->d_name[NAME_MAX] = '\0';
    my_dirent->d_type = dirent->d_type;
}

// This function is for Scala Native internal use only.

// returns 0 in case of success, -1 in case of empty dir, errno otherwise
int scalanative_readdirImpl(DIR *dirp, struct scalanative_dirent *buf) {
    errno = 0;
    struct dirent *orig_buf = readdir(dirp);
    int result = 0;

    if (orig_buf != NULL)
        scalanative_dirent_init(orig_buf, buf);
    else if (errno == 0)
        result = -1;
    else
        result = errno;

    return result;
}
/* This function is for Scala Native internal use only.
 * It is provided to preserve the entry point and prevent a breaking
 * change in 0.5.2. To reduce enduring & compounding complexity, it should
 * be removed for 0.6.0.
 *
 * No Scala Native code defines or calls it.
 */

int scalanative_readdir(DIR *dirp, struct scalanative_dirent *buf) {
    scalanative_readdirImpl(dirp, buf);
}

int scalanative_closedir(DIR *dirp) { return closedir(dirp); }

#endif // Unix or Mac OS
#endif
