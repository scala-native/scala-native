#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_SPAWN)
#if !defined(_WIN32)

#include <spawn.h>

#if !(defined __STDC_VERSION__) || (__STDC_VERSION__ < 201112L)
#ifndef SCALANATIVE_SUPPRESS_STRUCT_CHECK_WARNING
#warning "Size and order of C structures are not checked when -std < c11."
#endif
#else

// posix_spawnattr_t
_Static_assert(sizeof(posix_spawnattr_t) <= 336,
               "Scala Native posix_spawnattr_t is too small");

// posix_spawn_file_actions_t
_Static_assert(sizeof(posix_spawn_file_actions_t) <= 80,
               "Scala Native posix_spawn_file_actions_t is too small");

#endif // __STDC_VERSION__

// Symbolic constants

int scalanative_posix_spawn_posix_spawn_resetids() {
    return POSIX_SPAWN_RESETIDS;
}

int scalanative_posix_spawn_posix_spawn_setpgroup() {
    return POSIX_SPAWN_SETPGROUP;
}

/** PS */
int scalanative_posix_spawn_setschedparam() {
#if defined(__APPLE__)
    return 0; // Unsupported - zero bits set is the "no-op/do-nothing" flag
#else
    return POSIX_SPAWN_SETSCHEDPARAM;
#endif // !__APPLE__
}

/** PS */
int scalanative_posix_spawn_setscheduler() {
#if defined(__APPLE__)
    return 0; // Unsupported - zero bits set is the "no-op/do-nothing" flag
#else
    return POSIX_SPAWN_SETSCHEDULER;
#endif // !__APPLE__
}

int scalanative_posix_spawn_setsigdef() { return POSIX_SPAWN_SETSIGDEF; }

int scalanative_posix_spawn_setsigmask() { return POSIX_SPAWN_SETSIGMASK; }

#endif // Unix or Mac OS
#endif