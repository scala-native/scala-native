#include <setjmp.h>
#include <signal.h>
#include <stdio.h>

#include "ThreadManager.h"
#include "semaphore/Semaphore.h"
#include "State.h"

#define SIG_SUSPEND SIGXFSZ
#define SIG_RESUME SIGXCPU

void *suspendingThreadStackTop;
Semaphore semaphore;

void ThreadManager_Init() {
    Semaphore_Init(&semaphore, 1);
    pthread_mutexattr_t mta;
    pthread_mutexattr_init(&mta);
    pthread_mutexattr_settype(&mta, PTHREAD_MUTEX_RECURSIVE);
    pthread_mutex_init(&mutex, &mta);
    threadList = NULL;
}

void suspend_handler(int sig) {
    sigset_t sigset;

    jmp_buf regs;
    setjmp(regs);
    word_t *dummy;

    suspendingThreadStackTop = &dummy;

    sigfillset(&sigset);
    sigdelset(&sigset, SIG_RESUME);
    Semaphore_Post(&semaphore);
    sigsuspend(&sigset);
}

void resume_handler(int sig) {}

void ThreadManager_Register_Thread(void *stackBottom) {
    // threadList = ThreadList_Cons(pthread_self(), stackBottom, threadList);

    struct sigaction action;
    action.sa_flags = 0;
    action.sa_handler = suspend_handler;
    sigaction(SIG_SUSPEND, &action, NULL);
    action.sa_handler = resume_handler;
    sigaction(SIG_RESUME, &action, NULL);
}

void suspend_thread(pthread_t thread) {
    if (pthread_equal(pthread_self(), thread))
        return;
    int res = pthread_kill(thread, SIG_SUSPEND);
    if (res == 0) {
        Semaphore_Wait(&semaphore);
        ThreadList_SetStackTopForThread(thread, suspendingThreadStackTop, threadList);
    } else {
        threadList = ThreadList_Remove(thread, threadList);
    }
}

void resume_thread(pthread_t thread) {
    if (pthread_equal(pthread_self(), thread))
        return;
    int res = pthread_kill(thread, SIG_RESUME);
    if (res != 0) {
        threadList = ThreadList_Remove(thread, threadList);
    }
}

void suspend_all_threads() {
    ThreadList *current = threadList;
    while (current != NULL) {
        suspend_thread(current->thread);
        current = current->next;
    }
}

void resume_all_threads() {
    ThreadList *current = threadList;
    while (current != NULL) {
        resume_thread(current->thread);
        current = current->next;
    }
}
