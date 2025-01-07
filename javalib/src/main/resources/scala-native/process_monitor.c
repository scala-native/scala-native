#if defined(SCALANATIVE_COMPILE_ALWAYS) ||                                     \
    defined(__SCALANATIVE_JAVALIB_PROCESS_MONITOR)
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))

#include <pthread.h>
#include <sys/wait.h>
#include <sys/time.h>
#include <semaphore.h>
#include <errno.h>
#include <sys/mman.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <string.h>

// The +1 accounts for the null char at the end of the name
#ifdef __APPLE__
#include <sys/posix_sem.h>
#define SEM_MAX_LENGTH PSEMNAMLEN + 1
#else
#include <limits.h>
#define SEM_MAX_LENGTH _POSIX_PATH_MAX + 1
#endif

typedef struct Monitor {
    pthread_cond_t cond;
    int *res;
} Monitor;

typedef struct WaitingProc {
    int pid;
    Monitor *monitor;
    struct WaitingProc *next;
} WaitingProc;

typedef struct FinishedProc {
    int pid;
    int result;
    struct FinishedProc *next;
} FinishedProc;

static pthread_mutex_t shared_mutex;
static WaitingProc *waiting_procs = NULL;
static FinishedProc *finished_procs = NULL;
static sem_t *active_procs;

static void add_finished_proc(int pid, int result) {
    FinishedProc *new_proc = (FinishedProc *)malloc(sizeof(FinishedProc));
    new_proc->pid = pid;
    new_proc->result = result;
    new_proc->next = finished_procs;
    finished_procs = new_proc;
}

static Monitor *get_or_create_monitor(int pid, int *proc_res) {
    WaitingProc **curr = &waiting_procs;
    while (*curr) {
        if ((*curr)->pid == pid) {
            return (*curr)->monitor;
        }
        curr = &(*curr)->next;
    }
    Monitor *new_monitor = (Monitor *)malloc(sizeof(Monitor));
    pthread_cond_init(&new_monitor->cond, NULL);
    new_monitor->res = proc_res;

    WaitingProc *new_proc = (WaitingProc *)malloc(sizeof(WaitingProc));
    new_proc->pid = pid;
    new_proc->monitor = new_monitor;
    new_proc->next = waiting_procs;
    waiting_procs = new_proc;

    return new_monitor;
}

static void remove_waiting_proc(int pid) {
    WaitingProc **curr = &waiting_procs;
    while (*curr) {
        if ((*curr)->pid == pid) {
            Monitor *to_free_monitor = (*curr)->monitor;
            pthread_cond_destroy(&to_free_monitor->cond);
            free(to_free_monitor);

            WaitingProc *to_free_proc = *curr;
            *curr = (*curr)->next;
            free(to_free_proc);
            return;
        }
        curr = &(*curr)->next;
    }
}

static void *wait_loop(void *arg) {
    while (1) {
        int wait_result;
        do {
            wait_result = sem_wait(active_procs);
        } while (wait_result == -1 && errno == EINTR);

        int status;
        int pid = waitpid(-1, &status, 0);
        if (pid != -1) {
            pthread_mutex_lock(&shared_mutex);

            int last_result = status;

            if (WIFEXITED(status)) {
                last_result = WEXITSTATUS(status);
            } else if (WIFSIGNALED(status)) {
                last_result = 0x80 + WTERMSIG(status);
            }

            WaitingProc **proc = &waiting_procs;
            while (*proc) {
                if ((*proc)->pid == pid) {
                    Monitor *monitor = (*proc)->monitor;
                    *monitor->res = last_result;
                    pthread_cond_broadcast(&monitor->cond);
                    remove_waiting_proc(pid);
                    break;
                }
                proc = &(*proc)->next;
            }
            if (!(*proc)) {
                add_finished_proc(pid, last_result);
            }

            pthread_mutex_unlock(&shared_mutex);
        }
    }
    return NULL;
}

// The shared lock must be passed into this function for thread-safety.
static int check_result(const int pid, pthread_mutex_t *lock) {
    FinishedProc **curr = &finished_procs;
    while (*curr) {
        if ((*curr)->pid == pid) {
            int result = (*curr)->result;
            FinishedProc *to_free = *curr;
            *curr = (*curr)->next;
            free(to_free);
            return result;
        }
        curr = &(*curr)->next;
    }
    return -1;
}

void scalanative_process_monitor_notify() { sem_post(active_procs); }

int scalanative_process_monitor_check_result(const int pid) {
    pthread_mutex_lock(&shared_mutex);
    int res = check_result(pid, &shared_mutex);
    pthread_mutex_unlock(&shared_mutex);
    return res;
}

int scalanative_process_monitor_wait_for_pid(const int pid, struct timespec *ts,
                                             int *proc_res) {
    pthread_mutex_lock(&shared_mutex);
    int result = check_result(pid, &shared_mutex);
    if (result != -1) {
        *proc_res = result;
        pthread_mutex_unlock(&shared_mutex);
        return 0;
    }

    Monitor *monitor = get_or_create_monitor(pid, proc_res);
    int res = ts ? pthread_cond_timedwait(&monitor->cond, &shared_mutex, ts)
                 : pthread_cond_wait(&monitor->cond, &shared_mutex);
    pthread_mutex_unlock(&shared_mutex);
    return res;
}

void scalanative_process_monitor_init() {
    pthread_t thread;
    pthread_mutex_init(&shared_mutex, NULL);

    char semaphoreName[SEM_MAX_LENGTH];
#if defined(__FreeBSD__) || defined(__NetBSD__)
#define SEM_NAME_PREFIX "/"
#else
#define SEM_NAME_PREFIX ""
#endif
    snprintf(semaphoreName, SEM_MAX_LENGTH,
             SEM_NAME_PREFIX "__sn_%d-process-monitor", getpid());
    active_procs = sem_open(semaphoreName, O_CREAT | O_EXCL, 0644, 0);
    if (active_procs == SEM_FAILED) {
        perror("Failed to create or open process monitor semaphore");
        exit(errno);
    }

    pthread_create(&thread, NULL, wait_loop, NULL);
    pthread_detach(thread);
}
#endif // Unix or Mac OS
#endif // __SCALANATIVE_PROCESS_MONITOR
