#include "nativeThreadTLS.h"
#include <string.h>
#ifdef _MSC_VER
#include <intrin.h>
#endif

#if defined(__linux__)
extern int pthread_getattr_np(pthread_t thread, pthread_attr_t *attr);
#elif defined(__FreeBSD__)
#include <pthread_np.h>
#endif

bool scalanative_detectExternalThreadStack(ThreadInfo *info) {
    void *low = NULL;
    void *high = NULL;
    size_t size = 0;
    /* Unlike a local variable, the actual frame cannot be moved onto ASan's
     * fake stack. Stack discovery must describe the OS stack in that build too.
     */
#ifdef _MSC_VER
    void *onStack = _AddressOfReturnAddress();
#else
    void *onStack = __builtin_frame_address(0);
#endif

#ifdef _WIN32
    typedef void(WINAPI * StackLimitsFn)(PULONG_PTR, PULONG_PTR);
    HMODULE kernel = GetModuleHandleW(L"kernel32.dll");
    StackLimitsFn limits = kernel == NULL
                               ? NULL
                               : (StackLimitsFn)GetProcAddress(
                                     kernel, "GetCurrentThreadStackLimits");
    if (limits == NULL)
        return false;
    ULONG_PTR lower, upper;
    limits(&lower, &upper);
    if (upper <= lower)
        return false;
    low = (void *)lower;
    high = (void *)upper;
    size = upper - lower;
#elif defined(__linux__) || defined(__FreeBSD__)
    pthread_attr_t attr;
#if defined(__linux__)
    if (pthread_getattr_np(pthread_self(), &attr) != 0)
        return false;
#else
    if (pthread_attr_init(&attr) != 0)
        return false;
    if (pthread_attr_get_np(pthread_self(), &attr) != 0) {
        pthread_attr_destroy(&attr);
        return false;
    }
#endif
    int status = pthread_attr_getstack(&attr, &low, &size);
    pthread_attr_destroy(&attr);
    if (status != 0 || (uintptr_t)low > UINTPTR_MAX - size)
        return false;
    high = (void *)((uintptr_t)low + size);
#elif defined(__APPLE__) && defined(__MACH__)
    high = pthread_get_stackaddr_np(pthread_self());
    size = pthread_get_stacksize_np(pthread_self());
    if ((uintptr_t)high < size)
        return false;
    low = (void *)((uintptr_t)high - size);
#else
    return false;
#endif

    if (!low || !high || !size || (uintptr_t)onStack < (uintptr_t)low ||
        (uintptr_t)onStack >= (uintptr_t)high)
        return false;

    ThreadInfo discovered = {0};
    discovered.stackTop = low;
    discovered.stackBottom = high;
    discovered.stackSize = size;
    discovered.maxStackSize = size;
    *info = discovered;
    return true;
}
