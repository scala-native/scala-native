#define _XOPEN_SOURCE
#include <ucontext.h>
#include <pthread.h>

void *StackTop(ucontext_t *context);
void *StackBottom(pthread_t thread);
