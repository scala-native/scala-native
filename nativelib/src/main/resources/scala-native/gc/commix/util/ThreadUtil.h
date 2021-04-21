#ifndef COMMIX_THREAD_UTIL_H
#define COMMIX_THREAD_UTIL_H

#include "GCTypes.h"
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

typedef void *(*routine_fn)(void *);
#ifdef _WIN32
typedef HANDLE thread_t;
typedef HANDLE mutex_t;
typedef HANDLE semaphore_t;
typedef int pid_t;
#else
typedef pthread_t thread_t;
typedef pthread_mutex_t mutex_t;
typedef sem_t semaphore_t;
#endif

bool thread_create(thread_t *ref, routine_fn routine, void *data);
void thread_yield();

pid_t process_getid();

bool mutex_init(mutex_t *ref);
bool mutex_lock(mutex_t *ref);
bool mutex_unlock(mutex_t *ref);

semaphore_t *semaphore_open(char *name, unsigned int initValue);
bool semaphore_wait(semaphore_t *ref);
bool semaphore_unlock(semaphore_t *ref);

#endif // COMMIX_THREAD_UTIL_H
