#if defined(__SCALANATIVE_POSIX_PTHREAD)
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
#include <pthread.h>
#include <sys/types.h>
#include <string.h>

int scalanative_pthread_cancel_asynchronous() {
    return PTHREAD_CANCEL_ASYNCHRONOUS;
}

int scalanative_pthread_cancel_enable() { return PTHREAD_CANCEL_ENABLE; }

int scalanative_pthread_cancel_deferred() { return PTHREAD_CANCEL_DEFERRED; }

int scalanative_pthread_cancel_disable() { return PTHREAD_CANCEL_DISABLE; }

void *scalanative_pthread_canceled() { return PTHREAD_CANCELED; }

int scalanative_pthread_create_detached() { return PTHREAD_CREATE_DETACHED; }

int scalanative_pthread_create_joinable() { return PTHREAD_CREATE_JOINABLE; }

int scalanative_pthread_explicit_sched() { return PTHREAD_EXPLICIT_SCHED; }

int scalanative_pthread_inherit_sched() { return PTHREAD_INHERIT_SCHED; }

int scalanative_pthread_mutex_default() { return PTHREAD_MUTEX_DEFAULT; }

int scalanative_pthread_mutex_errorcheck() { return PTHREAD_MUTEX_ERRORCHECK; }

int scalanative_pthread_mutex_normal() { return PTHREAD_MUTEX_NORMAL; }

int scalanative_pthread_mutex_recursive() { return PTHREAD_MUTEX_RECURSIVE; }

pthread_once_t scalanative_pthread_once_init() {
    // On macOS, PTHREAD_ONCE_INIT is defined as an expression
    pthread_once_t once_block = PTHREAD_ONCE_INIT;
    return once_block;
}

int scalanative_pthread_prio_inherit() { return PTHREAD_PRIO_INHERIT; }

int scalanative_pthread_prio_none() { return PTHREAD_PRIO_NONE; }

int scalanative_pthread_prio_protect() { return PTHREAD_PRIO_PROTECT; }

int scalanative_pthread_process_shared() { return PTHREAD_PROCESS_SHARED; }

int scalanative_pthread_process_private() { return PTHREAD_PROCESS_PRIVATE; }

int scalanative_pthread_scope_process() { return PTHREAD_SCOPE_PROCESS; }

int scalanative_pthread_scope_system() { return PTHREAD_SCOPE_SYSTEM; }

// The following are not part of the POSIX spec

size_t scalanative_pthread_t_size() { return sizeof(pthread_t); }

size_t scalanative_pthread_attr_t_size() { return sizeof(pthread_attr_t); }

size_t scalanative_pthread_cond_t_size() { return sizeof(pthread_cond_t); }

size_t scalanative_pthread_condattr_t_size() {
    return sizeof(pthread_condattr_t);
}

size_t scalanative_pthread_mutex_t_size() { return sizeof(pthread_mutex_t); }

size_t scalanative_pthread_mutexattr_t_size() {
    return sizeof(pthread_mutexattr_t);
}

#endif // Unix or Mac OS
#endif