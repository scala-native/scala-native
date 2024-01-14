#ifndef COMMIX_THREAD_UTIL_H
#define COMMIX_THREAD_UTIL_H

#include "shared/GCTypes.h"
#include <stdbool.h>

#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#else
#include <pthread.h>
#include <semaphore.h>
#include <sched.h>
#include <unistd.h>
#include <fcntl.h>
#endif

#ifndef SCALANATIVE_MULTITHREADING_ENABLED
#define SN_ThreadLocal
#else
#if __STDC_VERSION__ >= 201112L
// TODO Use tls_model hints when building application, but not when creating
// library #define TLS_MODEL_ATTR __attribute__((tls_model("local-exec")))
#define SN_ThreadLocal _Thread_local
#elif defined(_MSC_VER)
#define SN_ThreadLocal __declspec(thread)
#elif defined(__GNUC__) || defined(__clang__)
#define SN_ThreadLocal __thread
#else
#error Unable to create thread local storage
#endif
#endif // SCALANATIVE_MULTITHREADING_ENABLED

typedef void *(*routine_fn)(void *);
#ifdef _WIN32
typedef HANDLE thread_t;
typedef DWORD thread_id;
typedef HANDLE mutex_t;
typedef HANDLE semaphore_t;
typedef SRWLOCK rwlock_t;
typedef int pid_t;
#else
typedef pthread_t thread_t;
typedef pthread_t thread_id;
typedef pthread_mutex_t mutex_t;
typedef pthread_rwlock_t rwlock_t;
typedef sem_t *semaphore_t;
#endif

bool thread_create(thread_t *ref, routine_fn routine, void *data);
thread_id thread_getid();
bool thread_equals(thread_id l, thread_id r);
void thread_yield();

pid_t process_getid();

bool mutex_init(mutex_t *ref);
bool mutex_lock(mutex_t *ref);
bool mutex_tryLock(mutex_t *ref);
bool mutex_unlock(mutex_t *ref);

bool semaphore_open(semaphore_t *ref, char *name, unsigned int initValue);
bool semaphore_wait(semaphore_t ref);
bool semaphore_unlock(semaphore_t ref);

bool rwlock_init(rwlock_t *ref);
bool rwlock_lockRead(rwlock_t *ref);
bool rwlock_lockWrite(rwlock_t *ref);
bool rwlock_unlockRead(rwlock_t *ref);
bool rwlock_unlockWrite(rwlock_t *ref);

#endif // COMMIX_THREAD_UTIL_H
