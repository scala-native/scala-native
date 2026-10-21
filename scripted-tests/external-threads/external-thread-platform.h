/* Shared native host primitives. This is test support, not a runtime header. */
#ifndef EXTERNAL_THREAD_PLATFORM_H
#define EXTERNAL_THREAD_PLATFORM_H

#ifdef _WIN32
#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#ifndef NOMINMAX
#define NOMINMAX
#endif
#include <windows.h>
#include <process.h>
#include <crtdbg.h>

typedef HANDLE host_thread_t;
typedef SRWLOCK host_mutex_t;
#define HOST_MUTEX_INIT SRWLOCK_INIT
static void host_mutex_lock(host_mutex_t *mutex) {
    AcquireSRWLockExclusive(mutex);
}
static void host_mutex_unlock(host_mutex_t *mutex) {
    ReleaseSRWLockExclusive(mutex);
}
static void host_mutex_close(host_mutex_t *mutex) { (void)mutex; }

typedef struct worker_call {
    void *(*function)(void *);
    void *argument;
} worker_call;
static unsigned __stdcall worker_trampoline(void *argument) {
    worker_call call = *(worker_call *)argument;
    free(argument);
    call.function(call.argument);
    return 0;
}
static host_thread_t worker_start_with_stack(void *(*function)(void *),
                                             void *argument,
                                             size_t stack_size) {
    worker_call *call = (worker_call *)malloc(sizeof(*call));
    CHECK(call);
    call->function = function;
    call->argument = argument;
    uintptr_t result = _beginthreadex(
        NULL, (unsigned)stack_size, worker_trampoline, call,
        stack_size ? STACK_SIZE_PARAM_IS_A_RESERVATION : 0, NULL);
    if (!result)
        free(call);
    CHECK(result);
    return (HANDLE)result;
}
static void worker_join(host_thread_t thread) {
    CHECK(WaitForSingleObject(thread, INFINITE) == WAIT_OBJECT_0);
    CHECK(CloseHandle(thread));
}
static void worker_exit(void) { _endthreadex(0); }
static void host_initialize(void) {
    /* Deliberate fatal scenarios must terminate without a desktop dialog. */
    SetErrorMode(SEM_FAILCRITICALERRORS | SEM_NOGPFAULTERRORBOX |
                 SEM_NOOPENFILEERRORBOX);
    _set_error_mode(_OUT_TO_STDERR);
    _set_abort_behavior(0, _WRITE_ABORT_MSG | _CALL_REPORTFAULT);
}
#else
#include <pthread.h>
#include <signal.h>

typedef pthread_t host_thread_t;
typedef pthread_mutex_t host_mutex_t;
#define HOST_MUTEX_INIT PTHREAD_MUTEX_INITIALIZER
static void host_mutex_lock(host_mutex_t *mutex) {
    CHECK(pthread_mutex_lock(mutex) == 0);
}
static void host_mutex_unlock(host_mutex_t *mutex) {
    CHECK(pthread_mutex_unlock(mutex) == 0);
}
static void host_mutex_close(host_mutex_t *mutex) {
    CHECK(pthread_mutex_destroy(mutex) == 0);
}
static host_thread_t worker_start_with_stack(void *(*function)(void *),
                                             void *argument,
                                             size_t stack_size) {
    pthread_attr_t attr;
    CHECK(pthread_attr_init(&attr) == 0);
    if (stack_size)
        CHECK(pthread_attr_setstacksize(&attr, stack_size) == 0);
    pthread_t result;
    CHECK(pthread_create(&result, &attr, function, argument) == 0);
    CHECK(pthread_attr_destroy(&attr) == 0);
    return result;
}
static void worker_join(host_thread_t thread) {
    CHECK(pthread_join(thread, NULL) == 0);
}
static void worker_exit(void) { pthread_exit(NULL); }
static void host_initialize(void) {}
#endif

static host_thread_t worker_start(void *(*function)(void *), void *argument) {
    return worker_start_with_stack(function, argument, 0);
}

typedef struct event {
    host_mutex_t mutex;
#ifdef _WIN32
    CONDITION_VARIABLE cond;
#else
    pthread_cond_t cond;
#endif
    int signaled;
} event;
#ifdef _WIN32
#define EVENT_INIT {SRWLOCK_INIT, CONDITION_VARIABLE_INIT, 0}
#else
#define EVENT_INIT {PTHREAD_MUTEX_INITIALIZER, PTHREAD_COND_INITIALIZER, 0}
#endif
static void event_set(event *e) {
    host_mutex_lock(&e->mutex);
    e->signaled = 1;
#ifdef _WIN32
    WakeAllConditionVariable(&e->cond);
#else
    CHECK(pthread_cond_broadcast(&e->cond) == 0);
#endif
    host_mutex_unlock(&e->mutex);
}
static void event_wait(event *e) {
    host_mutex_lock(&e->mutex);
    while (!e->signaled) {
#ifdef _WIN32
        CHECK(SleepConditionVariableSRW(&e->cond, &e->mutex, INFINITE, 0));
#else
        CHECK(pthread_cond_wait(&e->cond, &e->mutex) == 0);
#endif
    }
    host_mutex_unlock(&e->mutex);
}
static void event_reset(event *e) {
    host_mutex_lock(&e->mutex);
    e->signaled = 0;
    host_mutex_unlock(&e->mutex);
}
static int event_is_set(event *e) {
    host_mutex_lock(&e->mutex);
    int result = e->signaled;
    host_mutex_unlock(&e->mutex);
    return result;
}
static void event_close(event *e) {
#ifndef _WIN32
    CHECK(pthread_cond_destroy(&e->cond) == 0);
#endif
    host_mutex_close(&e->mutex);
}
#endif
