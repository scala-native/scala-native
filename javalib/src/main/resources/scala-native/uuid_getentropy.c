#include <errno.h>

#ifdef __APPLE__
#include <sys/random.h>
#endif

#ifndef _WIN32
#include <unistd.h>
#else // _WIN32
// https://docs.microsoft.com/en-us/cpp/c-runtime-library/reference/
//     realloc?view=msvc-170
//     rand-s?view=msvc-170
#define _CRT_RAND_S
#include <stdlib.h>
#include <string.h>
#endif // _WIN32

/* This file is not a candidate for either 'posixlib' or 'clib'.
 * 'getentropy()' and 'rand_s1' are not defined in either POSIX, nor IEEE/ISO.
 */

int scalanative_uuid_getentropy(void *buffer, size_t length, int *error) {

#if defined(__linux__) || defined(__FreeBSD__) || defined(__APPLE__)
    /* Linux:
     * URL https://man7.org/linux/man-pages/man3/getentropy.3.html
     * content dated: 2021-03-22
     * Other BSDs should work but have not been tested.
     *
     * On macOS there is no real need for the overhead of "arc4random()"
     * because only a small number of bytes are being fetched,
     */

    int status = getentropy(buffer, length);

    if (status == -1) {
        *error = errno;
    }

    return status;

#elif defined(_WIN32)
    int nInts = length / sizeof(int);
    int nExcessBytes = length % sizeof(int);
    errno_t status;

    for (int i = 0; i < nInts; i += 1) {
        status = rand_s(buffer + (i * sizeof(int)));
        if (status == -1) {
            *error = status;
            break;
        }
    }

    if ((status == 0) && (nExcessBytes > 0)) {
        int excessBytes = 0;
        status = rand_s(&excessBytes);
        if (status == -1) {
            *error = status;
        } else {
            (void)memcpy(buffer + (length - nExcessBytes), &excessBytes,
                         nExcessBytes);
        }
    }

    return status;

#else
#warning "Unknown OS, not Linux, FreeBSD, macOS, or Windows"
    /* If ENOSYS available on OS, code should compile & and link,
     * but fail unit-test.
     * Compilation failure will focus attention of person porting new OS here.
     */
    *error = ENOSYS;
    return status;
#endif
}
