#include "shared/ThreadUtil.h"
#include <stdio.h>
#include <limits.h>

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

INLINE thread_id thread_getid() {
#ifdef _WIN32
    return GetCurrentThreadId();
#else
    return pthread_self();
#endif
}
INLINE bool thread_equals(thread_id l, thread_id r) {
#ifdef _WIN32
    return l == r;
#else
    return pthread_equal(l, r);
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
    HANDLE sem = CreateSemaphore(NULL, initValue, LONG_MAX, NULL);
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

bool rwlock_init(rwlock_t *ref) {
#ifdef _WIN32
    InitializeSRWLock(ref);
    return true;
#else
    return pthread_rwlock_init(ref, NULL) == 0;
#endif
}

bool rwlock_lockRead(rwlock_t *ref) {
#ifdef _WIN32
    AcquireSRWLockShared(ref);
    return true;
#else
    return pthread_rwlock_rdlock(ref) == 0;
#endif
}
bool rwlock_lockWrite(rwlock_t *ref) {
#ifdef _WIN32
    AcquireSRWLockExclusive(ref);
    return true;
#else
    return pthread_rwlock_wrlock(ref) == 0;
#endif
}

bool rwlock_unlockWrite(rwlock_t *ref) {
#ifdef _WIN32
    ReleaseSRWLockExclusive(ref);
    return true;
#else
    return pthread_rwlock_unlock(ref) == 0;
#endif
}

bool rwlock_unlockRead(rwlock_t *ref) {
#ifdef _WIN32
    ReleaseSRWLockShared(ref);
    return true;
#else
    return pthread_rwlock_unlock(ref) == 0;
#endif
}
