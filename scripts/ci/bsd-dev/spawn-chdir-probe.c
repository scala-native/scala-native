/* Link with javalib_spawn.c to check the selected spawn/chdir ABI. */
#include <assert.h>
#include <limits.h>
#include <spawn.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/wait.h>
#include <unistd.h>

extern bool hasFileActionsAddChdir(void);
extern void fileActionsAddChdir(posix_spawn_file_actions_t *, char *);
extern char **environ;

int main(void) {
    alarm(10);
    if (!hasFileActionsAddChdir()) {
        puts("spawn chdir unavailable; fork fallback required");
        return 0;
    }

    char parentCwd[PATH_MAX], afterCwd[PATH_MAX];
    assert(getcwd(parentCwd, sizeof(parentCwd)) != NULL);
    char directory[] = "/tmp/sn-spawn-chdir-XXXXXX";
    assert(mkdtemp(directory) != NULL);
    char childCwd[PATH_MAX];
    assert(realpath(directory, childCwd) != NULL);
    int output[2];
    assert(pipe(output) == 0);
    posix_spawn_file_actions_t actions;
    assert(posix_spawn_file_actions_init(&actions) == 0);
    fileActionsAddChdir(&actions, directory);
    assert(posix_spawn_file_actions_adddup2(&actions, output[1],
                                            STDOUT_FILENO) == 0);
    assert(posix_spawn_file_actions_addclose(&actions, output[0]) == 0);
    assert(posix_spawn_file_actions_addclose(&actions, output[1]) == 0);
    char *argv[] = {"pwd", NULL};
    pid_t child;
    assert(posix_spawn(&child, "/bin/pwd", &actions, NULL, argv, environ) == 0);
    assert(posix_spawn_file_actions_destroy(&actions) == 0);
    close(output[1]);

    char actual[PATH_MAX];
    size_t used = 0;
    ssize_t bytes;
    while ((bytes = read(output[0], actual + used, sizeof(actual) - used - 1)) >
           0)
        used += (size_t)bytes;
    assert(bytes == 0 && used > 0 && actual[used - 1] == '\n');
    actual[used - 1] = '\0';
    close(output[0]);
    int status;
    assert(waitpid(child, &status, 0) == child);
    assert(WIFEXITED(status) && WEXITSTATUS(status) == 0);
    assert(strcmp(actual, childCwd) == 0);
    assert(getcwd(afterCwd, sizeof(afterCwd)) != NULL);
    assert(strcmp(parentCwd, afterCwd) == 0);
    assert(rmdir(directory) == 0);
    puts("spawn chdir passed; parent working directory unchanged");
    return 0;
}
