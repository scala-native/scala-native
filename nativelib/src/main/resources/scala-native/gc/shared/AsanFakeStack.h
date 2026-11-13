#ifndef ASAN_FAKE_STACK_H
#define ASAN_FAKE_STACK_H

#include <stdbool.h>
#include <stddef.h>

// Check if this is compiled with -fsanitize=address
#if defined(__has_feature)
#if __has_feature(address_sanitizer)
#define SN_ASAN_FAKE_STACK 1
#endif
#endif

#ifdef SN_ASAN_FAKE_STACK
void *__asan_get_current_fake_stack(void);
void *__asan_addr_is_in_fake_stack(void *fake_stack, void *addr, void **beg,
                                   void **end);
#endif

// wrappers for
// https://github.com/llvm/llvm-project/blob/3889c5d7d3570b6f858c91b4d08cf0843c0fa229/compiler-rt/include/sanitizer/asan_interface.h#L345-L373

static inline void *AsanFakeStack_current(void) {
#ifdef SN_ASAN_FAKE_STACK
    return __asan_get_current_fake_stack();
#else
    return NULL;
#endif
}

static inline bool AsanFakeStack_frame(void *fakeStack, void *addr,
                                       void **begin, void **end) {
#ifdef SN_ASAN_FAKE_STACK
    if (fakeStack == NULL || addr == NULL)
        return false;
    return __asan_addr_is_in_fake_stack(fakeStack, addr, begin, end) != NULL;
#else
    (void)fakeStack;
    (void)addr;
    (void)begin;
    (void)end;
    return false;
#endif
}

#endif
