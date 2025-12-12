#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_JAVALIB_SPAWN)

#include <spawn.h>

void fileActionsAddChdir(posix_spawn_file_actions_t *actions, char *newCwd) {

#undef SCALANATIVE_JAVALIB_HAVE_POSIX_CHDIR

#if (_POSIX_VERSION >= 202405L) // Open Group Base Specifications Issue 8
#define SCALANATIVE_JAVALIB_HAVE_POSIX_CHDIR 1 // earlier Linux uses _np from

#elif defined(__APPLE__)
#include <Availability.h>
#if __MAC_OS_X_VERSION_MAX_ALLOWED >= 260000 // earlier uses _np form
#define SCALANATIVE_JAVALIB_HAVE_POSIX_CHDIR 1
#endif

#elif defined(__NetBSD__)
#define SCALANATIVE_JAVALIB_HAVE_POSIX_CHDIR 1 // NetBsd 11.00 2020-07-08

#elif defined(__FreeBSD__)
    // FreeBSD up to and including 16.0 use _np form

#elif defined(__OpenBSD__)
#error "posix_spawn_file_actions_addchdir or _np is not available on OpenBSD"

#elif defined(_WIN32)
#error "posix_spawn_file_actions_addchdir is not available on Windows"
#endif

#if defined(SCALANATIVE_JAVALIB_HAVE_POSIX_CHDIR)
    posix_spawn_file_actions_addchdir(actions, newCwd);
#else
    posix_spawn_file_actions_addchdir_np(actions, newCwd);
#endif
}
#endif
