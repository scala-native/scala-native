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

/*
 * Include windows.h without Windows Sockets 1.1 to prevent conflicts with
 * Windows Sockets 2.0.
 */
#ifndef WIN32_LEAN_AND_MEAN
#   define WIN32_LEAN_AND_MEAN
#endif
#include <windows.h>

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
#   define FILE_ATTRIBUTE_DEVICE 0x40
#endif

/* File type and permission flags for stat(), general mask */
#if !defined(S_IFMT)
#   define S_IFMT _S_IFMT
#endif

/* Directory bit */
#if !defined(S_IFDIR)
#   define S_IFDIR _S_IFDIR
#endif

/* Character device bit */
#if !defined(S_IFCHR)
#   define S_IFCHR _S_IFCHR
#endif

/* Pipe bit */
#if !defined(S_IFFIFO)
#   define S_IFFIFO _S_IFFIFO
#endif

/* Regular file bit */
#if !defined(S_IFREG)
#   define S_IFREG _S_IFREG
#endif

/* Read permission */
#if !defined(S_IREAD)
#   define S_IREAD _S_IREAD
#endif

/* Write permission */
#if !defined(S_IWRITE)
#   define S_IWRITE _S_IWRITE
#endif

/* Execute permission */
#if !defined(S_IEXEC)
#   define S_IEXEC _S_IEXEC
#endif

/* Pipe */
#if !defined(S_IFIFO)
#   define S_IFIFO _S_IFIFO
#endif

/* Block device */
#if !defined(S_IFBLK)
#   define S_IFBLK 0
#endif

/* Link */
#if !defined(S_IFLNK)
#   define S_IFLNK 0
#endif

/* Socket */
#if !defined(S_IFSOCK)
#   define S_IFSOCK 0
#endif

/* Read user permission */
#if !defined(S_IRUSR)
#   define S_IRUSR _S_IREAD
#endif

/* Write user permission */
#if !defined(S_IWUSR)
#   define S_IWUSR _S_IWRITE
#endif

/* Execute user permission */
#if !defined(S_IXUSR)
#   define S_IXUSR _S_IEXEC
#endif

/* Read group permission */
#if !defined(S_IRGRP)
#   define S_IRGRP 0x00200000
#endif

/* Write group permission */
#if !defined(S_IWGRP)
#   define S_IWGRP 0x00100000
#endif

/* Execute group permission */
#if !defined(S_IXGRP)
#   define S_IXGRP 0x00080000
#endif

/* Read others permission */
#if !defined(S_IROTH)
#   define S_IROTH 0x00040000
#endif

/* Write others permission */
#if !defined(S_IWOTH)
#   define S_IWOTH 0x00020000
#endif

/* Execute others permission */
#if !defined(S_IXOTH)
#   define S_IXOTH 0x00010000
#endif

#if !defined(S_ISUID)
#   define S_ISUID (0x08000000)
#endif

#if !defined(S_ISGID)
#   define S_ISGID (0x04000000)
#endif

#if !defined(S_ISVTX)
#   define S_ISVTX (0x02000000)
#endif

/* Maximum length of file name */
#if !defined(PATH_MAX)
#   define PATH_MAX MAX_PATH
#endif
#if !defined(FILENAME_MAX)
#   define FILENAME_MAX MAX_PATH
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
#define IFTODT(mode) ((mode) & S_IFMT)
#define DTTOIF(type) (type)

/*
 * File type macros.  Note that block devices, sockets and links cannot be
 * distinguished on Windows and the macros S_ISBLK, S_ISSOCK and S_ISLNK are
 * only defined for compatibility.  These macros should always return false
 * on Windows.
 */
#if !defined(S_ISFIFO)
#   define S_ISFIFO(mode) (((mode) & S_IFMT) == S_IFIFO)
#endif
#if !defined(S_ISDIR)
#   define S_ISDIR(mode) (((mode) & S_IFMT) == S_IFDIR)
#endif
#if !defined(S_ISREG)
#   define S_ISREG(mode) (((mode) & S_IFMT) == S_IFREG)
#endif
#if !defined(S_ISLNK)
#   define S_ISLNK(mode) (((mode) & S_IFMT) == S_IFLNK)
#endif
#if !defined(S_ISSOCK)
#   define S_ISSOCK(mode) (((mode) & S_IFMT) == S_IFSOCK)
#endif
#if !defined(S_ISCHR)
#   define S_ISCHR(mode) (((mode) & S_IFMT) == S_IFCHR)
#endif
#if !defined(S_ISBLK)
#   define S_ISBLK(mode) (((mode) & S_IFMT) == S_IFBLK)
#endif

