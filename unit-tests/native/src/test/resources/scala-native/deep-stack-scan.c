#include <alloca.h>
#include <stddef.h>
#include <stdint.h>

// Leading fields of ThreadInfo (nativeThreadTLS.h).
typedef struct {
    size_t stackSize;
    size_t maxStackSize;
    void *stackTop;
    void *stackBottom;
} ThreadInfoPrefix;

extern ThreadInfoPrefix *scalanative_currentThreadInfo(void);

// Runs callback with a slot 64KB below the soft stack limit, without touching
// the stack in between, as a frame larger than a page would. Returns 0 when
// the thread's stack does not extend that far.
int scalanative_test_call_below_soft_limit(void (*callback)(void **)) {
    ThreadInfoPrefix *info = scalanative_currentThreadInfo();
    char here;
    uintptr_t target = (uintptr_t)info->stackTop - 64 * 1024;
    uintptr_t realLow = (uintptr_t)info->stackBottom - info->maxStackSize;
    if (info->maxStackSize <= info->stackSize ||
        target < realLow + 256 * 1024 || target >= (uintptr_t)&here)
        return 0;
    void **slot = (void **)alloca((uintptr_t)&here - target);
    *slot = NULL;
    callback(slot);
    __asm__ volatile("" : : "r"(slot) : "memory");
    return 1;
}
