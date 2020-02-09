#ifndef IMMIX_THREADMANAGER_H
#define IMMIX_THREADMANAGER_H

void ThreadManager_Init();

void ThreadManager_RegisterThread(void *stackBottom);

void ThreadManager_SuspendAllThreads();

void ThreadManager_ResumeAllThreads();

#endif // IMMIX_THREADMANAGER_H
