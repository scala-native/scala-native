#include <chrono>
#include <condition_variable>
#include <ctime>
#include <memory>
#include <mutex>
#include <sys/time.h>
#include <sys/wait.h>
#include <thread>
#include <unordered_map>

#define RETURN_ON_ERROR(f)                                                     \
    do {                                                                       \
        int res = f;                                                           \
        if (res != 0)                                                          \
            return res;                                                        \
    } while (0)

struct Monitor {
  public:
    std::unique_ptr<std::condition_variable> cond;
    int *res;
    Monitor(int *res) : res(res), cond(new std::condition_variable()) {}
    ~Monitor() {}
};
static std::mutex shared_mutex;
static std::unordered_map<int, std::shared_ptr<Monitor>> waiting_procs;
static std::unordered_map<int, int> finished_procs;

static void wait_loop() {
    while (1) {
        int status;
        const int pid = waitpid(-1, &status, 0);
        if (pid != -1) {
            std::unique_lock<std::mutex> lck(shared_mutex);
            const int last_result =
                WIFSIGNALED(status) ? 0x80 + status : status;
            const auto monitor = waiting_procs.find(pid);
            if (monitor != waiting_procs.end()) {
                waiting_procs.erase(monitor);
                auto m = monitor->second;
                *m->res = last_result;
                m->cond->notify_all();
            } else {
                finished_procs[pid] = last_result;
            }
        }
    }
    // should be unreachable
    return;
}

static std::chrono::time_point<std::chrono::system_clock,
                               std::chrono::nanoseconds>
timespec_to_time_point(timespec ts, std::mutex *lock) {
    auto d =
        std::chrono::seconds{ts.tv_sec} + std::chrono::nanoseconds{ts.tv_nsec};

    return std::chrono::time_point<std::chrono::system_clock,
                                   std::chrono::nanoseconds>{
        std::chrono::duration_cast<std::chrono::system_clock::duration>(
            std::chrono::duration_cast<std::chrono::nanoseconds>(d))};
}

// The shared lock must be passed into this function for thread-safety.
static int check_result(const int pid, std::mutex *lock) {
    const auto result = finished_procs.find(pid);
    if (result != finished_procs.end()) {
        finished_procs.erase(result);
        return result->second;
    }
    return -1;
}

extern "C" {
int scalanative_process_monitor_check_result(const int pid) {
    std::unique_lock<std::mutex> lck(shared_mutex);
    const int res = check_result(pid, &shared_mutex);
    return res;
}

int scalanative_process_monitor_wait_for_pid(const int pid, timespec *ts,
                                             int *proc_res) {
    std::unique_lock<std::mutex> lck(shared_mutex);
    const int result = check_result(pid, &shared_mutex);
    if (result != -1) {
        *proc_res = result;
        return 0;
    }
    const auto it = waiting_procs.find(pid);
    const std::shared_ptr<Monitor> monitor =
        (it == waiting_procs.end()) ? std::make_shared<Monitor>(proc_res)
                                    : it->second;
    if (it == waiting_procs.end()) {
        waiting_procs.insert(std::make_pair(pid, monitor));
    }
    if (ts) {
        auto res = monitor->cond->wait_until(
            lck, timespec_to_time_point(*ts, &shared_mutex));
        return (int)res;
    } else {
        monitor->cond->wait(lck);
        return 0;
    }
}

void scalanative_process_monitor_init() {
    std::thread thread(wait_loop);
    thread.detach();
}

int scalanative_no_timeout = (int)std::cv_status::no_timeout;
int scalanative_timeout = (int)std::cv_status::timeout;
}
