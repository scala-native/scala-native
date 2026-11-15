#include <stddef.h>

#if defined(__has_feature)
#if __has_feature(address_sanitizer)
#define SN_TEST_ASAN 1
#endif
#endif

extern void scalanative_GC_collect(void);

#ifdef SN_TEST_ASAN
void *__asan_get_current_fake_stack(void);
void *__asan_addr_is_in_fake_stack(void *fake_stack, void *addr, void **beg,
                                   void **end);
#endif

int scalanative_test_slot_is_in_fake_stack(void **slot) {
#ifdef SN_TEST_ASAN
    void *fakeStack = __asan_get_current_fake_stack();
    void *begin = NULL;
    void *end = NULL;
    if (fakeStack == NULL || slot == NULL)
        return 0;
    return __asan_addr_is_in_fake_stack(fakeStack, slot, &begin, &end) != NULL;
#else
    (void)slot;
    return 0;
#endif
}

void scalanative_test_collect_with_escaped_slot(void **slot) {
    (void)slot;
    scalanative_GC_collect();
}
