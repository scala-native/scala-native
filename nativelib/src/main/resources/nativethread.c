#include <sys/types.h>
#include <stdint.h>
#include <pthread.h>
#include <sched.h>
#include <stdlib.h>
#include <stdio.h>

pthread_attr_t PTHREAD_DEFAULT_ATTR;
struct sched_param PTHREAD_DEFAULT_SCHED_PARAM;
int PTHREAD_DEFAULT_POLICY;
size_t PTHREAD_DEFAULT_STACK_SIZE;
int initialized = 0;

void init() {
    pthread_attr_init(&PTHREAD_DEFAULT_ATTR);
    pthread_attr_getschedparam(&PTHREAD_DEFAULT_ATTR,
                               &PTHREAD_DEFAULT_SCHED_PARAM);
    pthread_attr_getschedpolicy(&PTHREAD_DEFAULT_ATTR, &PTHREAD_DEFAULT_POLICY);
    pthread_attr_getstacksize(&PTHREAD_DEFAULT_ATTR,
                              &PTHREAD_DEFAULT_STACK_SIZE);

    initialized = 1;
}

int get_max_priority() {
    if (!initialized)
        init();
    return sched_get_priority_max(PTHREAD_DEFAULT_POLICY);
}

int get_min_priority() {
    if (!initialized)
        init();
    return sched_get_priority_min(PTHREAD_DEFAULT_POLICY);
}

int get_norm_priority() {
    if (!initialized)
        init();
    return PTHREAD_DEFAULT_SCHED_PARAM.sched_priority;
}

size_t get_stack_size() {
    if (!initialized)
        init();
    return PTHREAD_DEFAULT_STACK_SIZE;
}

void attr_set_priority(pthread_attr_t *attr, int priority) {
    struct sched_param param;

    pthread_attr_setschedparam(attr, &param);
    param.sched_priority = priority;

    pthread_attr_setschedparam(attr, &param);
}

void set_priority(pthread_t thread, int priority) {
    struct sched_param param;
    int policy;

    pthread_getschedparam(thread, &policy, &param);
    param.sched_priority = priority;

    pthread_setschedparam(thread, policy, &param);
}
