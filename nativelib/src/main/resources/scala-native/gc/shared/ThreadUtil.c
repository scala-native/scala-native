#include "ThreadUtil.h"
#include <stdio.h>

INLINE
bool thread_create(thread_t *ref, routine_fn routine, void *data) {
#ifdef _WIN32
    *ref =
        CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)routine, data, 0, NULL);
    return *ref != NULL;
#else
    return pthread_create(ref, NULL, routine, data) == 0;
#endif
}

INLINE
void thread_yield() {
#ifdef _WIN32
    SwitchToThread();
#else
    sched_yield();
#endif
}

INLINE
pid_t process_getid() {
#ifdef _WIN32
    return (pid_t)GetCurrentProcessId();
#else
    return (pid_t)getpid();
#endif
}

INLINE
bool mutex_init(mutex_t *ref) {
#ifdef _WIN32
    *ref = CreateMutex(NULL, FALSE, NULL);
    return *ref != NULL;
#else
    pthread_mutexattr_t attr;
    pthread_mutexattr_init(&attr);
    pthread_mutexattr_settype(&attr, PTHREAD_MUTEX_RECURSIVE);
    return pthread_mutex_init(ref, &attr) == 0;
#endif
}

INLINE
bool mutex_lock(mutex_t *ref) {
#ifdef _WIN32
    return WaitForSingleObject(*ref, INFINITE) == WAIT_OBJECT_0;
#else
    return pthread_mutex_lock(ref) == 0;
#endif
}

INLINE
bool mutex_tryLock(mutex_t *ref) {
#ifdef _WIN32
    return WaitForSingleObject(*ref, 0) == WAIT_OBJECT_0;
#else
    return pthread_mutex_trylock(ref) == 0;
#endif
}

INLINE
bool mutex_unlock(mutex_t *ref) {
#ifdef _WIN32
    return ReleaseMutex(*ref);
#else
    return pthread_mutex_unlock(ref) == 0;
#endif
}

INLINE
bool semaphore_open(semaphore_t *ref, char *name, unsigned int initValue) {
#ifdef _WIN32
    HANDLE sem = CreateSemaphore(NULL, initValue, 128, NULL);
    *ref = sem;
    return sem != NULL;
#else
    sem_t *sem = sem_open(name, O_CREAT | O_EXCL, 0644, initValue);
    *ref = sem;
    return sem != SEM_FAILED;
#endif
}

INLINE
bool semaphore_wait(semaphore_t ref) {
#ifdef _WIN32
    return WaitForSingleObject(ref, INFINITE) == WAIT_OBJECT_0;
#else
    return sem_wait(ref) == 0;
#endif
}

INLINE
bool semaphore_unlock(semaphore_t ref) {
#ifdef _WIN32
    return ReleaseSemaphore(ref, 1, NULL);
#else
    return sem_post(ref) == 0;
#endif
}
