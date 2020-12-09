#include <memory>
#include <pthread.h>
#include <sys/wait.h>
#include <sys/time.h>
#include <unordered_map>

#define RETURN_ON_ERROR(f)                                                     \
    do {                                                                       \
        int res = f;                                                           \
        if (res != 0)                                                          \
            return res;                                                        \
    } while (0)

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

static void *wait_loop(void *arg) {
    while (1) {
        int status;
        const int pid = waitpid(-1, &status, 0);
        if (pid != -1) {
            pthread_mutex_lock(&shared_mutex);
            const int last_result =
                WIFSIGNALED(status) ? 0x80 + status : status;
            const auto monitor = waiting_procs.find(pid);
            if (monitor != waiting_procs.end()) {
                waiting_procs.erase(monitor);
                auto m = monitor->second;
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
        finished_procs.erase(result);
        return result->second;
    }
    return -1;
}

extern "C" {
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
    pthread_create(&thread, NULL, wait_loop, NULL);
    pthread_detach(thread);
}
}
