#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_DELIMCC)
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

#endif

#if defined(__linux__) && defined(__ELF__)
/* Reference:
 *   https://wiki.gentoo.org/wiki/Hardened/GNU_stack_quickstart
 */

.section .note.GNU-stack,"",%progbits

#endif
#endif