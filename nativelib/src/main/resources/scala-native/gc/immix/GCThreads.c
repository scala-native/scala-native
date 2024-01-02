#include "immix/State.h"
#include "shared/ScalaNativeGC.h"
#ifdef SCALANATIVE_GC_IMMIX
#include "GCThreads.h"
#include "MutatorThread.h"
#include "WeakRefStack.h"
#include <stdlib.h>
#include <stdatomic.h>
#include <stdbool.h>

static void
GCThread_WeakThreadsHandler_init(struct GCWeakRefsHandlerThread *self) {
    MutatorThread_init((word_t **)&self);
    MutatorThread_switchState(currentMutatorThread,
                              MutatorThreadState_Unmanaged);
#ifdef _WIN32
    self->resumeEvent = CreateEvent(NULL, true, false, NULL);
    if (self->resumeEvent == NULL) {
        fprintf(stderr,
                "Failed to setup GC weak refs threads event: errno=%lu\n",
                GetLastError());
        exit(1);
    }
#else
    if (pthread_mutex_init(&self->resumeEvent.lock, NULL) != 0 ||
        pthread_cond_init(&self->resumeEvent.cond, NULL) != 0) {
        perror("Failed to setup GC weak refs thread");
        exit(1);
    }
#endif
}

static void *GCThread_WeakThreadsHandlerLoop(void *arg) {
    struct GCWeakRefsHandlerThread *self =
        (struct GCWeakRefsHandlerThread *)arg;
    GCThread_WeakThreadsHandler_init(self);
    // main loop
    while (true) {
        // Wait for dispatch
#ifdef _WIN32
        while (!atomic_load(&self->isActive)) {
            WaitForSingleObject(self->resumeEvent, INFINITE);
            ResetEvent(self->resumeEvent);
        }
#else
        pthread_mutex_lock(&self->resumeEvent.lock);
        while (!atomic_load(&self->isActive)) {
            pthread_cond_wait(&self->resumeEvent.cond, &self->resumeEvent.lock);
        }
        pthread_mutex_unlock(&self->resumeEvent.lock);
#endif
        MutatorThread_switchState(currentMutatorThread,
                                  MutatorThreadState_Managed);
        WeakRefStack_CallHandlers();
        MutatorThread_switchState(currentMutatorThread,
                                  MutatorThreadState_Unmanaged);
        atomic_store(&self->isActive, false);
    }
    free(self);
}

struct GCWeakRefsHandlerThread *GCThread_WeakThreadsHandler_Start() {
    struct GCWeakRefsHandlerThread *thread =
        (struct GCWeakRefsHandlerThread *)malloc(
            sizeof(struct GCWeakRefsHandlerThread));
    thread_create(&thread->handle, GCThread_WeakThreadsHandlerLoop,
                  (void *)thread);
    return thread;
}

void GCThread_WeakThreadsHandler_Resume(
    struct GCWeakRefsHandlerThread *thread) {
    bool expected = false;
    if (atomic_compare_exchange_weak(&thread->isActive, &expected, true)) {
#ifdef _WIN32
        SetEvent(thread->resumeEvent);
#else
        pthread_mutex_lock(&thread->resumeEvent.lock);
        pthread_cond_signal(&thread->resumeEvent.cond);
        pthread_mutex_unlock(&thread->resumeEvent.lock);
#endif
    }
}

#endif