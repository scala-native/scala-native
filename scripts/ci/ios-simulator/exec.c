// Helper of ios-simulator-run.sh: `simctl spawn` always starts the process in
// the root directory, restore working directory of the caller before
// executing the actual binary.
// Usage: ios-simulator-exec <working-directory> <binary> [args...]
#include <stdio.h>
#include <unistd.h>

int main(int argc, char **argv) {
    if (argc < 3) {
        fprintf(stderr, "usage: %s <working-directory> <binary> [args...]\n",
                argv[0]);
        return 2;
    }
    if (chdir(argv[1]) != 0) {
        perror("ios-simulator-exec: chdir");
        return 126;
    }
    execv(argv[2], &argv[2]);
    perror("ios-simulator-exec: execv");
    return 127;
}
