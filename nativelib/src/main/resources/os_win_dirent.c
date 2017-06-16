#ifdef _WIN32

#include "os_win_dirent.h"

/*
 * Include windows.h without Windows Sockets 1.1 to prevent conflicts with
 * Windows Sockets 2.0.
 */
#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#include <windows.h>

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

_WDIR *_wopendir(const wchar_t *dirname);
struct _wdirent *_wreaddir(_WDIR *dirp);
int _wclosedir(_WDIR *dirp);
void _wrewinddir(_WDIR *dirp);

/* For compatibility with Symbian */
#define wdirent _wdirent
#define WDIR _WDIR
#define wopendir _wopendir
#define wreaddir _wreaddir
#define wclosedir _wclosedir
#define wrewinddir _wrewinddir

DIR *opendir(const char *dirname);
struct dirent *readdir(DIR *dirp);
int closedir(DIR *dirp);
void rewinddir(DIR *dirp);

/* Internal utility functions */
WIN32_FIND_DATAW *dirent_first(_WDIR *dirp);
WIN32_FIND_DATAW *dirent_next(_WDIR *dirp);

int dirent_mbstowcs_s(size_t *pReturnValue, wchar_t *wcstr, size_t sizeInWords,
                      const char *mbstr, size_t count);

int dirent_wcstombs_s(size_t *pReturnValue, char *mbstr, size_t sizeInBytes,
                      const wchar_t *wcstr, size_t count);

void dirent_set_errno(int error);

/*
 * Open directory stream DIRNAME for read and return a pointer to the
 * internal working area that is used to retrieve individual directory
 * entries.
 */
_WDIR *_wopendir(const wchar_t *dirname);

/*
 * Read next directory entry.  The directory entry is returned in dirent
 * structure in the d_name field.  Individual directory entries returned by
 * this function include regular files, sub-directories, pseudo-directories
 * "." and ".." as well as volume labels, hidden files and system files.
 */
struct _wdirent *_wreaddir(_WDIR *dirp);

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
void _wrewinddir(_WDIR *dirp);

/* Get first directory entry (internal) */
WIN32_FIND_DATAW *dirent_first(_WDIR *dirp);

/* Get next directory entry (internal) */
WIN32_FIND_DATAW *dirent_next(_WDIR *dirp);

/* Convert multi-byte string to wide character string */
int dirent_mbstowcs_s(size_t *pReturnValue, wchar_t *wcstr, size_t sizeInWords,
                      const char *mbstr, size_t count);

/* Convert wide-character string to multi-byte string */
int dirent_wcstombs_s(size_t *pReturnValue, char *mbstr,
                      size_t sizeInBytes, /* max size of mbstr */
                      const wchar_t *wcstr, size_t count);

/* Set errno variable */
void dirent_set_errno(int error);

/*
 * Open directory stream DIRNAME for read and return a pointer to the
 * internal working area that is used to retrieve individual directory
 * entries.
 */
