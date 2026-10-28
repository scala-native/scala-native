/**
 * Zombie Thread Helper Functions
 *
 * Provides functions to create "zombie" threads for testing GC liveness
 * detection. A zombie thread is one that was registered with GC but terminated
 * without cleanup.
 */

#ifndef _WIN32
#ifndef _POSIX_C_SOURCE
#define _POSIX_C_SOURCE 199309L
#endif
#ifdef __linux__
#define _DEFAULT_SOURCE // syscall
#endif
#endif

#ifdef _WIN32
#include <windows.h>
#else
#include <pthread.h>
#include <time.h>
#ifdef __linux__
#include <sys/syscall.h>
#include <unistd.h>
#endif
#endif

#include <stdio.h>
#include <stdlib.h>

// Exit without GC cleanup. On Linux, pthread_exit's forced unwind crashes
// in the bundled libunwind, so use SYS_exit.
void zombie_thread_exit_no_cleanup(void) {
    fprintf(stderr, "[zombie_thread] Exiting thread without cleanup...\n");
    fflush(stderr);

#ifdef _WIN32
    ExitThread(0);
#elif defined(__linux__)
    syscall(SYS_exit, 0);
#else
    pthread_exit(NULL);
#endif
}

// Keep a Managed mutator that never reaches a safepoint.
void zombie_thread_spin_forever(void) {
    volatile int sink = 0;
    for (;;)
        sink++;
}

/**
 * Sleep in native code without triggering state transitions.
 */
void zombie_thread_native_sleep_ms(int ms) {
#ifdef _WIN32
    Sleep(ms);
#else
    struct timespec ts;
    ts.tv_sec = ms / 1000;
    ts.tv_nsec = (ms % 1000) * 1000000;
    nanosleep(&ts, NULL);
#endif
}
