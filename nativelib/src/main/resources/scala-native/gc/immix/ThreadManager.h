#ifndef IMMIX_THREADMANAGER_H
#define IMMIX_THREADMANAGER_H

#include <pthread.h>
#include "datastructures/ThreadList.h"

typedef struct {
    ThreadList *threadList;
    pthread_mutex_t mutex;
} ThreadManager;

void ThreadManager_Init(ThreadManager *threadManager);

void ThreadManager_RegisterThread(ThreadManager *threadManager,
                                  void *stackBottom);

void ThreadManager_SuspendAllThreads(ThreadManager *threadManager);

void ThreadManager_ResumeAllThreads(ThreadManager *threadManager);

#endif // IMMIX_THREADMANAGER_H
