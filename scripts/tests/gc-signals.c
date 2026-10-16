/* Standalone POSIX regression tests; run with scripts/tests/gc-signals.sh. */
#include <assert.h>
#include <errno.h>
#include <signal.h>
#include <stdatomic.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/resource.h>
#include <sys/wait.h>
#include <unistd.h>
#include <dlfcn.h>
#include "SignalDiagnostics.h"
#include "shared/YieldPointTrap.h"
#include "shared/ThreadUtil.h"
#include "shared/Log.h"
#ifdef SCALANATIVE_GC_IMMIX
#include "immix/Synchronizer.c"
#else
#include "commix/Synchronizer.c"
#endif

/* Deliberately widen the initialization window. A lock attempted before
 * publication of the initialized mutex must fail this test deterministically.
 */
static atomic_bool initialized;
bool mutex_init(mutex_t *lock) {
    usleep(50000);
    assert(pthread_mutex_init(lock, NULL) == 0);
    atomic_store(&initialized, true);
    return true;
}
bool mutex_lock(mutex_t *lock) {
    assert(atomic_load(&initialized));
    return pthread_mutex_lock(lock) == 0;
}
bool mutex_unlock(mutex_t *lock) { return pthread_mutex_unlock(lock) == 0; }
void thread_yield(void) { sched_yield(); }
GC_LogLevel GC_Log_GetLevel(void) { return GC_LOG_LEVEL_NONE; }
void GC_Log_Write(GC_LogLevel level, const char *prefix, const char *format,
                  ...) {
    abort();
}
#ifdef __APPLE__
void YieldPointTrap_resetTaskMachBadAccessPorts(void) {}
#endif

static void *allocateTraps(void *unused) {
    for (int i = 0; i < 100; i++) {
        safepoint_t trap = YieldPointTrap_init();
        assert(YieldPointTrap_contains(trap));
        YieldPointTrap_arm(trap);
        /* Registry lookup must not read the protected mapping itself. */
        assert(YieldPointTrap_contains(trap));
        YieldPointTrap_free(trap);
        assert(YieldPointTrap_contains(trap));
    }
    return NULL;
}

static void fatalHandler(int sig, siginfo_t *info, void *context) {
    scalanative_signal_fatal(sig, info, context, NULL, NULL, false);
}

static void interceptFault(int sig) {
    struct sigaction action = {0};
    action.sa_sigaction = fatalHandler;
    action.sa_flags = SA_SIGINFO;
    sigemptyset(&action.sa_mask);
    sigaction(sig, &action, NULL);
    raise(sig);
}

/* 0: hardware fault, 1: raise, 2: LLVM-style interception, 3: libclang. */
static void checkFatal(int kind) {
    int expectedSignal = SIGSEGV;
#ifdef __APPLE__
    if (kind >= 2)
        expectedSignal = SIGBUS;
#endif
    int output[2];
    assert(pipe(output) == 0);
    pid_t child = fork();
    assert(child >= 0);
    if (child == 0) {
        alarm(5);
        struct rlimit noCore = {0, 0};
        setrlimit(RLIMIT_CORE, &noCore);
        close(output[0]);
        assert(dup2(output[1], STDERR_FILENO) >= 0);
        close(output[1]);
        struct sigaction action = {0};
        action.sa_sigaction = fatalHandler;
        action.sa_flags = SA_SIGINFO;
        sigemptyset(&action.sa_mask);
        assert(sigaction(expectedSignal, &action, NULL) == 0);
        if (kind == 2) {
            action.sa_handler = interceptFault;
            action.sa_flags = 0;
            assert(sigaction(expectedSignal, &action, NULL) == 0);
        } else if (kind == 3) {
            unsetenv("LIBCLANG_DISABLE_CRASH_RECOVERY");
            void *lib = dlopen(getenv("GC_SIGNAL_TEST_LIBCLANG"), RTLD_NOW);
            assert(lib != NULL);
            void *(*createIndex)(int, int) = dlsym(lib, "clang_createIndex");
            assert(createIndex != NULL && createIndex(0, 0) != NULL);
        }
        if (kind == 1)
            raise(SIGSEGV);
        else if (kind >= 2) {
            safepoint_t trap = YieldPointTrap_init();
            YieldPointTrap_arm(trap);
            (void)*(void *volatile *)trap;
        } else
            *(volatile int *)(uintptr_t)1 = 0;
        _exit(99);
    }
    close(output[1]);
    char buffer[2048];
    size_t used = 0;
    ssize_t n;
    while ((n = read(output[0], buffer + used, sizeof(buffer) - 1 - used)) > 0)
        used += n;
    buffer[used] = 0;
    fputs(buffer, stdout);
    close(output[0]);
    int status;
    assert(waitpid(child, &status, 0) == child);
    assert(WIFSIGNALED(status) && WTERMSIG(status) == expectedSignal);
    assert(strstr(buffer, "ScalaNative: fatal signal="));
    assert(strstr(buffer, " pc=0x") && !strstr(buffer, " pc=0x0 "));
    assert(strstr(buffer, " sp=0x") && !strstr(buffer, " sp=0x0 "));
    if (kind != 0) {
#ifdef __linux__
        assert(strstr(buffer, "sender_pid="));
        assert(!strstr(buffer, " address="));
        assert(strstr(buffer, "code=-6"));
#endif
    } else {
        assert(strstr(buffer, "address=0x1 "));
        assert(!strstr(buffer, "sender_pid="));
#ifdef __linux__
        assert(strstr(buffer, "code=1 "));
#endif
    }
}

int main(void) {
    alarm(30);
    pthread_t threads[16];
    for (int i = 0; i < 16; i++)
        assert(pthread_create(&threads[i], NULL, allocateTraps, NULL) == 0);
    for (int i = 0; i < 16; i++)
        assert(pthread_join(threads[i], NULL) == 0);
    assert(!YieldPointTrap_contains(NULL));
    assert(!YieldPointTrap_contains(&threads));
    MutatorThread mutator = {0};
    siginfo_t fault = {0};
    fault.si_code = SEGV_ACCERR;
    fault.si_addr = YieldPointTrap_init();
    atomic_store(&Synchronizer_stopThreads, true);
    assert(isContinuationStaleTrapFault(SIGSEGV, &fault, &mutator));
    assert(!isContinuationStaleTrapFault(SIGSEGV, &fault, NULL));
    fault.si_code = SEGV_MAPERR;
    assert(!isContinuationStaleTrapFault(SIGSEGV, &fault, &mutator));
    fault.si_code = SI_USER;
    assert(!isContinuationStaleTrapFault(SIGSEGV, &fault, &mutator));
    fault.si_code = SEGV_ACCERR;
    fault.si_addr = &threads;
    assert(!isContinuationStaleTrapFault(SIGSEGV, &fault, &mutator));
    fault.si_addr = YieldPointTrap_init();
    atomic_store(&Synchronizer_stopThreads, false);
    assert(!isContinuationStaleTrapFault(SIGSEGV, &fault, &mutator));
    checkFatal(0);
    checkFatal(1);
    checkFatal(2);
    if (getenv("GC_SIGNAL_TEST_LIBCLANG"))
        checkFatal(3);
    puts("GC signal regression tests passed");
}
