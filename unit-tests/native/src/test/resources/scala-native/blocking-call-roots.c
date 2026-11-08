#if !defined(_WIN32) && defined(SCALANATIVE_MULTITHREADING_ENABLED) &&         \
    (defined(__aarch64__) || defined(__x86_64__)) && defined(__clang__)

#include <pthread.h>
#include <stddef.h>

extern int scalanative_GC_pthread_create(pthread_t *, pthread_attr_t *,
                                         void *(*)(void *), void *);
extern void scalanative_GC_collect(void);

#define STRINGIFY(x) #x
#define SYMBOL_(prefix, name) STRINGIFY(prefix) #name
#define SYMBOL(name) SYMBOL_(__USER_LABEL_PREFIX__, name)
#if defined(__x86_64__) && !defined(__APPLE__)
#define CALL(name) "call " SYMBOL(name) "@PLT\n"
#elif defined(__x86_64__)
#define CALL(name) "call " SYMBOL(name) "\n"
#else
#define CALL(name) "bl " SYMBOL(name) "\n"
#endif

static void *collect(void *unused) {
    (void)unused;
    scalanative_GC_collect();
    return NULL;
}

// Overwrites the stack left by the state switch, then collects from another
// thread with the callee-saved registers spilled far below the stack top.
__attribute__((noinline)) void scalanative_test_collect_in_deep_frame(void) {
    volatile char pad[16 * 1024];
    for (size_t i = 0; i < sizeof(pad); i++)
        pad[i] = 0;
    pthread_t thread;
    if (scalanative_GC_pthread_create(&thread, NULL, collect, NULL) == 0)
        pthread_join(thread, NULL);
    __asm__ volatile("");
}

int scalanative_test_blocking_call_roots_supported(void) { return 1; }

// Takes the object out of *slot and keeps it only in callee-saved registers
// across a blocking call, as compiled Scala code does.
// clang-format off
__attribute__((naked)) void
scalanative_test_hold_in_callee_saved_registers(void **slot) {
#if defined(__aarch64__)
    __asm__ volatile(
        "stp x29, x30, [sp, #-32]!\n"
        "mov x29, sp\n"
        "stp x19, x20, [sp, #16]\n"
        "ldr x19, [x0]\n"
        "str xzr, [x0]\n"
        "mov x20, x19\n"
        "mov w0, #1\n"
        CALL(scalanative_GC_set_mutator_thread_state)
        CALL(scalanative_test_collect_in_deep_frame)
        "mov w0, #0\n"
        CALL(scalanative_GC_set_mutator_thread_state)
        "ldp x19, x20, [sp, #16]\n"
        "ldp x29, x30, [sp], #32\n"
        "ret\n");
#else
    __asm__ volatile(
        "pushq %rbp\n"
        "movq %rsp, %rbp\n"
        "pushq %rbx\n"
        "pushq %r12\n"
        "movq (%rdi), %rbx\n"
        "movq $0, (%rdi)\n"
        "movq %rbx, %r12\n"
        "movl $1, %edi\n"
        CALL(scalanative_GC_set_mutator_thread_state)
        CALL(scalanative_test_collect_in_deep_frame)
        "xorl %edi, %edi\n"
        CALL(scalanative_GC_set_mutator_thread_state)
        "popq %r12\n"
        "popq %rbx\n"
        "popq %rbp\n"
        "ret\n");
#endif
}
// clang-format on

#else
int scalanative_test_blocking_call_roots_supported(void) { return 0; }
void scalanative_test_hold_in_callee_saved_registers(void **slot) {
    (void)slot;
}
#endif
