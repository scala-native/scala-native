#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#else // Unix
#include <pthread.h>
#include <sys/resource.h>
#endif // Unix

#include "string_constants.h"
#include "nativeThreadTLS.h"
#include "gc/shared/ThreadUtil.h"
#include "stackOverflowGuards.h"
#include <assert.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

SN_ThreadLocal JavaThread currentThread = NULL;
SN_ThreadLocal NativeThread currentNativeThread = NULL;
SN_ThreadLocal ThreadInfo currentThreadInfo = {0};

void scalanative_assignCurrentThread(JavaThread thread,
                                     NativeThread nativeThread) {
    currentThread = thread;
    currentNativeThread = nativeThread;
}

JavaThread scalanative_currentThread() { return currentThread; }
NativeThread scalanative_currentNativeThread() { return currentNativeThread; }
ThreadInfo *scalanative_currentThreadInfo() { return &currentThreadInfo; }

size_t scalanative_mainThreadMaxStackSize() {
    static size_t computed = -1;
    if (computed == -1) {
#ifdef _WIN32
        MEMORY_BASIC_INFORMATION mbi;
        VirtualQuery(&mbi, &mbi, sizeof(mbi));
        computed = (size_t)mbi.RegionSize;
#else
        struct rlimit rl;
        if (getrlimit(RLIMIT_STACK, &rl) == 0) {
            computed = (size_t)rl.rlim_cur;
        }
#endif
        if (computed <= 0) {
            fprintf(stderr,
                    "%s Unable to resolve main thread "
                    "max stack size",
                    snFatalErrorPrefix);
            abort();
        }
    }
    return computed;
}

static bool approximateStackBounds(void *stackBottom, size_t stackSize,
                                   ThreadInfo *threadInfo) {
    // Align stack bottom to page size
    threadInfo->stackBottom = alignToNextPage(stackBottom);
    assert((uintptr_t)threadInfo->stackBottom >= (uintptr_t)stackBottom);

    threadInfo->stackSize = threadInfo->maxStackSize;
    if (stackSize > 0 && threadInfo->stackSize > stackSize) {
        threadInfo->stackSize = stackSize;
    }
    assert(threadInfo->stackSize > 0);

    threadInfo->stackTop =
        (void *)((uintptr_t)threadInfo->stackBottom - threadInfo->stackSize);
    return true;
}

#if defined(__linux__)
/* GNU extension; <pthread.h> hides it without _GNU_SOURCE. Called directly
 * rather than via dlopen/dlsym so it resolves at link time in fully-static
 * binaries, where those fail (musl's static dlopen is a no-op stub). */
extern int pthread_getattr_np(pthread_t thread, pthread_attr_t *attr);
#endif

