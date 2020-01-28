#ifndef IMMIX_THREADMANAGER_H
#define IMMIX_THREADMANAGER_H

void ThreadManager_Init();

void ThreadManager_Register_Thread(void *stackBottom);

void suspend_all_threads();

void resume_all_threads();

#endif // IMMIX_THREADMANAGER_H
