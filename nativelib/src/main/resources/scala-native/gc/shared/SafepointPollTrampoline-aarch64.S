/* clang-format off */
/*
 * POSIX aarch64: same role as SafepointPollTrampoline-x86_64.S.
 * Saves Q0-Q31, X0-X30, NZCV and FPSR, calls run_yield + load_resume_pc, br
 * to resume via x16 (clobbered by generated polls).
 * See docs/contrib/gc-safepoint-trampoline.md.
 */
#if defined(SCALANATIVE_MULTITHREADING_ENABLED) &&                              \
    defined(SCALANATIVE_GC_USE_YIELDPOINT_TRAPS) && !defined(_WIN32) &&         \
    defined(__aarch64__) && (defined(__linux__) || defined(__APPLE__))

/* x0-x30 (248 B), nzcv, fpsr, resume pc + pad, q0-q31 (512 B) */
#define SFPT_NZCV 248
#define SFPT_FPSR 256
#define SFPT_RESUME 272
#define SFPT_Q 288
#define SFPT_FRAME 800

#if defined(__APPLE__)
/* Darwin lets leaf functions use 128 bytes below sp; prepare_redirect skips
 * them. Keep in sync with SN_RED_ZONE in SafepointPollTrampoline.c */
#define SFPT_RED_ZONE 128
#else
#define SFPT_RED_ZONE 0
#endif

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
    sub sp, sp, #SFPT_FRAME

    stp x0, x1, [sp, #0]
    stp x2, x3, [sp, #16]
    stp x4, x5, [sp, #32]
    stp x6, x7, [sp, #48]
    stp x8, x9, [sp, #64]
    stp x10, x11, [sp, #80]
    stp x12, x13, [sp, #96]
    stp x14, x15, [sp, #112]
    stp x16, x17, [sp, #128]
    stp x18, x19, [sp, #144]
    stp x20, x21, [sp, #160]
    stp x22, x23, [sp, #176]
    stp x24, x25, [sp, #192]
    stp x26, x27, [sp, #208]
    stp x28, x29, [sp, #224]
    str x30, [sp, #240]

    mrs x0, nzcv
    str x0, [sp, #SFPT_NZCV]
    mrs x0, fpsr
    str x0, [sp, #SFPT_FPSR]

    stp q0, q1, [sp, #(SFPT_Q + 0)]
    stp q2, q3, [sp, #(SFPT_Q + 32)]
    stp q4, q5, [sp, #(SFPT_Q + 64)]
    stp q6, q7, [sp, #(SFPT_Q + 96)]
    stp q8, q9, [sp, #(SFPT_Q + 128)]
    stp q10, q11, [sp, #(SFPT_Q + 160)]
    stp q12, q13, [sp, #(SFPT_Q + 192)]
    stp q14, q15, [sp, #(SFPT_Q + 224)]
    stp q16, q17, [sp, #(SFPT_Q + 256)]
    stp q18, q19, [sp, #(SFPT_Q + 288)]
    stp q20, q21, [sp, #(SFPT_Q + 320)]
    stp q22, q23, [sp, #(SFPT_Q + 352)]
    stp q24, q25, [sp, #(SFPT_Q + 384)]
    stp q26, q27, [sp, #(SFPT_Q + 416)]
    stp q28, q29, [sp, #(SFPT_Q + 448)]
    stp q30, q31, [sp, #(SFPT_Q + 480)]

    bl RUN_YIELD
    bl LOAD_RESUME
    str x0, [sp, #SFPT_RESUME]

    ldp q30, q31, [sp, #(SFPT_Q + 480)]
    ldp q28, q29, [sp, #(SFPT_Q + 448)]
    ldp q26, q27, [sp, #(SFPT_Q + 416)]
    ldp q24, q25, [sp, #(SFPT_Q + 384)]
    ldp q22, q23, [sp, #(SFPT_Q + 352)]
    ldp q20, q21, [sp, #(SFPT_Q + 320)]
    ldp q18, q19, [sp, #(SFPT_Q + 288)]
    ldp q16, q17, [sp, #(SFPT_Q + 256)]
    ldp q14, q15, [sp, #(SFPT_Q + 224)]
    ldp q12, q13, [sp, #(SFPT_Q + 192)]
    ldp q10, q11, [sp, #(SFPT_Q + 160)]
    ldp q8, q9, [sp, #(SFPT_Q + 128)]
    ldp q6, q7, [sp, #(SFPT_Q + 96)]
    ldp q4, q5, [sp, #(SFPT_Q + 64)]
    ldp q2, q3, [sp, #(SFPT_Q + 32)]
    ldp q0, q1, [sp, #(SFPT_Q + 0)]

    ldr x0, [sp, #SFPT_FPSR]
    msr fpsr, x0
    ldr x0, [sp, #SFPT_NZCV]
    msr nzcv, x0

    ldr x30, [sp, #240]
    ldp x28, x29, [sp, #224]
    ldp x26, x27, [sp, #208]
    ldp x24, x25, [sp, #192]
    ldp x22, x23, [sp, #176]
    ldp x20, x21, [sp, #160]
    ldp x18, x19, [sp, #144]
    ldp x16, x17, [sp, #128]
    ldp x14, x15, [sp, #112]
    ldp x12, x13, [sp, #96]
    ldp x10, x11, [sp, #80]
    ldp x8, x9, [sp, #64]
    ldp x6, x7, [sp, #48]
    ldp x4, x5, [sp, #32]
    ldp x2, x3, [sp, #16]
    ldp x0, x1, [sp, #0]

    ldr x16, [sp, #SFPT_RESUME]
    add sp, sp, #(SFPT_FRAME + SFPT_RED_ZONE)
    br x16

#endif

#if defined(__linux__) && defined(__ELF__)
.section .note.GNU-stack,"",%progbits
#endif
