#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_DELIMCC)

#if defined(__x86_64__) && (defined(__linux__) || defined(__APPLE__) || defined(__FreeBSD__) || defined(__OpenBSD__) || defined(__NetBSD__))

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
.global _lh_boundary_entry
.global _lh_resume_entry
.global _lh_get_sp

/* under MacOSX the c-compiler adds underscores to cdecl functions
   add these labels too so the linker can resolve it. */
.global __lh_setjmp
.global __lh_longjmp
.global __lh_boundary_entry
.global __lh_resume_entry
.global __lh_get_sp

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
ok:
  movq  16 (%rdi), %rsp       /* restore the stack pointer */
  jmpq *(%rdi)                /* and jump to rip */

_lh_boundary_entry: 
__lh_boundary_entry: /* rdi: arg 1, rsi : arg 2, (rdx: arg 3) */
  movq  %rsi, %rdx
  movq  %rdi, %rsi
  movq  %rsp, %rdi
  addq  $8, %rdi
  pushq %rbx /* for rsp alignment */
  call  __continuation_boundary_impl
  popq  %rbx
  ret

_lh_resume_entry: /* rdi = cont_size, rsi = cont, rdx = arg */
__lh_resume_entry:
  movq  0 (%rsp), %rcx /* store lr */
  movq  %rsp, %rax /* store sp */
  subq  %rdi, %rsp /* move sp */
  movq  %rax, %rdi /* pass old sp as arg 1 */
  addq  $8, %rdi   /* forget about lr in stack tail */
  jmp   __continuation_resume_impl /* it will just return from here */


_lh_get_sp:
__lh_get_sp:
  movq %rsp, %rax
  addq $8, %rax
  ret

#endif // setjmp_arm64.S

#if defined(__i386__) && (defined(__linux__) || defined(__APPLE__))

/* ----------------------------------------------------------------------------
// Copyright (c) 2016, 2017 Microsoft Research, Daan Leijen
// This is free software// you can redistribute it and/or modify it under the
// terms of the Apache License, Version 2.0.
// -----------------------------------------------------------------------------

// -------------------------------------------------------
// Code for x86 (ia32) cdecl calling convention on Unix's.
// Differs from the win32 x86 calling convention since it
// does not use fs:0 for exception handling. See:
// - <https://en.wikipedia.org/wiki/X86_calling_conventions>
// - <https://www.uclibc.org/docs/psABI-i386.pdf> System V Application Binary Interface i386
//
// jump_buf layout:
//  0: ebp
//  4: ebx
//  8: edi
// 12: esi
// 16: esp
// 20: eip
// 24: sse control word (32 bits)
// 28: fpu control word (16 bits)
// 30: unused
// 32: sizeof jmp_buf
// ------------------------------------------------------- */

.global _lh_setjmp
.global _lh_longjmp
.global _lh_boundary_entry
.global _lh_resume_entry
.global _lh_get_sp

/* under MacOSX gcc silently adds underscores to cdecl functions;
   add these labels too so the linker can resolve it. */
.global __lh_setjmp
.global __lh_longjmp
.global __lh_boundary_entry
.global __lh_resume_entry
.global __lh_get_sp

/* called with jmp_buf at sp+4 */
__lh_setjmp:
_lh_setjmp:
  movl    4 (%esp), %ecx   /* jmp_buf to ecx  */
  movl    0 (%esp), %eax   /* eip: save the return address */
  movl    %eax, 20 (%ecx)

  leal    4 (%esp), %eax   /* save esp (minus return address) */
  movl    %eax, 16 (%ecx)

  movl    %ebp,  0 (%ecx)  /* save registers */
  movl    %ebx,  4 (%ecx)
  movl    %edi,  8 (%ecx)
  movl    %esi, 12 (%ecx)

  stmxcsr 24 (%ecx)        /* save sse control word */
  fnstcw  28 (%ecx)        /* save fpu control word */

  xorl    %eax, %eax       /* return zero */
  ret


/* called with jmp_buf at esp+4, and arg at sp+8 */
__lh_longjmp:
_lh_longjmp:
  movl    8 (%esp), %eax      /* set eax to the return value (arg) */
  movl    4 (%esp), %ecx      /* set ecx to jmp_buf */

  movl    0 (%ecx), %ebp      /* restore registers */
  movl    4 (%ecx), %ebx
  movl    8 (%ecx), %edi
  movl    12 (%ecx), %esi

  ldmxcsr 24 (%ecx)           /* restore sse control word */
  fnclex                      /* clear fpu exception flags */
  fldcw   28 (%ecx)           /* restore fpu control word */

  testl   %eax, %eax          /* longjmp should never return 0 */
  jnz     ok
  incl    %eax
