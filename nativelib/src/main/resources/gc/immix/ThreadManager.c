#include <signal.h>

#include "ThreadManager.h"
#include "datastructures/ThreadList.h"
#include "semaphore/Semaphore.h"
#include "State.h"

#define SIG_SUSPEND SIGXFSZ
#define SIG_RESUME SIGXCPU

static ucontext_t *suspendingThreadContext;
static Semaphore semaphore;

void ThreadManager_Init() {
    Semaphore_Init(&semaphore, 1);
    pthread_mutexattr_t mta;
    pthread_mutexattr_init(&mta);
    pthread_mutexattr_settype(&mta, PTHREAD_MUTEX_RECURSIVE);
    pthread_mutex_init(&mutex, &mta);
}

static void suspend_handler(int sig, siginfo_t *info, void *context) {
    sigset_t sigset;
    ucontext_t ucontext;

    // Push context on stack
    ucontext = *(ucontext_t *)context;

    suspendingThreadContext = &ucontext;

    sigfillset(&sigset);
    sigdelset(&sigset, SIG_RESUME);
    Semaphore_Post(&semaphore);
    sigsuspend(&sigset);
}

static void resume_handler(int sig) {}

inline void register_suspend_resume_handlers() {
    struct sigaction action;
    action.sa_flags = 0;
    action.sa_sigaction = suspend_handler;
    sigaction(SIG_SUSPEND, &action, (struct sigaction *)0);
    action.sa_handler = resume_handler;
    sigaction(SIG_RESUME, &action, (struct sigaction *)0);
}

void register_thread() {
    threadList = ThreadList_Cons(pthread_self(), threadList);
    register_suspend_resume_handlers();
}

void suspend_thread(pthread_t thread) {
    if (pthread_equal(pthread_self(), thread))
        return;
    int res = pthread_kill(thread, SIG_SUSPEND);
    if (res == 0) {
        Semaphore_Wait(&semaphore);
        ThreadList_SetContextForThread(thread, suspendingThreadContext,
                                       threadList);
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
