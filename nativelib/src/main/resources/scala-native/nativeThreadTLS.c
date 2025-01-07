#include "nativeThreadTLS.h"
#include "gc/shared/ThreadUtil.h"
#include "stackOverflowGuards.h"
#include <assert.h>

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

void scalanative_setupCurrentThreadInfo(void *stackBottom, uint stackSize,
                                        bool isMainThread) {
    size_t pageSize = resolvePageSize();
    // Assert stack grows downwards
    int dummy;
    assert((uintptr_t)&dummy < (uintptr_t)stackBottom);

    // Align stack bottom to page size
    currentThreadInfo.stackBottom =
        (void *)(((uintptr_t)stackBottom + pageSize - 1) & ~(pageSize - 1));
    assert((uintptr_t)currentThreadInfo.stackBottom >= (uintptr_t)stackBottom);

    currentThreadInfo.isMainThread = isMainThread;
    if (isMainThread) {
        // Ignore stack size from param, calculate fresh one
#ifdef _WIN32
        MEMORY_BASIC_INFORMATION mbi;
        VirtualQuery(&mbi, &mbi, sizeof(mbi));
        currentThreadInfo.stackSize = (size_t)mbi.RegionSize;
#else
        struct rlimit rl;
        if (getrlimit(RLIMIT_STACK, &rl) == 0) {
            currentThreadInfo.stackSize = (size_t)rl.rlim_cur;
        }
#endif
    } else {
        currentThreadInfo.stackSize = stackSize;
    }
    assert(currentThreadInfo.stackSize > 0);
    currentThreadInfo.stackTop =
        (void *)((uintptr_t)currentThreadInfo.stackBottom -
                 currentThreadInfo.stackSize);
}

void scalanative_checkThreadPendingExceptions() {
    ThreadInfo info = currentThreadInfo;
    if (info.checkPendingExceptions) {
        if (info.pendingStackOverflowException) {
            scalanative_handlePendingStackOverflowError();
        }
    }
}