ok:
  movl    16 (%ecx), %esp     /* restore esp */
  jmpl    *20 (%ecx)          /* and jump to the eip */

/* f : esp+4, arg : esp+8 */
/* __continuation_boundary_impl: btm, f, arg */
/*
 stack should look like this pre-call:
   arg
   *ret*
   eip -- old esp
   arg
   f
   old_esp+8 -- new esp = old esp - 12
*/
_lh_boundary_entry:
__lh_boundary_entry:
  /* copy arguments */
  movl    4 (%esp), %edx /* f */
  movl    8 (%esp), %eax /* arg */
  leal    12 (%esp), %ecx /* set btm */
  /* set esp and call */
  pushl   %eax
  pushl   %edx
  pushl   %ecx
  call    __continuation_boundary_impl
  addl    $12, %esp
  /* return */
  ret


_lh_resume_entry: /* cont_size, cont, arg */
__lh_resume_entry:
  movl  %esp, %eax     /* store sp */
  movl  4 (%esp), %edx /* take cont_size */
  subl  %edx, %esp     /* move sp */
  /* set esp and call */
  movl  8 (%eax), %ecx /* f */
  movl  12 (%eax), %edx /* arg */
  pushl $__lh_resume_entry_ret /* lr */
  pushl %edx
  pushl %ecx
  pushl %eax
  calll __continuation_resume_impl
/* note that from now we return from _lh_boundary_entry */
__lh_resume_entry_ret:
  addl  $8, %esp
  ret


_lh_get_sp:
__lh_get_sp:
  leal   4 (%esp), %eax
  ret

#endif // setjmp_arm32.S

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
.global _lh_longjmp
.global _lh_boundary_entry
.global _lh_resume_entry
.global _lh_get_sp
#if !defined(__APPLE__)
.type _lh_setjmp,%function
.type _lh_longjmp,%function
.type _lh_boundary_entry,%function
.type _lh_resume_entry,%function
.type _lh_get_sp,%function
#endif

.balign 4
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

.balign 4
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
    cmp   x1, #0
    cinc  x0, x1, eq
    ret                         /* jump to lr */

