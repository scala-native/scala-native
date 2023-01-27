#ifndef NATIVE_THREAD_TLS_H
#define NATIVE_THREAD_TLS_H
#include <stddef.h>

#ifndef thread_local
#if __STDC_VERSION__ >= 201112 && !defined __STDC_NO_THREADS__
#define thread_local _Thread_local
#elif defined _WIN32 && (defined _MSC_VER || defined __ICL ||                  \
                         defined __DMC__ || defined __BORLANDC__)
#define thread_local __declspec(thread)
/* note that ICC (linux) and Clang are covered by __GNUC__ */
#elif defined __GNUC__ || defined __SUNPRO_C || defined __xlC__
#define thread_local __thread
#else
#error "Cannot define thread_local"
#endif
#endif

typedef void *JavaThread;
typedef void *NativeThread;

extern thread_local JavaThread currentThread;
extern thread_local NativeThread currentNativeThread;

void scalanative_assignCurrentThread(JavaThread thread,
                                     NativeThread nativeThread);
JavaThread scalanative_currentThread();
NativeThread scalanative_currentNativeThread();
#endif