#include <pthread.h>
#include <sys/types.h>

int scalanative_pthread_cancel_asynchronous() {
    return PTHREAD_CANCEL_ASYNCHRONOUS;
}

int scalanative_pthread_cancel_enable() {
    return PTHREAD_CANCEL_ENABLE;
}

int scalanative_pthread_cancel_ered() {
    return PTHREAD_CANCEL_DEFERRED;
}

int scalanative_pthread_cancel_disable() {
    return PTHREAD_CANCEL_DISABLE;
}

void* scalanative_pthread_canceled() {
    return PTHREAD_CANCELED;
}

int scalanative_pthread_create_deteached() {
    return PTHREAD_CREATE_DETACHED;
}

int scalanative_pthread_create_joinale() {
    return PTHREAD_CREATE_JOINABLE;
}

int scalanative_pthread_explicit_sched() {
    return PTHREAD_EXPLICIT_SCHED;
}

int scalanative_pthread_inherit_sched() {
    return PTHREAD_INHERIT_SCHED;
}

int scalanative_pthread_mutex_ault() {
    return PTHREAD_MUTEX_DEFAULT;
}

int scalanative_pthread_mutex_errorcheck() {
    return PTHREAD_MUTEX_ERRORCHECK;
}

int scalanative_pthread_mutex_normal() {
    return PTHREAD_MUTEX_NORMAL;
}

int scalanative_pthread_mutex_recursive() {
    return PTHREAD_MUTEX_RECURSIVE;
}

pthread_once_t scalanative_pthread_once_init() {
    pthread_once_t once_block = PTHREAD_ONCE_INIT;
    return once_block;
}

int scalanative_pthread_prio_inherit() {
    return PTHREAD_PRIO_INHERIT;
}

int scalanative_pthread_prio_none() {
    return PTHREAD_PRIO_NONE;
}

int scalanative_pthread_prio_protect() {
    return PTHREAD_PRIO_PROTECT;
}

int scalanative_pthread_process_shared() {
    return PTHREAD_PROCESS_SHARED;
}

int scalanative_pthread_process_private() {
    return PTHREAD_PROCESS_PRIVATE;
}

int scalanative_pthread_scope_process() {
    return PTHREAD_SCOPE_PROCESS;
}

int scalanative_pthread_scope_system() {
    return PTHREAD_SCOPE_SYSTEM;
}

pthread_cond_t scalanative_pthread_cond_initializer() {
    return (pthread_cond_t)PTHREAD_COND_INITIALIZER;
}

pthread_mutex_t scalanative_pthread_mutex_initializer() {
    return (pthread_mutex_t)PTHREAD_MUTEX_INITIALIZER;
}

pthread_rwlock_t scalanative_pthread_rwlock_initializer() {
    return (pthread_rwlock_t)PTHREAD_RWLOCK_INITIALIZER;
}
