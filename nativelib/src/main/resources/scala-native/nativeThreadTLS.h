#ifndef NATIVE_THREAD_TLS_H
#define NATIVE_THREAD_TLS_H

#include <stddef.h>
#include <stdbool.h>
#include <stdint.h>
#include "gc/shared/ThreadUtil.h"

typedef void *JavaThread;
typedef void *NativeThread;

#ifndef _WIN32
#define SCALANATIVE_THREAD_ALT_STACK
#endif

typedef struct ThreadInfo {
    // Currently available stack size, might be less the maxStackSize if the
    // memory was not yet commited
    size_t stackSize;
    // Equals to either stackSize passed from Scala on ThreadInfo.init or detect
    // system default soft limit for stack size for main thread
    size_t maxStackSize;
    void *stackTop;    // highest stack address
    void *stackBottom; // lowest stack address
    void *stackGuardPage;
    bool isMainThread;
#ifndef _WIN32
    bool pendingStackOverflowException;
    void *signalHandlerStack;
    size_t signalHandlerStackSize;
#endif
} ThreadInfo;

extern SN_ThreadLocal ThreadInfo currentThreadInfo;

void scalanative_assignCurrentThread(JavaThread thread,
                                     NativeThread nativeThread);
JavaThread scalanative_currentThread();
NativeThread scalanative_currentNativeThread();
ThreadInfo *scalanative_currentThreadInfo();
void scalanative_setupCurrentThreadInfo(void *stackBottom, int32_t stackSize,
                                        bool isMainThread);

size_t scalanative_mainThreadMaxStackSize();

#endif