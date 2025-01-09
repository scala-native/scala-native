#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#else // Unix
#if defined(__APPLE__) && defined(__MACH__)
#include <pthread.h>
#endif
#include <sys/resource.h>
#endif

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
SN_ThreadLocal ThreadInfo currentThreadInfo = {.stackSize = 0,
                                               .stackTop = NULL,
                                               .stackBottom = NULL,
                                               .firstStackGuardPage = NULL,
                                               .secondStackGuardPage = NULL,
                                               .checkPendingExceptions = false,
                                               .pendingStackOverflowException =
                                                   false};

void scalanative_assignCurrentThread(JavaThread thread,
                                     NativeThread nativeThread) {
    currentThread = thread;
    currentNativeThread = nativeThread;
}

JavaThread scalanative_currentThread() { return currentThread; }
NativeThread scalanative_currentNativeThread() {
    scalanative_checkThreadPendingExceptions();
    return currentNativeThread;
}
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

    // Align stack bottom to page size
    currentThreadInfo.stackBottom =
        (void *)(((uintptr_t)stackBottom + pageSize - 1) & ~(pageSize - 1));
    assert((uintptr_t)currentThreadInfo.stackBottom >= (uintptr_t)stackBottom);

    if (currentThreadInfo.isMainThread) {
        // Ignore stack size from param, calculate fresh one
        currentThreadInfo.stackSize = scalanative_mainThreadMaxStackSize();
    } else {
        currentThreadInfo.stackSize = stackSize;
    }
    assert(currentThreadInfo.stackSize > 0);

    currentThreadInfo.stackTop =
        (void *)((uintptr_t)currentThreadInfo.stackBottom -
                 currentThreadInfo.stackSize);
    return true;
}

bool scalanative_forceMainThreadStackGrowth() {
    ThreadInfo *threadInfo = &currentThreadInfo;
    assert(threadInfo->isMainThread);
    // Main thread stack memory was not grown yet
    // Force it to at least default JVM thread size (1MB) of max
    // stack size
    // We would grow it again when when stack guard is reached
    size_t curStackSize = threadInfo->stackSize;
    size_t maxStackSize = scalanative_mainThreadMaxStackSize();
    if (curStackSize < maxStackSize) {
#define InitialMainThreadStackSize (1024 * 1024) // 1MB
        if (curStackSize < InitialMainThreadStackSize)
            threadInfo->stackSize = InitialMainThreadStackSize;
        else
            threadInfo->stackSize += InitialMainThreadStackSize;
        if (threadInfo->stackSize > maxStackSize)
            threadInfo->stackSize = maxStackSize;

        // Force growing of stack pointer and before updating thread info
        void *newStackBottom =
            (char *)(threadInfo->stackBottom) - threadInfo->stackSize;
#ifdef _WIN32
        VirtualAlloc(threadInfo->stackTop, threadInfo->stackSize, MEM_COMMIT,
                     PAGE_READWRITE);
#else
        volatile char *ptr = threadInfo->stackTop;
        while ((void *)ptr > newStackBottom) {
            *ptr = 0; // Write to the memory to force allocation
            ptr -= scalanative_page_size();
        }
#endif
        threadInfo->stackTop = newStackBottom;

        return true;
#undef InitialMainThreadStackSize
    }
    return false;
}

static bool detectStackBounds(void *onStackPointer, ThreadInfo *threadInfo) {
#ifdef _WIN32
    MEMORY_BASIC_INFORMATION mbi;
    VirtualQuery(onStackPointer, &mbi, sizeof(mbi));
    threadInfo->stackTop = (void *)mbi.AllocationBase;
    threadInfo->stackBottom =
        (void *)((char *)mbi.AllocationBase + mbi.RegionSize);
    threadInfo->stackSize = (size_t)((char *)threadInfo->stackBottom -
                                     (char *)threadInfo->stackTop);
    return true;
#elif defined(__linux__)
    FILE *maps = fopen("/proc/self/maps", "r");
    if (!maps) {
        return false;
    }

    char line[256];
    while (fgets(line, sizeof(line), maps)) {
        uintptr_t start, end;
        if (sscanf(line, "%lx-%lx ", &start, &end) == 2) {
            if ((void *)start <= onStackPointer &&
                onStackPointer < (void *)end) {
                threadInfo->stackTop = (void *)start;
                threadInfo->stackBottom = (void *)end;
                threadInfo->stackSize = end - start;
                if (threadInfo->isMainThread) {
                    // Main thread memory might not be fully available yet
                    // Force growing stack size
                    scalanative_forceMainThreadStackGrowth();
                }
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

void scalanative_setupCurrentThreadInfo(void *stackBottom, uint32_t stackSize,
                                        bool isMainThread) {
    // Assert stack grows downwards
    int dummy;
    assert((uintptr_t)&dummy < (uintptr_t)stackBottom);

    currentThreadInfo.isMainThread = isMainThread;
    if (!detectStackBounds(stackBottom, &currentThreadInfo)) {
        if (!approximateStackBounds(stackBottom, stackSize,
                                    &currentThreadInfo)) {
            fprintf(stderr, "Scala Native Fatal Error: Failed to detect of "
                            "approximate stack bounds of current thread");
            abort();
        }
    };

    assert(stackBottom < currentThreadInfo.stackBottom);
    assert(stackBottom > currentThreadInfo.stackTop);
    assert(currentThreadInfo.stackBottom > currentThreadInfo.stackTop);
}

void scalanative_checkThreadPendingExceptions() {
    ThreadInfo info = currentThreadInfo;
    if (info.checkPendingExceptions) {
        if (info.pendingStackOverflowException) {
            scalanative_handlePendingStackOverflowError();
        }
    }
}
