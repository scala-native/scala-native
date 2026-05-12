/* clang-format off */
/*
 * POSIX x86_64: normal-context stub after a yield-point page fault.
 * Saves XMM0-15 and GPRs, calls scalanative_gc_safepoint_poll_run_yield (C),
 * loads resume PC from scalanative_gc_safepoint_load_resume_pc, jmp to it.
 * See SafepointPollTrampoline.c and docs/contrib/gc-safepoint-trampoline.md.
 */
#if defined(SCALANATIVE_MULTITHREADING_ENABLED) &&                              \
    defined(SCALANATIVE_GC_USE_YIELDPOINT_TRAPS) && !defined(_WIN32) &&         \
    defined(__x86_64__) && (defined(__linux__) || defined(__APPLE__))

/* Frame: 16 XMM (256 B) + 8 resume + 15 GPR (120 B) + pad = 400 B */
#define SFPT_FRAME 400

.text
.balign 16

#if defined(__APPLE__)
#    define RUN_YIELD _scalanative_gc_safepoint_poll_run_yield
#    define LOAD_RESUME _scalanative_gc_safepoint_load_resume_pc
.globl scalanative_gc_safepoint_poll_trampoline
.globl _scalanative_gc_safepoint_poll_trampoline
scalanative_gc_safepoint_poll_trampoline:
_scalanative_gc_safepoint_poll_trampoline:
#else
#    define RUN_YIELD scalanative_gc_safepoint_poll_run_yield
#    define LOAD_RESUME scalanative_gc_safepoint_load_resume_pc
.globl scalanative_gc_safepoint_poll_trampoline
.type scalanative_gc_safepoint_poll_trampoline, @function
scalanative_gc_safepoint_poll_trampoline:
#endif
    subq $SFPT_FRAME, %rsp

    movdqa %xmm0, 0(%rsp)
    movdqa %xmm1, 16(%rsp)
    movdqa %xmm2, 32(%rsp)
    movdqa %xmm3, 48(%rsp)
    movdqa %xmm4, 64(%rsp)
    movdqa %xmm5, 80(%rsp)
    movdqa %xmm6, 96(%rsp)
    movdqa %xmm7, 112(%rsp)
    movdqa %xmm8, 128(%rsp)
    movdqa %xmm9, 144(%rsp)
    movdqa %xmm10, 160(%rsp)
    movdqa %xmm11, 176(%rsp)
    movdqa %xmm12, 192(%rsp)
    movdqa %xmm13, 208(%rsp)
    movdqa %xmm14, 224(%rsp)
    movdqa %xmm15, 240(%rsp)

    movq %rax, 272(%rsp)
    movq %rbx, 280(%rsp)
    movq %rcx, 288(%rsp)
    movq %rdx, 296(%rsp)
    movq %rsi, 304(%rsp)
    movq %rdi, 312(%rsp)
    movq %r8,  320(%rsp)
    movq %r9,  328(%rsp)
    movq %r10, 336(%rsp)
    movq %r11, 344(%rsp)
    movq %rbp, 352(%rsp)
    movq %r12, 360(%rsp)
    movq %r13, 368(%rsp)
    movq %r14, 376(%rsp)
    movq %r15, 384(%rsp)

    call RUN_YIELD
    call LOAD_RESUME
    movq %rax, 256(%rsp)

    movq 384(%rsp), %r15
    movq 376(%rsp), %r14
    movq 368(%rsp), %r13
    movq 360(%rsp), %r12
    movq 352(%rsp), %rbp
    movq 344(%rsp), %r11
    movq 336(%rsp), %r10
    movq 328(%rsp), %r9
    movq 320(%rsp), %r8
    movq 312(%rsp), %rdi
    movq 304(%rsp), %rsi
    movq 296(%rsp), %rdx
    movq 288(%rsp), %rcx
    movq 280(%rsp), %rbx
    movq 272(%rsp), %rax

    movdqa 240(%rsp), %xmm15
    movdqa 224(%rsp), %xmm14
    movdqa 208(%rsp), %xmm13
    movdqa 192(%rsp), %xmm12
    movdqa 176(%rsp), %xmm11
    movdqa 160(%rsp), %xmm10
    movdqa 144(%rsp), %xmm9
    movdqa 128(%rsp), %xmm8
    movdqa 112(%rsp), %xmm7
    movdqa 96(%rsp), %xmm6
    movdqa 80(%rsp), %xmm5
    movdqa 64(%rsp), %xmm4
    movdqa 48(%rsp), %xmm3
    movdqa 32(%rsp), %xmm2
    movdqa 16(%rsp), %xmm1
    movdqa 0(%rsp), %xmm0

    movq 256(%rsp), %r10
    addq $SFPT_FRAME, %rsp
    jmp *%r10

#else
.text
.balign 4
.globl scalanative_gc_safepoint_trampoline_x86_64_skipped
scalanative_gc_safepoint_trampoline_x86_64_skipped:
    ret
#endif
