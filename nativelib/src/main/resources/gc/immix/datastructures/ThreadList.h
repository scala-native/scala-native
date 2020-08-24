#ifndef IMMIX_THREADLIST_H
#define IMMIX_THREADLIST_H

#include <pthread.h>

/**
 * Mutable LinkedList of pthreads.
 */
typedef struct ThreadList {
    pthread_t thread;
    void *stackBottom;
    void *stackTop;
    struct ThreadList *next;
} ThreadList;

/**
 * Adds @thread with @stackBottom as head of @threadList if it is not already present.
 * Returns the list with prepended element.
 */
ThreadList *ThreadList_Cons(pthread_t thread, void *stackBottom, ThreadList *threadList);

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
 * Sets @stackTop pointer for @thread in @threadList.
 */
void ThreadList_SetStackTopForThread(pthread_t thread, void *stackTop, ThreadList *threadList);

#endif // IMMIX_THREADLIST_H
