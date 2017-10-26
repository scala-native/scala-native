#include <pthread.h>
#include <signal.h>
#include <unistd.h>
#include <sys/wait.h>
#include <sys/time.h>

#define RETURN_ON_ERROR(f) do { \
    int res = f; \
    if (res != 0) return res; \
} while (0)

struct proc_info {
    int pid;
    int result;
};
static pthread_mutex_t shared_mutex;
static pthread_mutex_t private_mutex;
static pthread_cond_t cond;
static pthread_t main_thread;
static int last_pid = -1;
static int last_result = -1;

void *wait_loop(void *arg) {
    int status;
    // Block all signals on the monitor thread.
    sigset_t set;
    sigfillset(&set);
    pthread_sigmask(SIG_BLOCK, &set, NULL);

    pthread_mutex_lock(&private_mutex);
    while (1) {
        int pid = waitpid(-1, &status, 0);
        if (pid != -1) {
            pthread_mutex_lock(&shared_mutex);
            last_pid = pid;
            last_result = WIFSIGNALED(status) ? 0x80 + status : status;
            pthread_kill(main_thread, SIGUSR1);
            // Wait until the main thread has handled the signal to start
            // polling for pids again.
            pthread_cond_wait(&cond, &private_mutex);
            pthread_mutex_unlock(&shared_mutex);
        }
    }
    // should be unreachable
    pthread_mutex_unlock(&private_mutex);
    return NULL;
}

pthread_mutex_t *scalanative_process_monitor_shared_mutex() {
    return &shared_mutex;
}

void scalanative_process_monitor_last_proc_info(struct proc_info *info) {
    info->pid = last_pid;
    info->result = last_result;
}

int scalanative_process_monitor_wakeup() {
    RETURN_ON_ERROR(pthread_mutex_lock(&private_mutex));
    RETURN_ON_ERROR(pthread_cond_broadcast(&cond));
    return pthread_mutex_unlock(&private_mutex);
}

void scalanative_process_monitor_init() {
    pthread_t thread;
    pthread_mutex_init(&shared_mutex, NULL);
    pthread_mutex_init(&private_mutex, NULL);
    pthread_cond_init(&cond, NULL);
    pthread_create(&thread, NULL, wait_loop, NULL);
    pthread_detach(thread);
    main_thread = pthread_self();
}
