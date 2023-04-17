#if defined(__aarch64__)
/* ----------------------------------------------------------------------------
  Copyright (c) 2016, 2017, Microsoft Research, Daan Leijen
  This is free software; you can redistribute it and/or modify it under the
  terms of the Apache License, Version 2.0. A copy of the License can be
  found in the file "license.txt" at the root of this distribution.
-----------------------------------------------------------------------------*/

/*
Code for ARM 64-bit.
See:
- <https://en.wikipedia.org/wiki/Calling_convention#ARM_.28A64.29>
- <http://infocenter.arm.com/help/topic/com.arm.doc.ihi0055c/IHI0055C_beta_aapcs64.pdf>

notes: 
- According to the ARM ABI specification, only the bottom 64 bits of the floating 
  point registers need to be preserved (sec. 5.1.2 of aapcs64)
- The x18 register is the "platform register" and may be temporary or not. For safety
  we always save it.

jump_buf layout:
   0: x18  
   8: x19
  16: x20
  24: x21
  32: x22
  40: x23
  48: x24
  56: x25
  64: x26
  72: x27
  80: x28
  88: fp   = x29
  96: lr   = x30
 104: sp   = x31
 112: fpcr
 120: fpsr
 128: d8  (64 bits)
 136: d9
 ...
 184: d15
 192: sizeof jmp_buf
*/

.global _lh_setjmp
.global _lh_store_lr_longjmp
.global _lh_longjmp
.type _lh_setjmp,%function
.type _lh_store_lr_longjmp,%function
.type _lh_longjmp,%function

/* called with x0: &jmp_buf */
_lh_setjmp:                 
    stp   x18, x19, [x0], #16
    stp   x20, x21, [x0], #16
    stp   x22, x23, [x0], #16
    stp   x24, x25, [x0], #16
    stp   x26, x27, [x0], #16
    stp   x28, x29, [x0], #16   /* x28 and fp */
    mov   x10, sp               /* sp to x10 */
    stp   x30, x10, [x0], #16   /* lr and sp */
    /* store fp control and status */
    mrs   x10, fpcr
    mrs   x11, fpsr
    stp   x10, x11, [x0], #16    
    /* store float registers */
    stp   d8,  d9,  [x0], #16
    stp   d10, d11, [x0], #16
    stp   d12, d13, [x0], #16
    stp   d14, d15, [x0], #16
    /* always return zero */
    mov   x0, #0
    ret                         /* jump to lr */

    
/* called with x0: &jmp_buf, x1: address to paste the return address into */
_lh_store_lr_longjmp:                  
    str   x30, [x1]
    ldp   x18, x19, [x0], #16
    ldp   x20, x21, [x0], #16
    ldp   x22, x23, [x0], #16
    ldp   x24, x25, [x0], #16
    ldp   x26, x27, [x0], #16
    ldp   x28, x29, [x0], #16   /* x28 and fp */
    ldp   x30, x10, [x0], #16   /* lr and sp */
    mov   sp,  x10
    /* load fp control and status */
    ldp   x10, x11, [x0], #16
    msr   fpcr, x10
    msr   fpsr, x11
    /* load float registers */
    ldp   d8,  d9,  [x0], #16
    ldp   d10, d11, [x0], #16
    ldp   d12, d13, [x0], #16
    ldp   d14, d15, [x0], #16
    /* never return zero */
    mov   x0, #1
    cmp   w1, #0
    cinc  w0, w1, eq
    ret                         /* jump to lr */

/* called with x0: &jmp_buf, x1: value to return */
_lh_longjmp:
    ldp   x18, x19, [x0], #16
    ldp   x20, x21, [x0], #16
    ldp   x22, x23, [x0], #16
    ldp   x24, x25, [x0], #16
    ldp   x26, x27, [x0], #16
    ldp   x28, x29, [x0], #16   /* x28 and fp */
    ldp   x30, x10, [x0], #16   /* lr and sp */
    mov   sp,  x10
    /* load fp control and status */
    ldp   x10, x11, [x0], #16
    msr   fpcr, x10
    msr   fpsr, x11
    /* load float registers */
    ldp   d8,  d9,  [x0], #16
    ldp   d10, d11, [x0], #16
    ldp   d12, d13, [x0], #16
    ldp   d14, d15, [x0], #16
    /* never return zero */
    mov   x0, x1
    cmp   w1, #0
    cinc  w0, w1, eq
    ret                         /* jump to lr */