_WDIR *_wopendir(const wchar_t *dirname) {
    _WDIR *dirp = NULL;
    int error;

    /* Must have directory name */
    if (dirname == NULL || dirname[0] == '\0') {
        dirent_set_errno(ENOENT);
        return NULL;
    }

    /* Allocate new _WDIR structure */
    dirp = (_WDIR *)malloc(sizeof(struct _WDIR));
    if (dirp != NULL) {
        DWORD n;

        /* Reset _WDIR structure */
        dirp->handle = INVALID_HANDLE_VALUE;
        dirp->patt = NULL;
        dirp->cached = 0;

/* Compute the length of full path plus zero terminator
 *
 * Note that on WinRT there's no way to convert relative paths
 * into absolute paths, so just assume its an absolute path.
 */
#if defined(WINAPI_FAMILY) && (WINAPI_FAMILY == WINAPI_FAMILY_PHONE_APP)
        n = wcslen(dirname);
#else
        n = GetFullPathNameW(dirname, 0, NULL, NULL);
#endif

        /* Allocate room for absolute directory name and search pattern */
        dirp->patt = (wchar_t *)malloc(sizeof(wchar_t) * n + 16);
        if (dirp->patt) {

/*
 * Convert relative directory name to an absolute one.  This
 * allows rewinddir() to function correctly even when current
 * working directory is changed between opendir() and rewinddir().
 *
 * Note that on WinRT there's no way to convert relative paths
 * into absolute paths, so just assume its an absolute path.
 */
#if defined(WINAPI_FAMILY) && (WINAPI_FAMILY == WINAPI_FAMILY_PHONE_APP)
            wcsncpy_s(dirp->patt, n + 1, dirname, n);
#else
            n = GetFullPathNameW(dirname, n, dirp->patt, NULL);
#endif
            if (n > 0) {
                wchar_t *p;

                /* Append search pattern \* to the directory name */
                p = dirp->patt + n;
                if (dirp->patt < p) {
                    switch (p[-1]) {
                    case '\\':
                    case '/':
                    case ':':
                        /* Directory ends in path separator, e.g. c:\temp\ */
                        /*NOP*/;
                        break;

                    default:
                        /* Directory name doesn't end in path separator */
                        *p++ = '\\';
                    }
                }
                *p++ = '*';
                *p = '\0';

                /* Open directory stream and retrieve the first entry */
                if (dirent_first(dirp)) {
                    /* Directory stream opened successfully */
                    error = 0;
                } else {
                    /* Cannot retrieve first entry */
                    error = 1;
                    dirent_set_errno(ENOENT);
                }

            } else {
                /* Cannot retrieve full path name */
                dirent_set_errno(ENOENT);
                error = 1;
            }

        } else {
            /* Cannot allocate memory for search pattern */
            error = 1;
        }

    } else {
        /* Cannot allocate _WDIR structure */
        error = 1;
    }

    /* Clean up in case of error */
    if (error && dirp) {
        _wclosedir(dirp);
        dirp = NULL;
    }

    return dirp;
}

/*
 * Read next directory entry.  The directory entry is returned in dirent
 * structure in the d_name field.  Individual directory entries returned by
 * this function include regular files, sub-directories, pseudo-directories
 * "." and ".." as well as volume labels, hidden files and system files.
 */
struct _wdirent *_wreaddir(_WDIR *dirp) {
    WIN32_FIND_DATAW *datap;
    struct _wdirent *entp;

    /* Read next directory entry */
    datap = dirent_next(dirp);
    if (datap) {
        size_t n;
        DWORD attr;

        /* Pointer to directory entry to return */
        entp = &dirp->ent;

        /*
         * Copy file name as wide-character string.  If the file name is too
         * long to fit in to the destination buffer, then truncate file name
         * to PATH_MAX characters and zero-terminate the buffer.
         */
        n = 0;
        while (n + 1 < PATH_MAX && datap->cFileName[n] != 0) {
            entp->d_name[n] = datap->cFileName[n];
            n++;
        }
        dirp->ent.d_name[n] = 0;

        /* Length of file name excluding zero terminator */
        entp->d_namlen = n;

        /* File type */
        attr = datap->dwFileAttributes;
        if ((attr & FILE_ATTRIBUTE_DEVICE) != 0) {
            entp->d_type = DT_CHR;
        } else if ((attr & FILE_ATTRIBUTE_DIRECTORY) != 0) {
            entp->d_type = DT_DIR;
        } else {
            entp->d_type = DT_REG;
        }

        /* Reset dummy fields */
        entp->d_ino = 0;
        entp->d_reclen = sizeof(struct _wdirent);

    } else {

        /* Last directory entry read */
        entp = NULL;
    }

    return entp;
}

/*
 * Close directory stream opened by opendir() function.  This invalidates the
 * DIR structure as well as any directory entry read previously by
 * _wreaddir().
 */
