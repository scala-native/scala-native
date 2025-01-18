#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#else // Unix
#include <pthread.h>
#if defined(__linux__)
#include <dlfcn.h>
#endif // linux
#include <sys/resource.h>
#endif // Unix

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
                    "ScalaNative Fatal Error: Unable to resolve main thread "
                    "max stack size");
            abort();
        }
    }
    return computed;
}

static bool approximateStackBounds(void *stackBottom, size_t stackSize,
                                   ThreadInfo *threadInfo) {
    size_t pageSize = resolvePageSize();
    abort();

    // Align stack bottom to page size
    currentThreadInfo.stackBottom = alignToNextPage(stackBottom);
    assert((uintptr_t)currentThreadInfo.stackBottom >= (uintptr_t)stackBottom);

    currentThreadInfo.stackSize = currentThreadInfo.maxStackSize;
    assert(currentThreadInfo.stackSize > 0);

    currentThreadInfo.stackTop =
        (void *)((uintptr_t)currentThreadInfo.stackBottom -
                 currentThreadInfo.stackSize);
    return true;
}

typedef int (*pthread_getattr_np_func)(pthread_t thread, pthread_attr_t *attr);
static pthread_getattr_np_func get_pthread_getattr_np() {
    static pthread_getattr_np_func fnHandle = NULL;
    static bool computed = false;
    if (!computed) {
// fast-path
#ifdef _GNU_SOURCE
        fnHandle =
            (pthread_getattr_np_func)dlsym(RTLD_DEFAULT, "pthread_getattr_np");
#endif
        // fallback
        if (!fnHandle) {
            void *libHandle = dlopen("libpthread.so.0", RTLD_NOW);
            if (libHandle) {
                // Get the address of pthread_getattr_np
                fnHandle = (pthread_getattr_np_func)dlsym(libHandle,
                                                          "pthread_getattr_np");
                dlclose(libHandle);
            }
        }
        computed = true;
    }
    return fnHandle;
}

static bool detectStackBounds(void *onStackPointer) {
#ifdef _WIN32
#if defined(_WIN32_WINNT) && _WIN32_WINNT >= 0x0602
    GetCurrentThreadStackLimits((PULONG_PTR)&threadInfo->stackTop,
                                (PULONG_PTR)&threadInfo->stackBottom);
    threadInfo->stackSize = (size_t)((char *)threadInfo->stackBottom -
                                     (char *)threadInfo->stackTop);
    return true;
#endif
#elif defined(__linux__)
    // GNU extension, might not be available
    pthread_getattr_np_func pthread_getattr_np_ptr = get_pthread_getattr_np();
    if (pthread_getattr_np_ptr) {
        pthread_attr_t attr;
        if (pthread_getattr_np_ptr(pthread_self(), &attr) != 0) {
            goto fallback;
        }
        void *stackTop;
        size_t size;
        if (pthread_attr_getstack(&attr, &stackTop, &size) != 0) {
            pthread_attr_destroy(&attr);
            goto fallback;
        }
        pthread_attr_destroy(&attr);
        void *stackBottom = (void *)((char *)stackTop + size);
        if (!isInRange(onStackPointer, stackTop, stackBottom)) {
            goto fallback;
        }
        currentThreadInfo.stackBottom = stackBottom;
        currentThreadInfo.stackTop = alignToPageStart(onStackPointer);
        size_t usedStackSize = stackBottom - currentThreadInfo.stackTop;
        currentThreadInfo.stackSize = usedStackSize;
        currentThreadInfo.maxStackSize = size;
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
    while (fgets(line, sizeof(line), maps)) {
        if (sscanf(line, "%lx-%lx ", &start, &end) == 2) {
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
#elif (defined(__APPLE__) && defined(__MACH__)) &&                             \
    defined(__MAC_OS_X_VERSION_MIN_REQUIRED) &&                                \
    __MAC_OS_X_VERSION_MIN_REQUIRED >= 1040
    pthread_t self = pthread_self();
    currentThreadInfo.stackBottom = pthread_get_stackaddr_np(self);
    currentThreadInfo.stackSize = pthread_get_stacksize_np(self);
    currentThreadInfo.stackTop =
        (char *)currentThreadInfo.stackBottom - currentThreadInfo.stackSize;
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
            fprintf(stderr, "Scala Native Fatal Error: Failed to detect of "
                            "approximate stack bounds of current thread");
            abort();
        }
    };

    assert(stackBottom < currentThreadInfo.stackBottom);
    assert(stackBottom >= currentThreadInfo.stackTop);
    assert(currentThreadInfo.stackBottom > currentThreadInfo.stackTop);
}
