#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>

#ifndef _WIN32
#include <sys/resource.h>
#endif

// Layout prefix of ThreadInfo in nativelib/.../nativeThreadTLS.h
typedef struct ScalaNativeThreadInfoView {
    size_t stackSize;
    size_t maxStackSize;
    void *stackTop;
    void *stackBottom;
    void *stackGuardPage;
    bool isMainThread;
} ScalaNativeThreadInfoView;

extern ScalaNativeThreadInfoView *scalanative_currentThreadInfo(void);
extern void scalanative_setupCurrentThreadInfo(void *stackBottom,
                                               int32_t stackSize,
                                               bool isMainThread);

size_t scalanative_test_rlimit_stack(void) {
#ifndef _WIN32
    struct rlimit rl;
    if (getrlimit(RLIMIT_STACK, &rl) == 0) {
        return (size_t)rl.rlim_cur;
    }
#endif
    return 0;
}

size_t scalanative_test_simulate_main_thread_setup(void) {
    ScalaNativeThreadInfoView saved = *scalanative_currentThreadInfo();
    char on_stack;
    scalanative_setupCurrentThreadInfo(&on_stack, 0, true);
    size_t observed = scalanative_currentThreadInfo()->maxStackSize;
    *scalanative_currentThreadInfo() = saved;
    return observed;
}