int _wclosedir(_WDIR *dirp) {
    int ok;
    if (dirp) {

        /* Release search handle */
        if (dirp->handle != INVALID_HANDLE_VALUE) {
            FindClose(dirp->handle);
            dirp->handle = INVALID_HANDLE_VALUE;
        }

        /* Release search pattern */
        if (dirp->patt) {
            free(dirp->patt);
            dirp->patt = NULL;
        }

        /* Release directory structure */
        free(dirp);
        ok = /*success*/ 0;

    } else {
        /* Invalid directory stream */
        dirent_set_errno(EBADF);
        ok = /*failure*/ -1;
    }
    return ok;
}

/*
 * Rewind directory stream such that _wreaddir() returns the very first
 * file name again.
 */
void _wrewinddir(_WDIR *dirp) {
    if (dirp) {
        /* Release existing search handle */
        if (dirp->handle != INVALID_HANDLE_VALUE) {
            FindClose(dirp->handle);
        }

        /* Open new search handle */
        dirent_first(dirp);
    }
}

/* Get first directory entry (internal) */
WIN32_FIND_DATAW *dirent_first(_WDIR *dirp) {
    WIN32_FIND_DATAW *datap;

    /* Open directory and retrieve the first entry */
    dirp->handle = FindFirstFileExW(dirp->patt, FindExInfoStandard, &dirp->data,
                                    FindExSearchNameMatch, NULL, 0);
    if (dirp->handle != INVALID_HANDLE_VALUE) {

        /* a directory entry is now waiting in memory */
        datap = &dirp->data;
        dirp->cached = 1;

    } else {

        /* Failed to re-open directory: no directory entry in memory */
        dirp->cached = 0;
        datap = NULL;
    }
    return datap;
}

/* Get next directory entry (internal) */
WIN32_FIND_DATAW *dirent_next(_WDIR *dirp) {
    WIN32_FIND_DATAW *p;

    /* Get next directory entry */
    if (dirp->cached != 0) {

        /* A valid directory entry already in memory */
        p = &dirp->data;
        dirp->cached = 0;

    } else if (dirp->handle != INVALID_HANDLE_VALUE) {

        /* Get the next directory entry from stream */
        if (FindNextFileW(dirp->handle, &dirp->data) != FALSE) {
            /* Got a file */
            p = &dirp->data;
        } else {
            /* The very last entry has been processed or an error occured */
            FindClose(dirp->handle);
            dirp->handle = INVALID_HANDLE_VALUE;
            p = NULL;
        }

    } else {

        /* End of directory stream reached */
        p = NULL;
    }

    return p;
}

/*
 * Open directory stream using plain old C-string.
 */
DIR *opendir(const char *dirname) {
    struct DIR *dirp;
    int error;

    /* Must have directory name */
    if (dirname == NULL || dirname[0] == '\0') {
        dirent_set_errno(ENOENT);
        return NULL;
    }

    /* Allocate memory for DIR structure */
    dirp = (DIR *)malloc(sizeof(struct DIR));
    if (dirp) {
        wchar_t wname[PATH_MAX];
        size_t n;

        /* Convert directory name to wide-character string */
        error = dirent_mbstowcs_s(&n, wname, PATH_MAX, dirname, PATH_MAX);
        if (!error) {

            /* Open directory stream using wide-character name */
            dirp->wdirp = _wopendir(wname);
            if (dirp->wdirp) {
                /* Directory stream opened */
                error = 0;
            } else {
                /* Failed to open directory stream */
                error = 1;
            }

        } else {
            /*
             * Cannot convert file name to wide-character string.  This
             * occurs if the string contains invalid multi-byte sequences or
             * the output buffer is too small to contain the resulting
             * string.
             */
            error = 1;
        }

    } else {
        /* Cannot allocate DIR structure */
        error = 1;
    }

    /* Clean up in case of error */
    if (error && dirp) {
        free(dirp);
        dirp = NULL;
    }

    return dirp;
}

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
struct dirent *readdir(DIR *dirp) {
    WIN32_FIND_DATAW *datap;
    struct dirent *entp;

