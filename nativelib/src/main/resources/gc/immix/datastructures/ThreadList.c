#include <stdlib.h>
#include "ThreadList.h"

ThreadList *ThreadList_Cons(pthread_t thread, ThreadList *threadList) {
    ThreadList *current = threadList;
    while (current != NULL) {
        if (pthread_equal(current->thread, thread)) {
            return threadList;
        }
    }
    ThreadList *res = malloc(sizeof(ThreadList));
    res->thread = thread;
    res->next = threadList;
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

void ThreadList_SetContextForThread(pthread_t thread, ucontext_t *context,
                                    ThreadList *threadList) {
    for (ThreadList *list = threadList; list != NULL; list = list->next) {
        if (pthread_equal(list->thread, thread)) {
            list->context = context;
            break;
        }
    }
}
