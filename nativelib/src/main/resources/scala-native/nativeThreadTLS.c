#include "nativeThreadTLS.h"

thread_local JavaThread currentThread = NULL;
thread_local NativeThread currentNativeThread = NULL;

void scalanative_assignCurrentThread(JavaThread thread,
                                     NativeThread nativeThread) {
    currentThread = thread;
    currentNativeThread = nativeThread;
}

JavaThread scalanative_currentThread() { return currentThread; }
NativeThread scalanative_currentNativeThread() { return currentNativeThread; }
