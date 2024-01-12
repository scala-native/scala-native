#ifndef NATIVE_THREAD_TLS_H
#define NATIVE_THREAD_TLS_H

#include "gc/shared/ThreadUtil.h"

typedef void *JavaThread;
typedef void *NativeThread;

void scalanative_assignCurrentThread(JavaThread thread,
                                     NativeThread nativeThread);
JavaThread scalanative_currentThread();
NativeThread scalanative_currentNativeThread();
#endif