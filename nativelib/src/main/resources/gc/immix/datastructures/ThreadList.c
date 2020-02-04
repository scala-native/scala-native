#include <stdlib.h>
#include <stdbool.h>
#include <stdio.h>
#include "ThreadList.h"

ThreadList *ThreadList_Cons(pthread_t thread, void *stackBottom, ThreadList *threadList) {
    ThreadList *current = threadList;
    while (current != NULL) {
        if (pthread_equal(current->thread, thread)) {
            return threadList;
        }
        current = current->next;
    }
    ThreadList *res = malloc(sizeof(ThreadList));
    res->thread = thread;
    res->stackBottom = stackBottom;
    res->next = threadList;
    return res;
}

ThreadList *ThreadList_Remove(pthread_t thread, ThreadList *threadList) {
    if (threadList == NULL)
        return NULL;
    if (pthread_equal(threadList->thread, thread)) {
        ThreadList *res = threadList->next;
        free(threadList);
        return res;
    }
    ThreadList *current = threadList;
    while (current->next != NULL &&
           !pthread_equal(current->next->thread, thread))
        current = current->next;
    if (current != NULL) {
        ThreadList *toRemove = current->next;
        current->next = current->next->next;
        free(toRemove);
    }
    return threadList;
}

void ThreadList_Free(ThreadList *threadList) {
    ThreadList *current = threadList;
    while (current != NULL) {
        ThreadList *toFree = current;
        current = current->next;
        free(toFree);
    }
}

void ThreadList_SetStackTopForThread(pthread_t thread, void *stackTop, ThreadList *threadList) {
    for (ThreadList *list = threadList; list != NULL; list = list->next) {
        if (pthread_equal(list->thread, thread)) {
            list->stackTop = stackTop;
            break;
        }
    }
}