    /* Read next directory entry */
    datap = dirent_next(dirp->wdirp);
    if (datap) {
        size_t n;
        int error;

        /* Attempt to convert file name to multi-byte string */
        error = dirent_wcstombs_s(&n, dirp->ent.d_name, PATH_MAX,
                                  datap->cFileName, PATH_MAX);

        /*
         * If the file name cannot be represented by a multi-byte string,
         * then attempt to use old 8+3 file name.  This allows traditional
         * Unix-code to access some file names despite of unicode
         * characters, although file names may seem unfamiliar to the user.
         *
         * Be ware that the code below cannot come up with a short file
         * name unless the file system provides one.  At least
         * VirtualBox shared folders fail to do this.
         */
        if (error && datap->cAlternateFileName[0] != '\0') {
            error = dirent_wcstombs_s(&n, dirp->ent.d_name, PATH_MAX,
                                      datap->cAlternateFileName, PATH_MAX);
        }

        if (!error) {
            DWORD attr;

            /* Initialize directory entry for return */
            entp = &dirp->ent;

            /* Length of file name excluding zero terminator */
            entp->d_namlen = n - 1;

            /* File attributes */
            attr = datap->dwFileAttributes;
            if ((attr & FILE_ATTRIBUTE_DEVICE) != 0) {
                entp->d_type = DT_CHR;
            } else if ((attr & FILE_ATTRIBUTE_DIRECTORY) != 0) {
                entp->d_type = DT_DIR;
            } else {
                entp->d_type = DT_REG;
            }

            /* Reset dummy fields */
            entp->d_ino = 0;
            entp->d_reclen = sizeof(struct dirent);

        } else {
            /*
             * Cannot convert file name to multi-byte string so construct
             * an errornous directory entry and return that.  Note that
             * we cannot return NULL as that would stop the processing
             * of directory entries completely.
             */
            entp = &dirp->ent;
            entp->d_name[0] = '?';
            entp->d_name[1] = '\0';
            entp->d_namlen = 1;
            entp->d_type = DT_UNKNOWN;
            entp->d_ino = 0;
            entp->d_reclen = 0;
        }

    } else {
        /* No more directory entries */
        entp = NULL;
    }

    return entp;
}

/*
 * Close directory stream.
 */
int closedir(DIR *dirp) {
    int ok;
    if (dirp) {

        /* Close wide-character directory stream */
        ok = _wclosedir(dirp->wdirp);
        dirp->wdirp = NULL;

        /* Release multi-byte character version */
        free(dirp);

    } else {

        /* Invalid directory stream */
        dirent_set_errno(EBADF);
        ok = /*failure*/ -1;
    }
    return ok;
}

/*
 * Rewind directory stream to beginning.
 */
void rewinddir(DIR *dirp) {
    /* Rewind wide-character string directory stream */
    _wrewinddir(dirp->wdirp);
}

/* Convert multi-byte string to wide character string */
int dirent_mbstowcs_s(size_t *pReturnValue, wchar_t *wcstr, size_t sizeInWords,
                      const char *mbstr, size_t count) {
    int error;

#if defined(_MSC_VER) && _MSC_VER >= 1400

    /* Microsoft Visual Studio 2005 or later */
    error = mbstowcs_s(pReturnValue, wcstr, sizeInWords, mbstr, count);

#else

    /* Older Visual Studio or non-Microsoft compiler */
    size_t n;

    /* Convert to wide-character string (or count characters) */
    n = mbstowcs(wcstr, mbstr, sizeInWords);
    if (!wcstr || n < count) {

        /* Zero-terminate output buffer */
        if (wcstr && sizeInWords) {
            if (n >= sizeInWords) {
                n = sizeInWords - 1;
            }
            wcstr[n] = 0;
        }

        /* Length of resuting multi-byte string WITH zero terminator */
        if (pReturnValue) {
            *pReturnValue = n + 1;
        }

        /* Success */
        error = 0;

    } else {

        /* Could not convert string */
        error = 1;
    }

#endif

    return error;
}

