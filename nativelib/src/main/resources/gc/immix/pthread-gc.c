// Binds pthread_* functions to scalanative_pthread_* functions.
// This allows GCs to hook into pthreads.
// Every GC must include this file.

#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>

int scalanative_pthread_create(pthread_t *thread, const pthread_attr_t *attr,
                               void *(*start_routine)(void *), void *arg) {
    perror("Tried to create a thread when using immix GC\n");
    perror("This is not supported\n");
    exit(-1);

    return -1;
}

int scalanative_pthread_join(pthread_t thread, void **value_ptr) {
    return pthread_join(thread, value_ptr);
}

int scalanative_pthread_detach(pthread_t thread) {
    return pthread_detach(thread);
}

int scalanative_pthread_cancel(pthread_t thread) {
    return pthread_cancel(thread);
}

void scalanative_pthread_exit(void *retval) { pthread_exit(retval); }

// not bound in scala-native
/*
int scalanative_pthread_sigmask(int how, const sigset_t *set, sigset_t *oldset){
    return  pthread_sigmask(how, set, oldset);
}*/
