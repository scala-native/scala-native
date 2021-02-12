#include <sys/wait.h>

int scala_native_wait_WCONTINUED = WCONTINUED;

int scala_native_wait_WUNTRACED = WUNTRACED;

int scala_native_wait_WNOHANG = WNOHANG;

int scala_native_wait_WIFEXITED(int stats_val) { return WIFEXITED(stats_val); }

int scala_native_wait_WEXITSTATUS(int stats_val) {
    return WEXITSTATUS(stats_val);
}

int scala_native_wait_WIFSIGNALED(int stats_val) {
    return WIFSIGNALED(stats_val);
}

int scala_native_wait_WTERMSIG(int stats_val) { return WTERMSIG(stats_val); }

int scala_native_wait_WIFSTOPPED(int stats_val) {
    return WIFSTOPPED(stats_val);
}

int scala_native_wait_WSTOPSIG(int stats_val) { return WSTOPSIG(stats_val); }

int scala_native_wait_WIFCONTINUED(int stats_val) {
    return WIFCONTINUED(stats_val);
}