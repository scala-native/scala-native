/* This file is not a candidate for either 'posixlib' or 'clib'.
 * 'getentropy()' and 'rand_s' are not defined in either POSIX or IEEE/ISO.
 */

#include <errno.h>

#if defined(__linux__) || defined(__FreeBSD__)
#include <unistd.h>
#elif defined(__APPLE__)
/* getentropy() first appeared in 10.12
 * The proper include should work with the MacOSX10.15.sdk used in CI,
 * but it fails, probably because of ancient bugs in that sdk.
 *
 * The include works just file on Monteray 12.n.
 */
// #include <sys/random.h> // documented include
#include <stddef.h>
int getentropy(void *buffer, size_t size);
#elif defined(_WIN32)
// https://docs.microsoft.com/en-us/cpp/c-runtime-library/reference/
//     realloc?view=msvc-170
//     rand-s?view=msvc-170
#define _CRT_RAND_S
#include <stdlib.h>
#include <string.h>
#else
#warning "Unknown OS: not Linux, FreeBSD, macOS, or Windows"
#endif // OS specific configuration

int scalanative_uuid_getentropy(void *buffer, size_t length, int *error) {

#if defined(__linux__) || defined(__FreeBSD__) || defined(__APPLE__)
    /* Linux:
     * URL https://man7.org/linux/man-pages/man3/getentropy.3.html
     * content dated: 2021-03-22
     * Other BSDs should work but have not been tested.
     *
     * On macOS there is no real need for the overhead of "arc4random()"
     * because only a small number of bytes are being fetched.
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
    /* An application end user using an officially released version
     * of Scala Native should never enter this section.
     * unit-tests would have failed during Continuous Integration,
     * preventing official release.
     *
     * This section exists so that people porting Scala Native
     * to new operating systems are given the opportunity (i.e. forced)
     * to consider the security implications of this method. This is
     * explicit opt-in rather than silently providing something
     * unexamined & untested in that environment.
     *
     * There is an attempt to 'soft fail' in local development environments.
     * so that such developers can skip over the two "#warning"s and
     * link applications, including unit-tests.
     *
     * By intent, if ENOSYS is available, UUIDTest will fail until an
     * implementation for the new  operating system is provided, if only
     * by supplying a suitable '#if defined(__NEW_OS)'. Meanwhile,
     * other tests can run and provide value.
     *
     * If ENOSYS is not available on OS, this branch will not compile,
     * forcing attention to the issue at hand.
     */
    *error = ENOSYS;
    return status;
#endif
}
