/**
 * Zombie Thread Helper Functions
 *
 * Provides functions to create "zombie" threads for testing GC liveness
 * detection. A zombie thread is one that was registered with GC but terminated
 * without cleanup.
 */

#ifdef _WIN32
#include <windows.h>
#else
#include <pthread.h>
#include <unistd.h>
#endif

#include <stdio.h>
#include <stdlib.h>

/**
 * Exit the current thread WITHOUT calling MutatorThread_delete.
 * This creates a "zombie" - a thread that appears in GC's list but is dead.
 *
 * We detach the thread first so that pthread_kill(thread, 0) will
 * return ESRCH after the thread exits, allowing liveness detection.
 */
void zombie_thread_exit_no_cleanup(void) {
    fprintf(stderr, "[zombie_thread] Exiting thread without cleanup...\n");
    fflush(stderr);

#ifdef _WIN32
    ExitThread(0);
#else
    // Detach so resources are reclaimed immediately
    // Makes pthread_kill(thread, 0) return ESRCH after exit
    pthread_detach(pthread_self());
    pthread_exit(NULL);
#endif
}

/**
 * Sleep in native code without triggering state transitions.
 */
void zombie_thread_native_sleep_ms(int ms) {
#ifdef _WIN32
    Sleep(ms);
#else
    usleep(ms * 1000);
#endif
}
