/* clang-format off */
/*
 * POSIX aarch64: same role as SafepointPollTrampoline-x86_64.S.
 * Saves Q0–Q7 and X0–X30, calls run_yield + load_resume_pc, br to resume.
 * See docs/contrib/gc-safepoint-trampoline.md.
 */
#if defined(SCALANATIVE_MULTITHREADING_ENABLED) &&                              \
    defined(SCALANATIVE_GC_USE_YIELDPOINT_TRAPS) && !defined(_WIN32) &&         \
    defined(__aarch64__) && (defined(__linux__) || defined(__APPLE__))

/* q0–q7 caller-saved (128 B) + x0–x30 + resume + pad */
#define SFPT_FRAME 384

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

    stp q0, q1, [sp, #0]
    stp q2, q3, [sp, #32]
    stp q4, q5, [sp, #64]
    stp q6, q7, [sp, #96]

    stp x0, x1, [sp, #128]
    stp x2, x3, [sp, #144]
    stp x4, x5, [sp, #160]
    stp x6, x7, [sp, #176]
    stp x8, x9, [sp, #192]
    stp x10, x11, [sp, #208]
    stp x12, x13, [sp, #224]
    stp x14, x15, [sp, #240]
    stp x16, x17, [sp, #256]
    stp x18, x19, [sp, #272]
    stp x20, x21, [sp, #288]
    stp x22, x23, [sp, #304]
    stp x24, x25, [sp, #320]
    stp x26, x27, [sp, #336]
    stp x28, x29, [sp, #352]
    str x30, [sp, #368]

    bl RUN_YIELD
    bl LOAD_RESUME
    str x0, [sp, #376]

    ldr x30, [sp, #368]
    ldp x28, x29, [sp, #352]
    ldp x26, x27, [sp, #336]
    ldp x24, x25, [sp, #320]
    ldp x22, x23, [sp, #304]
    ldp x20, x21, [sp, #288]
    ldp x18, x19, [sp, #272]
    ldp x16, x17, [sp, #256]
    ldp x14, x15, [sp, #240]
    ldp x12, x13, [sp, #224]
    ldp x10, x11, [sp, #208]
    ldp x8, x9, [sp, #192]
    ldp x6, x7, [sp, #176]
    ldp x4, x5, [sp, #160]
    ldp x2, x3, [sp, #144]
    ldp x0, x1, [sp, #128]

    ldp q6, q7, [sp, #96]
    ldp q4, q5, [sp, #64]
    ldp q2, q3, [sp, #32]
    ldp q0, q1, [sp, #0]

    ldr x16, [sp, #376]
    add sp, sp, #SFPT_FRAME
    br x16

#else
.text
.balign 4
.globl scalanative_gc_safepoint_trampoline_aarch64_skipped
scalanative_gc_safepoint_trampoline_aarch64_skipped:
    ret
#endif
