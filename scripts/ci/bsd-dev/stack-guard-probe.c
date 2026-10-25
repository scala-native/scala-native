/* Exercise the actual runtime stack discovery and guards without a Scala build.
 * Link with nativeThreadTLS.c and stackOverflowGuards.c; see README.md.
 */
#include "nativeThreadTLS.h"
#include "stackOverflowGuards.h"
#include <setjmp.h>
#include <signal.h>
#include <sys/resource.h>
#include <unistd.h>

static _Thread_local jmp_buf escape;

size_t scalanative_page_size(void) { return (size_t)sysconf(_SC_PAGESIZE); }

void StackTrace_PrintStackTrace(void) {
    /* An unexpected native fault must fail instead of entering an unwinder. */
    _exit(90);
}

__attribute__((noreturn)) void scalanative_throwStackOverflowError(void) {
    longjmp(escape, 1);
}

__attribute__((noinline)) static int recurse(int n) {
    volatile char frame[512];
    frame[0] = (char)n;
    scalanative_StackOverflowGuards_check();
    if (n == 1000000)
        _exit(91); // The configured stack must overflow well before this.
    return frame[0] + recurse(n + 1);
}

static void *run(void *arg) {
    int bottom;
    bool isMain = arg != NULL;
    scalanative_setupCurrentThreadInfo(&bottom, isMain ? 0 : 1024 * 1024,
                                       isMain);
    scalanative_StackOverflowGuards_setup(isMain);
    for (int i = 0; i < 2; i++) {
        if (!setjmp(escape)) {
            recurse(0);
            _exit(92);
        }
        scalanative_StackOverflowGuards_reset();
    }
    printf("%s: recovered from two stack overflows\n",
           isMain ? "main" : "worker");
    scalanative_StackOverflowGuards_close();
    return NULL;
}

int main(void) {
    /* Bound both test duration and main-stack memory under QEMU TCG. */
    alarm(30);
    struct rlimit limit;
    if (getrlimit(RLIMIT_STACK, &limit) != 0)
        return 1;
    if (limit.rlim_cur > 8 * 1024 * 1024)
        limit.rlim_cur = 8 * 1024 * 1024;
    if (setrlimit(RLIMIT_STACK, &limit) != 0)
        return 2;
    run((void *)1);
    pthread_t worker;
    pthread_attr_t attr;
    if (pthread_attr_init(&attr) != 0)
        return 3;
    if (pthread_attr_setstacksize(&attr, 1024 * 1024) != 0)
        return 4;
    int status = pthread_create(&worker, &attr, run, NULL);
    pthread_attr_destroy(&attr);
    if (status != 0 || pthread_join(worker, NULL) != 0)
        return 5;
    return 0;
}
