#if !defined(_WIN32) &&                                                        \
    (defined(SCALANATIVE_COMPILE_ALWAYS) ||                                    \
     defined(__SCALANATIVE_DELIMCC_ONESHOT))

/*
 * Entry of a new stack of a one-shot continuation (delimcc_oneshot.c).
 *
 * The first switch to a coroutine jumps here with the stack pointer at the
 * coroutine itself, which lives at the high end of its stack. Passes it to
 * scalanative_continuation_main, which never returns. The frame pointer is
 * null, so stack walks end here.
 */

#if defined(__APPLE__)
#define SYMBOL(name) _##name
#define HIDDEN(name) .private_extern SYMBOL(name)
#else
#define SYMBOL(name) name
#define HIDDEN(name) .hidden SYMBOL(name)
#endif

    .text
    .globl SYMBOL(scalanative_continuation_entry)
    HIDDEN(scalanative_continuation_entry)

#if defined(__aarch64__)
    .balign 4
SYMBOL(scalanative_continuation_entry):
    hint  #36 /* bti j */
    mov   x0, sp
    bl    SYMBOL(scalanative_continuation_main)
    brk   #1

#elif defined(__x86_64__)
    .balign 16
SYMBOL(scalanative_continuation_entry):
    movq  %rsp, %rdi
    callq SYMBOL(scalanative_continuation_main)
    ud2

#elif defined(__i386__)
    .balign 16
SYMBOL(scalanative_continuation_entry):
    movl  %esp, %eax
    subl  $12, %esp
    pushl %eax
    calll SYMBOL(scalanative_continuation_main)
    ud2

#elif defined(__arm__)
    .arm
    .type scalanative_continuation_entry, %function
    .balign 4
scalanative_continuation_entry:
    .fnstart
    .cantunwind
    mov   r0, sp
    bl    scalanative_continuation_main
    udf   #1
    .fnend

#elif defined(__riscv) && __riscv_xlen == 64
    .balign 4
scalanative_continuation_entry:
    mv    a0, sp
    call  scalanative_continuation_main
    unimp

#endif

#endif // one-shot continuations

#if defined(__linux__) && defined(__ELF__)
    .section .note.GNU-stack,"",%progbits
#endif
