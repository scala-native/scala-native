#ifndef REGISTERS_CAPTURE_H
#define REGISTERS_CAPTURE_H

#if defined(_WIN32)
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#endif

#if defined(__i386__) || defined(__x86__)
#define CAPTURE_X86
typedef struct RegistersBuffer {
    void *ebx;
    void *edi;
    void *esi;
} RegistersBuffer;

#elif defined(__x86_64__)
#define CAPTURE_X86_64
typedef struct RegistersBuffer {
    void *rbx;
    void *rbp;
    void *rdi;
    void *r12;
    void *r13;
    void *r14;
    void *r15;
    void *xmm[16 * 2];
} RegistersBuffer;

#else
#define CAPTURE_SETJMP
#include <setjmp.h>
typedef jmp_buf RegistersBuffer;
#endif

#ifdef CAPTURE_SETJMP
#define RegistersCapture(out) (void)setjmp(out);
#else
INLINE static void RegistersCapture(RegistersBuffer out) {
#ifdef CAPTURE_X86
    void *regEsi;
    void *regEdi;
    void *regEbx;
#ifdef __GNUC__
    asm("mov %%esi, %0\n\t" : "=r"(regEsi));
    asm("mov %%edi, %0\n\t" : "=r"(regEdi));
    asm("mov %%ebx, %0\n\t" : "=r"(regEbx));
#else // _WIN
    __asm {
      mov regEsi, esi
      mov regEdi, edi
      mov regEbx, ebx
    }
#endif
    out.esi = regEsi;
    out.edi = regEdi;
    out.ebx = regEbx;

#elif defined(CAPTURE_X86_64)
#ifdef _WIN32
    CONTEXT context;

    context.ContextFlags = CONTEXT_INTEGER;
    RtlCaptureContext(&context);

    out.rbx = (void *)context.Rbx;
    out.rbp = (void *)context.Rbp;
    out.rdi = (void *)context.Rdi;
    out.r12 = (void *)context.R12;
    out.r13 = (void *)context.R13;
    out.r14 = (void *)context.R14;
    out.r15 = (void *)context.R15;
    memcpy(out.xmm, &context.Xmm0, sizeof(out.xmm));
#else
    void *regBx;
    void *regBp;
    void *regDi;
    void *reg12;
    void *reg13;
    void *reg14;
    void *reg15;
    asm("movq %%rbx, %0\n\t" : "=r"(regBx));
    asm("movq %%rbp, %0\n\t" : "=r"(regBp));
    asm("movq %%rdi, %0\n\t" : "=r"(regDi));
    asm("movq %%r12, %0\n\t" : "=r"(reg12));
    asm("movq %%r13, %0\n\t" : "=r"(reg13));
    asm("movq %%r14, %0\n\t" : "=r"(reg14));
    asm("movq %%r15, %0\n\t" : "=r"(reg15));
    out.rbx = regBx;
    out.rbp = regBp;
    out.r12 = reg12;
    out.r13 = reg13;
    out.r14 = reg14;
    out.r15 = reg15;
#endif // GNU_C

#else
#error "Unable to capture registers state"
#endif // CaptureRegisters
}
#endif // RegistersCapture

#endif // REGISTERS_CAPTURE_H