.balign 4
_lh_boundary_entry:
    mov   x2, x1
    mov   x1, x0
    mov   x0, sp
    sub   sp, sp, #16
    str	  x30, [sp, #8]                  // 8-byte Folded Spill
    bl    __continuation_boundary_impl
    ldr	  x30, [sp, #8]                  // 8-byte Folded Spill
    add   sp, sp, #16
    ret

.balign 4
_lh_resume_entry: /* x0 = cont_size, x1 = cont, x2 = arg */
    sub   sp, sp, x0
    add   x0, sp, x0
    mov   x3, x30 /* copy lr */
    bl    __continuation_resume_impl /* it will just return from here */

.balign 4
_lh_get_sp:
    mov  x0, sp
    ret

#endif // setjmp_arm64.S

#if defined(__x86_64__) && defined(_WIN64)

/* ----------------------------------------------------------------------------
  Copyright (c) 2016, Microsoft Research, Daan Leijen
  This is free software; you can redistribute it and/or modify it under the
  terms of the Apache License, Version 2.0. A copy of the License can be
  found in the file "license.txt" at the root of this distribution.
-----------------------------------------------------------------------------*/

/*
Code for x64 (x86_64) calling convention as used on Windows and mingw64
see: <https://en.wikipedia.org/wiki/X86_calling_conventions>
and: <https://msdn.microsoft.com/en-us/library/ms235286.aspx>

jump_buf layout (compatible with msvc):
   0: rdx ( frame pointer on msvc)
   8: rbx
  16: rsp
  24: rbp
  32: rsi
  40: rdi
  48: r12
  56: r13
  64: r14
  72: r15
  80: rip
  88: sse control word
  92: fpu control word
  94: unused
  96: xmm6
  ... (128-bit registers)
 240: xmm15
 256: sizeof jmp_buf
*/

.global _lh_setjmp
.global _lh_longjmp
.global _lh_boundary_entry
.global _lh_resume_entry
.global _lh_get_sp

/* Sometimes the c-compiler adds underscores to cdecl functions
   add these labels too so the linker can resolve it. */
.global __lh_setjmp
.global __lh_longjmp
.global __lh_boundary_entry
.global __lh_resume_entry
.global __lh_get_sp

/* called with jmp_buf at sp+4 */
__lh_setjmp:
_lh_setjmp:                 /* input: rcx: jmp_buf, rdx: frame pointer */
  movq    (%rsp), %rax      /* return address is on the stack */
  movq    %rax, 80 (%rcx)   /* rip */

  leaq    8 (%rsp), %rax
  movq    %rax, 16 (%rcx)   /* rsp: just from before the return address */

  movq    %rdx,  0 (%rcx)   /* save registers */
  movq    %rbx,  8 (%rcx)
  movq    %rbp, 24 (%rcx)
  movq    %rsi, 32 (%rcx)
  movq    %rdi, 40 (%rcx)
  movq    %r12, 48 (%rcx)
  movq    %r13, 56 (%rcx)
  movq    %r14, 64 (%rcx)
  movq    %r15, 72 (%rcx)

  stmxcsr 88 (%rcx)          /* save sse control word */
  fnstcw  92 (%rcx)          /* save fpu control word */

  movdqu  %xmm6,   96 (%rcx) /* save sse registers */
  movdqu  %xmm7,  112 (%rcx)
  movdqu  %xmm8,  128 (%rcx)
  movdqu  %xmm9,  144 (%rcx)
  movdqu  %xmm10, 160 (%rcx)
  movdqu  %xmm11, 176 (%rcx)
  movdqu  %xmm12, 192 (%rcx)
  movdqu  %xmm13, 208 (%rcx)
  movdqu  %xmm14, 224 (%rcx)
  movdqu  %xmm15, 240 (%rcx)

  xor     %rax, %rax          /* return 0 */
  ret

__lh_longjmp:
_lh_longjmp:                  /* rcx: jmp_buf, edx: arg */
  movq  %rdx, %rax            /* return arg to rax */

  movq   0 (%rcx), %rdx       /* restore registers */
  movq   8 (%rcx), %rbx
  movq  24 (%rcx), %rbp
  movq  32 (%rcx), %rsi
  movq  40 (%rcx), %rdi
  movq  48 (%rcx), %r12
  movq  56 (%rcx), %r13
  movq  64 (%rcx), %r14
  movq  72 (%rcx), %r15

  ldmxcsr 88 (%rcx)           /* restore sse control word */
  fnclex                      /* clear fpu exception flags */
  fldcw   92 (%rcx)           /* restore fpu control word */

  movdqu   96 (%rcx), %xmm6   /* restore sse registers */
  movdqu  112 (%rcx), %xmm7
  movdqu  128 (%rcx), %xmm8
  movdqu  144 (%rcx), %xmm9
  movdqu  160 (%rcx), %xmm10
  movdqu  176 (%rcx), %xmm11
  movdqu  192 (%rcx), %xmm12
  movdqu  208 (%rcx), %xmm13
  movdqu  224 (%rcx), %xmm14
  movdqu  240 (%rcx), %xmm15

  testl %eax, %eax            /* longjmp should never return 0 */
  jnz   ok
  incl  %eax
ok:
  movq  16 (%rcx), %rsp        /* set the stack frame */
  jmpq *80 (%rcx)              /* and jump to rip */

_lh_boundary_entry: 
__lh_boundary_entry: /* rcx: arg 1, rdx : arg 2, (r8: arg 3) */
  movq  %rdx, %r8
  movq  %rcx, %rdx
  movq  %rsp, %rcx
  addq  $8, %rcx
  subq  $40, %rsp /* for rsp alignment 8 bytes + home 8*4 bytes */
  call  __continuation_boundary_impl
  addq  $40, %rsp
  ret

_lh_resume_entry: /* rcx = cont_size, rdx = cont, r8 = arg */
__lh_resume_entry:
  movq  0 (%rsp), %r9 /* store lr */
  movq  %rsp, %rax /* store sp */
  subq  %rcx, %rsp /* move sp */
  movq  %rax, %rcx /* pass old sp as arg 1 */
  addq  $8, %rcx   /* forget about lr in stack tail */
  jmp   __continuation_resume_impl /* it will just return from here */


_lh_get_sp:
__lh_get_sp:
  movq %rsp, %rax
  addq $8, %rax
  ret

#endif // setjmp_x64.S
#endif // SCALANATIVE_COMPILE_ALWAYS) || __SCALANATIVE_DELIMCC

#if defined(__linux__) && defined(__ELF__)
/* Reference:
 *   https://wiki.gentoo.org/wiki/Hardened/GNU_stack_quickstart
 */

.section .note.GNU-stack,"",%progbits

#endif // __linux__ && __ELF__
