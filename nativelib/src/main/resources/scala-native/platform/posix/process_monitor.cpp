// This mechanism is only used in POSIX compliant platforms.
// On Windows other build in approach is used.
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
#include <memory>
#include <pthread.h>
#include <sys/wait.h>
#include <sys/time.h>
#include <unordered_map>
#include <semaphore.h>
#include <errno.h>
#include <sys/mman.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>

// The +1 accounts for the null char at the end of the name
#ifdef __APPLE__
#include <sys/posix_sem.h>
#define SEM_MAX_LENGTH PSEMNAMLEN + 1
#else
#include <limits.h>
#define SEM_MAX_LENGTH _POSIX_PATH_MAX + 1
#endif

struct Monitor {
  public:
    pthread_cond_t *cond;
    int *res;
    Monitor(int *res) : res(res) {
        cond = new pthread_cond_t;
        pthread_cond_init(cond, NULL);
    }
    ~Monitor() {
        pthread_cond_destroy(cond);
        delete cond;
    }
};

static pthread_mutex_t shared_mutex;
static std::unordered_map<int, std::shared_ptr<Monitor>> waiting_procs;
static std::unordered_map<int, int> finished_procs;
static sem_t *active_procs;

static void *wait_loop(void *arg) {
    while (1) {

        // Wait until there is at least 1 active process
        int wait_result;
        do {
            wait_result = sem_wait(active_procs);
        } while (wait_result == -1 && errno == EINTR);

        int status;
        const int pid = waitpid(-1, &status, 0);
        if (pid != -1) {
            pthread_mutex_lock(&shared_mutex);

            // probably yields garbage if not superseded below. Let it show.
            int last_result = status;

            if (WIFEXITED(status)) {
                last_result = WEXITSTATUS(status);
            } else if (WIFSIGNALED(status)) {
                last_result = 0x80 + WTERMSIG(status);
            }

            const auto monitor = waiting_procs.find(pid);
            if (monitor != waiting_procs.end()) {
                auto m = monitor->second;
                waiting_procs.erase(monitor);
                *m->res = last_result;
                pthread_cond_broadcast(m->cond);
            } else {
                finished_procs[pid] = last_result;
            }
            pthread_mutex_unlock(&shared_mutex);
        }
    }
    // should be unreachable
    return NULL;
}

// The shared lock must be passed into this function for thread-safety.
static int check_result(const int pid, pthread_mutex_t *lock) {
    const auto result = finished_procs.find(pid);
    if (result != finished_procs.end()) {
        const auto exit_code = result->second;
        finished_procs.erase(result);
        return exit_code;
    }
    return -1;
}

extern "C" {
/* Notify process monitor about spawning new process */
void scalanative_process_monitor_notify() { sem_post(active_procs); }

int scalanative_process_monitor_check_result(const int pid) {
    pthread_mutex_lock(&shared_mutex);
    const int res = check_result(pid, &shared_mutex);
    pthread_mutex_unlock(&shared_mutex);
    return res;
}

int scalanative_process_monitor_wait_for_pid(const int pid, timespec *ts,
                                             int *proc_res) {
    pthread_mutex_lock(&shared_mutex);
    const int result = check_result(pid, &shared_mutex);
    if (result != -1) {
        *proc_res = result;
        pthread_mutex_unlock(&shared_mutex);
        return 0;
    }
    const auto it = waiting_procs.find(pid);
    const std::shared_ptr<Monitor> monitor =
        (it == waiting_procs.end()) ? std::make_shared<Monitor>(proc_res)
                                    : it->second;
    if (it == waiting_procs.end()) {
        waiting_procs.insert(std::make_pair(pid, monitor));
    }
    const int res =
        ts ? pthread_cond_timedwait(monitor->cond, &shared_mutex, ts)
           : pthread_cond_wait(monitor->cond, &shared_mutex);
    pthread_mutex_unlock(&shared_mutex);
    return res;
}

void scalanative_process_monitor_init() {
    pthread_t thread;
    pthread_mutex_init(&shared_mutex, NULL);

    // MacOs might not allow for usage of anonymous semaphores (not implemented
    // on M1 chips) leading to deadlocks
    char semaphoreName[SEM_MAX_LENGTH];

#if defined(__FreeBSD__)
#define SEM_NAME_PREFIX "/" // FreeBSD semaphore names must start with '/'
#else
#define SEM_NAME_PREFIX ""
#endif // __FreeBSD__

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
}

#endif // Unix or Mac OS
