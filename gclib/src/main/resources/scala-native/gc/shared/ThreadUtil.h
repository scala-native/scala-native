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

typedef void *(*routine_fn)(void *);
#ifdef _WIN32
typedef HANDLE thread_t;
typedef DWORD thread_id;
typedef HANDLE mutex_t;
typedef HANDLE semaphore_t;
typedef int pid_t;
#else
typedef pthread_t thread_t;
typedef pthread_t thread_id;
typedef pthread_mutex_t mutex_t;
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

#endif // COMMIX_THREAD_UTIL_H
