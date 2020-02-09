#include <setjmp.h>
#include <signal.h>
#include <stdio.h>

#include "ThreadManager.h"
#include "State.h"

#define SIG_SUSPEND SIGXFSZ
#define SIG_RESUME SIGXCPU

void ThreadManager_suspendHandler(int sig) {
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

void ThreadManager_resumeHandler(int sig) {}

void ThreadManager_Init() {
    Semaphore_Init(&semaphore, 0);
    threadList = NULL;

    struct sigaction suspend_action;
    sigemptyset(&suspend_action.sa_mask);
    sigaddset(&suspend_action.sa_mask, SIG_RESUME);
    suspend_action.sa_flags = 0;
    suspend_action.sa_handler = ThreadManager_suspendHandler;
    sigaction(SIG_SUSPEND, &suspend_action, NULL);

    struct sigaction resume_action;
    sigemptyset(&resume_action.sa_mask);
    resume_action.sa_flags = 0;
    resume_action.sa_handler = ThreadManager_resumeHandler;
    sigaction(SIG_RESUME, &resume_action, NULL);
}

void ThreadManager_RegisterThread(void *stackBottom) {
    threadList = ThreadList_Cons(pthread_self(), stackBottom, threadList);
}

void ThreadManager_suspendThread(pthread_t thread) {
    if (pthread_equal(pthread_self(), thread))
        return;
    int res = pthread_kill(thread, SIG_SUSPEND);
    if (res == 0) {
        Semaphore_Wait(&semaphore);
        ThreadList_SetStackTopForThread(thread, suspendingThreadStackTop,
                                        threadList);
    } else {
        threadList = ThreadList_Remove(thread, threadList);
    }
}

void ThreadManager_resumeThread(pthread_t thread) {
    if (pthread_equal(pthread_self(), thread))
        return;
    int res = pthread_kill(thread, SIG_RESUME);
    if (res != 0) {
        threadList = ThreadList_Remove(thread, threadList);
    }
}

void ThreadManager_SuspendAllThreads() {
    ThreadList *current = threadList;
    while (current != NULL) {
        ThreadManager_suspendThread(current->thread);
        current = current->next;
    }
}

void ThreadManager_ResumeAllThreads() {
    ThreadList *current = threadList;
    while (current != NULL) {
        ThreadManager_resumeThread(current->thread);
        current = current->next;
    }
}
