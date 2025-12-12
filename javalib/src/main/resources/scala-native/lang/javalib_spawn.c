#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_JAVALIB_SPAWN)

#include <spawn.h>
#include <stdbool.h>

bool hasFileActionsAddChdir() {
    /* true if either of posix_spawn_file_actions_addchdir() or
     * posix_spawn_file_actions_addchdir_np() is available.
     */

    bool result = false;

#if (_POSIX_VERSION >= 202405L) // Open Group Base Specifications Issue 8
    result = true;

#elif defined(__ILP32__)
    // False

#elif defined(__linux__)
#if defined(__GLIBC__)
#if ((__GLIBC__ > 2) || (__GLIBC__ == 2 && __GLIBC_MINOR >= 29))
    result = true;
#endif
#endif

    /* musl has had the _np form in git since 2019-08-30.
     * Enabling its use is left as an exercise for a musl devotee.
     */

#elif defined(__APPLE__)
#include <Availability.h>

#if (__MAC_OS_X_VERSION_MAX_ALLOWED >= 110100000)
    result = true;
#endif

#elif defined(__NetBSD__)
// Compilation of this branch has not been exercised.
#include <sys/param.h>

#if (__NetBSD_Version__ >= 110000000)
    result = true;
#endif

#elif defined(__FreeBSD__)
    // Compilation of this branch has not been exercised.

#if (__FreeBSD_version >= 1310000)
    result = true;
#endif

#elif defined(__OpenBSD__)
#error "posix_spawn_file_actions_addchdir or _np is not available on OpenBSD"

#elif defined(_WIN32)
#error "posix_spawn_file_actions_addchdir is not available on Windows"
#endif

    return result;
}

void fileActionsAddChdir(posix_spawn_file_actions_t *actions, char *newCwd) {

#undef SCALANATIVE_JAVALIB_HAVE_POSIX_CHDIR

#if (_POSIX_VERSION >= 202405L) // Open Group Base Specifications Issue 8
#define SCALANATIVE_JAVALIB_HAVE_POSIX_CHDIR 1 // earlier Linux uses _np from

#elif defined(__linux__)
    extern int posix_spawn_file_actions_addchdir_np(
        posix_spawn_file_actions_t * __actions, const char *path);

#elif defined(__APPLE__)
#include <Availability.h>

#if __MAC_OS_X_VERSION_MAX_ALLOWED >= 260000
// earlier uses _np form, introduced in approx macOS 11.1, released 2020-12-20
#define SCALANATIVE_JAVALIB_HAVE_POSIX_CHDIR 1
#endif
#elif defined(_WIN32)
#error "posix_spawn_file_actions_addchdir is not available on Windows"
#endif // elif chain

#if defined(__ILP32__)
    // Do nothing, avoid possible link missing symbols. should never get here.
#else // Support 64 bit systems only
#if defined(SCALANATIVE_JAVALIB_HAVE_POSIX_CHDIR)
    posix_spawn_file_actions_addchdir(actions, newCwd);
#else
    posix_spawn_file_actions_addchdir_np(actions, newCwd);
#endif // to _np or not to _np
#endif // only 64 bit support
}
#endif