/* Convert wide-character string to multi-byte string */
int dirent_wcstombs_s(size_t *pReturnValue, char *mbstr,
                      size_t sizeInBytes, /* max size of mbstr */
                      const wchar_t *wcstr, size_t count) {
    int error;

#if defined(_MSC_VER) && _MSC_VER >= 1400

    /* Microsoft Visual Studio 2005 or later */
    error = wcstombs_s(pReturnValue, mbstr, sizeInBytes, wcstr, count);

#else

    /* Older Visual Studio or non-Microsoft compiler */
    size_t n;

    /* Convert to multi-byte string (or count the number of bytes needed) */
    n = wcstombs(mbstr, wcstr, sizeInBytes);
    if (!mbstr || n < count) {

        /* Zero-terminate output buffer */
        if (mbstr && sizeInBytes) {
            if (n >= sizeInBytes) {
                n = sizeInBytes - 1;
            }
            mbstr[n] = '\0';
        }

        /* Length of resulting multi-bytes string WITH zero-terminator */
        if (pReturnValue) {
            *pReturnValue = n + 1;
        }

        /* Success */
        error = 0;

    } else {

        /* Cannot convert string */
        error = 1;
    }

#endif

    return error;
}

/* Set errno variable */
void dirent_set_errno(int error) {
#if defined(_MSC_VER) && _MSC_VER >= 1400

    /* Microsoft Visual Studio 2005 and later */
    _set_errno(error);

#else

    /* Non-Microsoft compiler or older Microsoft compiler */
    errno = error;

#endif
}

int getWinTempDir(char *buffer, size_t length) {
    WCHAR lpTempPathBuffer[MAX_PATH];
    DWORD dwRetVal = GetTempPathW(MAX_PATH, lpTempPathBuffer);
    snprintf(buffer, length, "%ws", lpTempPathBuffer);
    return dwRetVal;
}

const char *realpath(const char *file_name, char *resolved_name) {
    const int cSize = 1024;
    WCHAR pathOut[cSize];
    HANDLE hFile;
    DWORD dwRet;
    wchar_t pathw[cSize];
    size_t outLength = 0;
    mbstowcs_s(&outLength, pathw, cSize, file_name, cSize);

    if (file_name) {
        hFile = CreateFileW(pathw,                      // file to open
                            GENERIC_READ,               // open for reading
                            FILE_SHARE_READ,            // share for reading
                            NULL,                       // default security
                            OPEN_EXISTING,              // existing file only
                            FILE_FLAG_BACKUP_SEMANTICS, // magic flag
                            NULL);                      // no attr. template

        if (hFile == INVALID_HANDLE_VALUE) {
            // printf("Could not open file (error %d\n)", GetLastError());
            resolved_name[0] = 0;
        } else {
            dwRet = GetFinalPathNameByHandleW(hFile, pathOut, cSize,
                                              VOLUME_NAME_DOS);
            wcstombs_s(&outLength, resolved_name, cSize, pathOut, cSize);
            CloseHandle(hFile);
        }
    } else {
        resolved_name[0] = 0;
    }
    return resolved_name;
}

int readlink(const char *path, char *buf, int bufsize) {
    const char *result = realpath(path, buf);
    if (result[0] == 0) {
        return -1;
    } else {
        return strlen(result);
    }
}

int scalanative_getStdHandle(int s) {
    switch (s) {
    case 0:
        return (int)(uintptr_t)GetStdHandle(STD_INPUT_HANDLE);
    case 1:
        return (int)(uintptr_t)GetStdHandle(STD_OUTPUT_HANDLE);
    case 2:
        return (int)(uintptr_t)GetStdHandle(STD_ERROR_HANDLE);
    default:
        break;
    }
    return -1;
}
int fsync(int _FileHandle) {
    if (_FileHandle <= 2) {
        _FileHandle = scalanative_getStdHandle(_FileHandle);
    }
    HANDLE hFile = (HANDLE)(uintptr_t)(_FileHandle);
    return FlushFileBuffers(hFile) ? 0 : -1;
}

#ifdef __cplusplus
}
#endif

#endif