/* Return the exact length of d_namlen without zero terminator */
#define _D_EXACT_NAMLEN(p) ((p)->d_namlen)

/* Return number of bytes needed to store d_namlen */
#define _D_ALLOC_NAMLEN(p) (PATH_MAX)


#ifdef __cplusplus
extern "C" {
#endif


/* Wide-character version */
struct _wdirent {
    /* Always zero */
    long d_ino;

    /* Structure size */
    unsigned short d_reclen;

    /* Length of name without \0 */
    size_t d_namlen;

    /* File type */
    int d_type;

    /* File name */
    wchar_t d_name[PATH_MAX];
};
typedef struct _wdirent _wdirent;

struct _WDIR {
    /* Current directory entry */
    struct _wdirent ent;

    /* Private file data */
    WIN32_FIND_DATAW data;

    /* True if data is valid */
    int cached;

    /* Win32 search handle */
    HANDLE handle;

    /* Initial directory name */
    wchar_t *patt;
};
typedef struct _WDIR _WDIR;

_WDIR *_wopendir (const wchar_t *dirname);
struct _wdirent *_wreaddir (_WDIR *dirp);
int _wclosedir (_WDIR *dirp);
void _wrewinddir (_WDIR* dirp);


/* For compatibility with Symbian */
#define wdirent _wdirent
#define WDIR _WDIR
#define wopendir _wopendir
#define wreaddir _wreaddir
#define wclosedir _wclosedir
#define wrewinddir _wrewinddir


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

struct DIR {
    struct dirent ent;
    struct _WDIR *wdirp;
};
typedef struct DIR DIR;

DIR *opendir (const char *dirname);
struct dirent *readdir (DIR *dirp);
int closedir (DIR *dirp);
void rewinddir (DIR* dirp);


/* Internal utility functions */
WIN32_FIND_DATAW *dirent_first (_WDIR *dirp);
WIN32_FIND_DATAW *dirent_next (_WDIR *dirp);

int dirent_mbstowcs_s(
    size_t *pReturnValue,
    wchar_t *wcstr,
    size_t sizeInWords,
    const char *mbstr,
    size_t count);

int dirent_wcstombs_s(
    size_t *pReturnValue,
    char *mbstr,
    size_t sizeInBytes,
    const wchar_t *wcstr,
    size_t count);

void dirent_set_errno (int error);

/*
 * Open directory stream DIRNAME for read and return a pointer to the
 * internal working area that is used to retrieve individual directory
 * entries.
 */
_WDIR* _wopendir(const wchar_t *dirname);

/*
 * Read next directory entry.  The directory entry is returned in dirent
 * structure in the d_name field.  Individual directory entries returned by
 * this function include regular files, sub-directories, pseudo-directories
 * "." and ".." as well as volume labels, hidden files and system files.
 */
struct _wdirent* _wreaddir(_WDIR *dirp);

/*
 * Close directory stream opened by opendir() function.  This invalidates the
 * DIR structure as well as any directory entry read previously by
 * _wreaddir().
 */
int _wclosedir(_WDIR *dirp);

/*
 * Rewind directory stream such that _wreaddir() returns the very first
 * file name again.
 */
void _wrewinddir(_WDIR* dirp);

/* Get first directory entry (internal) */
WIN32_FIND_DATAW* dirent_first(_WDIR *dirp);

/* Get next directory entry (internal) */
WIN32_FIND_DATAW* dirent_next(_WDIR *dirp);

/* 
 * Open directory stream using plain old C-string.
 */
DIR* opendir(const char *dirname);

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
struct dirent* readdir(DIR *dirp);

/*
 * Close directory stream.
 */
int closedir(DIR *dirp);

/*
 * Rewind directory stream to beginning.
 */
void rewinddir(DIR* dirp);

/* Convert multi-byte string to wide character string */
int
dirent_mbstowcs_s(
    size_t *pReturnValue,
    wchar_t *wcstr,
    size_t sizeInWords,
    const char *mbstr,
    size_t count);

/* Convert wide-character string to multi-byte string */
int dirent_wcstombs_s(
    size_t *pReturnValue,
    char *mbstr,
    size_t sizeInBytes, /* max size of mbstr */
    const wchar_t *wcstr,
    size_t count);

/* Set errno variable */
void dirent_set_errno(int error);

int getWinTempDir(char *buffer, size_t length);


#ifdef __cplusplus
}
#endif
#endif /*DIRENT_H*/

