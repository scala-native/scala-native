#if !defined(_WIN32) && (defined(__aarch64__) || defined(__x86_64__)) &&       \
    (defined(__clang__) || defined(__GNUC__))

#include <stddef.h>

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

// Clears stale stack slots, then collects. `used`: referenced only from asm.
__attribute__((noinline, used)) void
scalanative_test_callee_saved_roots_collect(void) {
    volatile char pad[16 * 1024];
    for (size_t i = 0; i < sizeof(pad); i++)
        pad[i] = 0;
    scalanative_GC_collect();
    __asm__ volatile("");
}

int scalanative_test_callee_saved_roots_supported(void) { return 1; }

// Holds slots[0..1] only in callee-saved registers during a collection.
// clang-format off
__attribute__((naked)) void
scalanative_test_collect_holding_in_callee_saved_registers(void **slots) {
#if defined(__aarch64__)
    __asm__ volatile(
        "stp x29, x30, [sp, #-48]!\n"
        "mov x29, sp\n"
        "stp x27, x28, [sp, #16]\n"
        "str x19, [sp, #32]\n"
        "mov x19, x0\n"
        "ldr x28, [x19]\n"
        "ldr x27, [x19, #8]\n"
        "str xzr, [x19]\n"
        "str xzr, [x19, #8]\n"
        CALL(scalanative_test_callee_saved_roots_collect)
        "str x28, [x19]\n"
        "str x27, [x19, #8]\n"
        "ldr x19, [sp, #32]\n"
        "ldp x27, x28, [sp, #16]\n"
        "ldp x29, x30, [sp], #48\n"
        "ret\n");
#else
    __asm__ volatile(
        "pushq %rbp\n"
        "pushq %r13\n"
        "pushq %rbx\n"
        "movq %rdi, %rbx\n"
        "movq (%rbx), %r13\n"
        "movq 8(%rbx), %rbp\n"
        "movq $0, (%rbx)\n"
        "movq $0, 8(%rbx)\n"
        CALL(scalanative_test_callee_saved_roots_collect)
        "movq %r13, (%rbx)\n"
        "movq %rbp, 8(%rbx)\n"
        "popq %rbx\n"
        "popq %r13\n"
        "popq %rbp\n"
        "ret\n");
#endif
}
// clang-format on

#else
int scalanative_test_callee_saved_roots_supported(void) { return 0; }
void scalanative_test_collect_holding_in_callee_saved_registers(void **slots) {
    (void)slots;
}
#endif
