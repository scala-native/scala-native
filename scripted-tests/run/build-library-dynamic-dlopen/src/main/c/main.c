#include <dlfcn.h>
#include <stdio.h>

int main(int argc, char **argv) {
    void *lib = dlopen(argv[1], RTLD_NOW);
    if (!lib) {
        fprintf(stderr, "%s\n", dlerror());
        return 1;
    }
    int (*answer)(void) = (int (*)(void))dlsym(lib, "answer");
    return answer == NULL || answer() != 42;
}