static bool detectStackBounds(void *onStackPointer) {
#ifdef _WIN32
#if defined(_WIN32_WINNT) && _WIN32_WINNT >= 0x0602
    GetCurrentThreadStackLimits((PULONG_PTR)&currentThreadInfo.stackTop,
                                (PULONG_PTR)&currentThreadInfo.stackBottom);
    currentThreadInfo.stackSize =
        (size_t)((char *)currentThreadInfo.stackBottom -
                 (char *)currentThreadInfo.stackTop);
    return true;
#endif
#elif defined(__linux__)
    {
        pthread_attr_t attr;
        if (pthread_getattr_np(pthread_self(), &attr) != 0) {
            goto fallback;
        }
        void *stackTop;
        size_t size;
        if (pthread_attr_getstack(&attr, &stackTop, &size) != 0) {
            pthread_attr_destroy(&attr);
            goto fallback;
        }
        size_t guardSize = 0;
        pthread_attr_getguardsize(&attr, &guardSize);
        pthread_attr_destroy(&attr);
        void *stackBottom = (void *)((char *)stackTop + size);
        if (!isInRange(onStackPointer, stackTop, stackBottom)) {
            goto fallback;
        }
        currentThreadInfo.stackBottom = stackBottom;
        currentThreadInfo.stackTop = alignToPageStart(onStackPointer);
        size_t usedStackSize = stackBottom - currentThreadInfo.stackTop;
        currentThreadInfo.stackSize = usedStackSize;
        // For the main thread, Linux's pthread_attr_getstack reports the
        // currently-mapped stack region rather than RLIMIT_STACK; overwriting
        // would clobber the RLIMIT_STACK-derived value already set via
        // scalanative_mainThreadMaxStackSize.
        if (!currentThreadInfo.isMainThread) {
            currentThreadInfo.maxStackSize = size - guardSize;
        }
        if (currentThreadInfo.stackSize > currentThreadInfo.maxStackSize) {
            currentThreadInfo.stackSize = currentThreadInfo.maxStackSize;
        }
        currentThreadInfo.stackTop = alignToPageStart(onStackPointer);
        return true;
    }
fallback:;
    FILE *maps = fopen("/proc/self/maps", "r");
    if (!maps) {
        perror("ScalaNative TheadInfo init failed to open /proc/self/maps");
        return false;
    }
    uintptr_t start, end;
    char line[256];
#ifdef __ILP32__
    char *format = "%x-%x ";
#else
    char *format = "%lx-%lx ";
#endif

    while (fgets(line, sizeof(line), maps)) {
        if (sscanf(line, format, &start, &end) == 2) {
            if (isInRange(onStackPointer, (void *)start, (void *)end)) {
                size_t size = end - start;
                currentThreadInfo.stackBottom = (void *)end;
                currentThreadInfo.stackTop = alignToPageStart(onStackPointer);
                size_t usedStackSize =
                    currentThreadInfo.stackBottom - currentThreadInfo.stackTop;
                size_t maxStackSize = currentThreadInfo.maxStackSize;
                currentThreadInfo.stackSize = (usedStackSize > maxStackSize)
                                                  ? maxStackSize
                                                  : usedStackSize;

                fclose(maps);
                return true;
            }
        }
    }
    fclose(maps);
#elif defined(__APPLE__) && defined(__MACH__)
    // No way to get thread-specific guard size
    // Use the default one
    size_t guardSize = 0;
    pthread_attr_t attrs;
    pthread_attr_init(&attrs);
    pthread_attr_getguardsize(&attrs, &guardSize);
    pthread_attr_destroy(&attrs);

    pthread_t self = pthread_self();
    void *stackAddr = pthread_get_stackaddr_np(self);
    size_t stackSize = pthread_get_stacksize_np(self);
    currentThreadInfo.stackSize = stackSize - guardSize;

    // pthread_get_stackaddr_np is not well documented, there are some mentions
    // that in some versions of MacOS it was pointing to stackTop instead of
    // stackBottom
    if (stackAddr < onStackPointer) {
        currentThreadInfo.stackBottom =
            (char *)stackAddr + stackSize + guardSize;
        currentThreadInfo.stackTop = (char *)stackAddr + guardSize;
    } else {
        currentThreadInfo.stackBottom = stackAddr;
        currentThreadInfo.stackTop = (char *)stackAddr - stackSize + guardSize;
    }
    assert(isInRange(onStackPointer, currentThreadInfo.stackTop,
                     currentThreadInfo.stackBottom));
    return true;
#endif
    return false;
}

void scalanative_setupCurrentThreadInfo(void *stackBottom, int32_t stackSize,
                                        bool isMainThread) {
    // Assert stack grows downwards
    int dummy;
    assert((uintptr_t)&dummy < (uintptr_t)stackBottom);

    currentThreadInfo.isMainThread = isMainThread;
    currentThreadInfo.maxStackSize =
        (isMainThread ? scalanative_mainThreadMaxStackSize() : stackSize) -
        4 * resolvePageSize(); // reserve for stack guard that might be
                               // introdueced by system. Also provide an
                               // error tolarance for approximation
    if (!detectStackBounds(stackBottom)) {
        if (!approximateStackBounds(stackBottom, stackSize,
                                    &currentThreadInfo)) {
            fprintf(stderr,
                    "%s Failed to detect of "
                    "approximate stack bounds of current thread",
                    snFatalErrorPrefix);
            abort();
        }
    };

    assert(stackBottom < currentThreadInfo.stackBottom);
    assert(stackBottom >= currentThreadInfo.stackTop);
    assert(currentThreadInfo.stackBottom > currentThreadInfo.stackTop);
}
