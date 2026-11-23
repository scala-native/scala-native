/* fork/wait/_exit for the fork scenario.
 *
 * posixlib's wait.c includes <sys/wait.h> when its symbols are reachable.
 * That header does not exist on Windows, so this file stays local.
 */

#ifndef _WIN32
#include <sys/wait.h>
#include <unistd.h>

int fork_support_fork(void) { return (int)fork(); }

int fork_support_wait_child(int pid, int *waited, int *status) {
    int st = 0;
    int w = (int)waitpid((pid_t)pid, &st, 0);
    *waited = w;
    *status = st;
    if (w != pid || !WIFEXITED(st) || WEXITSTATUS(st) != 0)
        return -1;
    return 0;
}

void fork_support_exit(int status) { _exit(status); }
#else
#include <windows.h>

int fork_support_fork(void) { return -1; }

int fork_support_wait_child(int pid, int *waited, int *status) {
    (void)pid;
    *waited = -1;
    *status = 0;
    return -1;
}

void fork_support_exit(int status) { ExitProcess((UINT)status); }
#endif
