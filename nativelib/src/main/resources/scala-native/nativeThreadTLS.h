#ifndef NATIVE_THREAD_TLS_H
#define NATIVE_THREAD_TLS_H

#include <stddef.h>
#include <stdbool.h>
#include <stdint.h>
#include "gc/shared/ThreadUtil.h"

typedef void *JavaThread;
typedef void *NativeThread;

typedef struct ThreadInfo {
    size_t stackSize;
    void *stackTop;            // highest stack address
    void *stackBottom;         // lowest stack address
    void *firstStackGuardPage; // register delayed StackOverflowError throw
#ifndef _WIN32
    void *secondStackGuardPage; // try throw immediately or abort
    bool checkPendingExceptions;
    bool pendingStackOverflowException;
#endif
    bool isMainThread;
} ThreadInfo;

extern SN_ThreadLocal ThreadInfo currentThreadInfo;

void scalanative_assignCurrentThread(JavaThread thread,
                                     NativeThread nativeThread);
JavaThread scalanative_currentThread();
NativeThread scalanative_currentNativeThread();
ThreadInfo *scalanative_currentThreadInfo();
void scalanative_setupCurrentThreadInfo(void *stackBottom, uint32_t stackSize,
                                        bool isMainThread);

void scalanative_checkThreadPendingExceptions();
size_t scalanative_mainThreadMaxStackSize();
bool scalanative_forceMainThreadStackGrowth();

#endif