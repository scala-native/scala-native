#ifdef SCALANATIVE_GC_IMMIX
#ifndef GC_THREADS_H
#define GC_THREADS_H

#include <stdatomic.h>
#include "shared/ThreadUtil.h"

struct GCWeakRefsHandlerThread {
    thread_t handle;
    atomic_bool isActive;
#ifdef _WIN32
    HANDLE resumeEvent;
#else
    struct {
        pthread_mutex_t lock;
        pthread_cond_t cond;
    } resumeEvent;
#endif
};

struct GCWeakRefsHandlerThread *GCThread_WeakThreadsHandler_Start();
void GCThread_WeakThreadsHandler_Resume(struct GCWeakRefsHandlerThread *);

#endif
#endif