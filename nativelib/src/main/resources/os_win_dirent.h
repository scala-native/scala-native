/*
 * Dirent interface for Microsoft Visual Studio
 * Version 1.21
 *
 * Copyright (C) 2006-2012 Toni Ronkko
 * This file is part of dirent.  Dirent may be freely distributed
 * under the MIT license.  For all details and documentation, see
 * https://github.com/tronkko/dirent
 */
#ifndef DIRENT_H
#define DIRENT_H

#include <stdio.h>
#include <stdarg.h>
#include <wchar.h>
#include <string.h>
#include <stdlib.h>
#include <malloc.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <errno.h>

/* Indicates that d_type field is available in dirent structure */
#define _DIRENT_HAVE_D_TYPE

/* Indicates that d_namlen field is available in dirent structure */
#define _DIRENT_HAVE_D_NAMLEN

/* Entries missing from MSVC 6.0 */
#if !defined(FILE_ATTRIBUTE_DEVICE)
#define FILE_ATTRIBUTE_DEVICE 0x40
#endif

/* File type and permission flags for stat(), general mask */
#if !defined(S_IFMT)
#define S_IFMT 0x1000F000
#endif

/* Directory bit */
#if !defined(S_IFDIR)
#define S_IFDIR _S_IFDIR
#endif

/* Character device bit */
#if !defined(S_IFCHR)
#define S_IFCHR _S_IFCHR
#endif

/* Pipe bit */
#if !defined(S_IFFIFO)
#define S_IFFIFO _S_IFFIFO
#endif

/* Regular file bit */
#if !defined(S_IFREG)
#define S_IFREG _S_IFREG
#endif

/* Read permission */
#if !defined(S_IREAD)
#define S_IREAD _S_IREAD
#endif

/* Write permission */
#if !defined(S_IWRITE)
#define S_IWRITE _S_IWRITE
#endif

/* Execute permission */
#if !defined(S_IEXEC)
#define S_IEXEC _S_IEXEC
#endif

/* Pipe */
#if !defined(S_IFIFO)
#define S_IFIFO _S_IFIFO
#endif

/* Block device */
#if !defined(S_IFBLK)
#define S_IFBLK 0x10001000
#endif

/* Link */
#if !defined(S_IFLNK)
#define S_IFLNK 0x10002000
#endif

/* Socket */
#if !defined(S_IFSOCK)
#define S_IFSOCK 0x10004000
#endif

/* Read user permission */
#if !defined(S_IRUSR)
#define S_IRUSR _S_IREAD
#endif

/* Write user permission */
#if !defined(S_IWUSR)
#define S_IWUSR _S_IWRITE
#endif

/* Execute user permission */
#if !defined(S_IXUSR)
#define S_IXUSR _S_IEXEC
#endif

/* Read group permission */
#if !defined(S_IRGRP)
#define S_IRGRP 0x00200000
#endif

/* Write group permission */
#if !defined(S_IWGRP)
#define S_IWGRP 0x00100000
#endif

/* Execute group permission */
#if !defined(S_IXGRP)
#define S_IXGRP 0x00080000
#endif

/* Read others permission */
#if !defined(S_IROTH)
#define S_IROTH 0x00040000
#endif

/* Write others permission */
#if !defined(S_IWOTH)
#define S_IWOTH 0x00020000
#endif

/* Execute others permission */
#if !defined(S_IXOTH)
#define S_IXOTH 0x00010000
#endif

#if !defined(S_ISUID)
#define S_ISUID (0x08000000)
#endif

#if !defined(S_ISGID)
#define S_ISGID (0x04000000)
#endif

#if !defined(S_ISVTX)
#define S_ISVTX (0x02000000)
#endif

/* Maximum length of file name */
#if !defined(PATH_MAX)
#define PATH_MAX 260
#endif
#if !defined(FILENAME_MAX)
#define FILENAME_MAX 260
#endif
//#if !defined(NAME_MAX)
//#   define NAME_MAX FILENAME_MAX
//#endif

/* File type flags for d_type */
#define DT_UNKNOWN 0
#define DT_REG S_IFREG
#define DT_DIR S_IFDIR
#define DT_FIFO S_IFIFO
#define DT_SOCK S_IFSOCK
#define DT_CHR S_IFCHR
#define DT_BLK S_IFBLK
#define DT_LNK S_IFLNK

/* Macros for converting between st_mode and d_type */
#define IFTODT(mode) ((mode)&S_IFMT)
#define DTTOIF(type) (type)

/*
 * File type macros.  Note that block devices, sockets and links cannot be
 * distinguished on Windows and the macros S_ISBLK, S_ISSOCK and S_ISLNK are
 * only defined for compatibility.  These macros should always return false
 * on Windows.
 */
#if !defined(S_ISFIFO)
#define S_ISFIFO(mode) (((mode)&S_IFMT) == S_IFIFO)
#endif
#if !defined(S_ISDIR)
#define S_ISDIR(mode) (((mode)&S_IFMT) == S_IFDIR)
#endif
#if !defined(S_ISREG)
#define S_ISREG(mode) (((mode)&S_IFMT) == S_IFREG)
#endif
#if !defined(S_ISLNK)
#define S_ISLNK(mode) (((mode)&S_IFMT) == S_IFLNK)
#endif
#if !defined(S_ISSOCK)
#define S_ISSOCK(mode) (((mode)&S_IFMT) == S_IFSOCK)
#endif
#if !defined(S_ISCHR)
#define S_ISCHR(mode) (((mode)&S_IFMT) == S_IFCHR)
#endif
#if !defined(S_ISBLK)
#define S_ISBLK(mode) (((mode)&S_IFMT) == S_IFBLK)
#endif

/* Return the exact length of d_namlen without zero terminator */
#define _D_EXACT_NAMLEN(p) ((p)->d_namlen)

/* Return number of bytes needed to store d_namlen */
#define _D_ALLOC_NAMLEN(p) (PATH_MAX)

#ifdef __cplusplus
extern "C" {
#endif

/* Multi-byte character versions */
struct dirent {
    /* Always zero */
    long d_ino;

    /* Structure size */
    unsigned short d_reclen;

    /* Length of name without \0 */
    size_t d_namlen;

    /* File type */
    int d_type;

    /* File name */
    char d_name[PATH_MAX];
};
typedef struct dirent dirent;

struct _WDIR;

struct DIR {
    struct dirent ent;
    struct _WDIR *wdirp;
};
typedef struct DIR DIR;

/*
 * Open directory stream using plain old C-string.
 */
DIR *opendir(const char *dirname);

/*
 * Read next directory entry.
 *
 * When working with text consoles, please note that file names returned by
 * readdir() are represented in the default ANSI code page while any output to
 * console is typically formatted on another code page.  Thus, non-ASCII
 * characters in file names will not usually display correctly on console.  The
 * problem can be fixed in two ways: (1) change the character set of console
 * to 1252 using chcp utility and use Lucida Console font, or (2) use
 * _cprintf function when writing to console.  The _cprinf() will re-encode
 * ANSI strings to the console code page so many non-ASCII characters will
 * display correcly.
 */
struct dirent *readdir(DIR *dirp);

/*
 * Close directory stream.
 */
int closedir(DIR *dirp);

/*
 * Rewind directory stream to beginning.
 */
void rewinddir(DIR *dirp);

int getWinTempDir(char *buffer, size_t length);

#ifdef __cplusplus
}
#endif
#endif /*DIRENT_H*/
