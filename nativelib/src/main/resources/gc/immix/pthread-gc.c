#include <pthread.h>
#include <stdio.h>
#include "ThreadManager.h"
#include "State.h"

// typedef struct {
//     void *(*start_routine)(void *);
//     void *arg;
// } thread_arg_t;

// void *scalanative_pthread_create_callback(thread_arg_t *arg) {
//     register_thread();
//     return arg->start_routine(arg->arg);
// }

int scalanative_pthread_create(pthread_t *thread,
                               const pthread_attr_t *attr,
                               void *(*start_routine)(void *),
                               void *arg) {
    // thread_arg_t newArg;
    // newArg.start_routine = start_routine;
    // newArg.arg = arg;

    // pthread_create(thread, attr, scalanative_pthread_create_callback,
    // &newArg);
    int res = pthread_create(thread, attr, start_routine, arg);
    return res;
}
