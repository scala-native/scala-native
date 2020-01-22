#include "StackPointers.h"

void *StackTop(ucontext_t *context) {
#if defined(__APPLE__)
    return (void *)context->uc_mcontext->__ss.__rsp;
#endif
}
void *StackBottom(pthread_t thread) {
#if defined(__APPLE__)
    return (void *)pthread_get_stackaddr_np(thread);
#endif
}