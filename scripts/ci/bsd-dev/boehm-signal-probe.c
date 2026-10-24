/* Reproduce a test runner overwriting Boehm's FreeBSD stop/resume signals.
 * Also runs on Linux by selecting the same signals before GC initialization.
 */
#define GC_THREADS
#include <gc/gc.h>
#include <pthread.h>
#include <signal.h>
#include <stdatomic.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

static atomic_int ready;
static atomic_int finished;

static void fatal(int signal) { _exit(128 + signal); }

static void *worker(void *unused) {
    (void)unused;
    void *volatile live = GC_MALLOC(128);
    memset(live, 1, 128);
    atomic_store(&ready, 1);
    while (!atomic_load(&finished))
        usleep(1000);
    return live;
}

int main(int argc, char **argv) {
    GC_set_suspend_signal(SIGUSR1);
    GC_set_thr_restart_signal(SIGUSR2);
    GC_INIT();
    pthread_t thread;
    if (pthread_create(&thread, NULL, worker, NULL))
        return 1;
    while (!atomic_load(&ready))
        usleep(1000);
    if (argc > 1 && !strcmp(argv[1], "overwrite")) {
        signal(SIGUSR1, fatal);
        signal(SIGUSR2, fatal);
    }
    GC_gcollect();
    atomic_store(&finished, 1);
    if (pthread_join(thread, NULL))
        return 1;
    puts("PASS: collection completed with Boehm signal handlers preserved");
    return 0;
}