#elif defined(__x86_64__) && (defined(__linux__) || defined(__APPLE__))

/* ----------------------------------------------------------------------------
  Copyright (c) 2016, Microsoft Research, Daan Leijen
  This is free software; you can redistribute it and/or modify it under the
  terms of the Apache License, Version 2.0. A copy of the License can be
  found in the file "license.txt" at the root of this distribution.
-----------------------------------------------------------------------------*/

/*
Code for amd64 calling convention on x86_64: Solaris, Linux, FreeBSD, OS X
- <https://en.wikipedia.org/wiki/X86_calling_conventions>
- <http://chamilo2.grenet.fr/inp/courses/ENSIMAG3MM1LDB/document/doc_abi_ia64.pdf>, page 21
- <http://www.agner.org/optimize/calling_conventions.pdf>, page 10

jump_buf layout (compatible with FreeBSD):
   0: rip
   8: rbx
  16: rsp
  24: rbp
  32: r12
  40: r13
  48: r14
  56: r15
  64: fpcr, fpu control word (16 bits)
  66: unused
  68: mxcsr, sse status register (32 bits)
  72: sizeof jmp_buf
*/

.global _lh_setjmp
.global _lh_longjmp
.global _lh_store_lr_longjmp

/* under MacOSX the c-compiler adds underscores to cdecl functions
   add these labels too so the linker can resolve it. */
.global __lh_setjmp
.global __lh_longjmp
.global __lh_store_lr_longjmp

__lh_setjmp:
_lh_setjmp:                 /* rdi: jmp_buf */
  movq    (%rsp), %rax      /* rip: return address is on the stack */
  movq    %rax, 0 (%rdi)

  leaq    8 (%rsp), %rax    /* rsp - return address */
  movq    %rax, 16 (%rdi)

  movq    %rbx,  8 (%rdi)   /* save registers */
  movq    %rbp, 24 (%rdi)
  movq    %r12, 32 (%rdi)
  movq    %r13, 40 (%rdi)
  movq    %r14, 48 (%rdi)
  movq    %r15, 56 (%rdi)

  fnstcw  64 (%rdi)          /* save fpu control word */
  stmxcsr 68 (%rdi)          /* save sse control word */

  xor     %rax, %rax         /* return 0 */
  ret

__lh_longjmp:
_lh_longjmp:                  /* rdi: jmp_buf, rsi: arg */
  movq  %rsi, %rax            /* return arg to rax */

  movq   8 (%rdi), %rbx       /* restore registers */
  movq  24 (%rdi), %rbp
  movq  32 (%rdi), %r12
  movq  40 (%rdi), %r13
  movq  48 (%rdi), %r14
  movq  56 (%rdi), %r15

  ldmxcsr 68 (%rdi)           /* restore sse control word */
  fnclex                      /* clear fpu exception flags */
  fldcw   64 (%rdi)           /* restore fpu control word */

  testl %eax, %eax            /* longjmp should never return 0 */
  jnz   ok
  incl  %eax

__lh_store_lr_longjmp:
_lh_store_lr_longjmp:         /* rdi: jmp_buf, rsi: addr */
  ; movq   0 (%rsp), %rax               
  ; movq   %rax, 0 (%rsi)
  movq   $1, %rax               /* return 1 to rax */

  movq   8 (%rdi), %rbx       /* restore registers */
  movq  24 (%rdi), %rbp
  movq  32 (%rdi), %r12
  movq  40 (%rdi), %r13
  movq  48 (%rdi), %r14
  movq  56 (%rdi), %r15

  ldmxcsr 68 (%rdi)           /* restore sse control word */
  fnclex                      /* clear fpu exception flags */
  fldcw   64 (%rdi)           /* restore fpu control word */

  testl %eax, %eax            /* longjmp should never return 0 */
  jnz   ok
  incl  %eax
ok:
  movq  16 (%rdi), %rsp       /* restore the stack pointer */
  jmpq *(%rdi)                /* and jump to rip */

#endif
