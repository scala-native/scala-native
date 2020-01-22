#ifndef IMMIX_THREADLIST_H
#define IMMIX_THREADLIST_H

#include <pthread.h>
#define _XOPEN_SOURCE
#include <ucontext.h>

/**
 * Mutable LinkedList of pthreads.
 */
typedef struct ThreadList {
    pthread_t thread;
    ucontext_t *context;
    struct ThreadList *next;
} ThreadList;

/**
 * Adds @thread as head of @threadList if it is not already present.
 * Returns the list with prepended element.
 */
ThreadList *ThreadList_Cons(pthread_t thread, ThreadList *threadList);

/**
 * Removes @thread from @threadList.
 * Returns the list with the element removed.
 * It is needed to update the list pointer with the returned pointer
 * since the head can be removed.
 */
ThreadList *ThreadList_Remove(pthread_t thread, ThreadList *threadList);

/**
 * Frees @threadList.
 * The contained threads are not freed.
 */
void ThreadList_Free(ThreadList *threadList);

/**
 * Frees @threadList.
 * The contained threads are not freed.
 */
void ThreadList_SetContextForThread(pthread_t thread, ucontext_t *context,
                                    ThreadList *threadList);

#endif // IMMIX_THREADLIST_H
