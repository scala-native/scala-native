
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
  call  __cont_boundary_impl
  popq  %rbx
  ret

_lh_resume_entry: /* rdi = cont_size, rsi = cont, rdx = arg */
__lh_resume_entry:
  movq  0 (%rsp), %rcx /* store lr */
  movq  %rsp, %rax /* store sp */
  subq  %rdi, %rsp /* move sp */
  movq  %rax, %rdi /* pass old sp as arg 1 */
  addq  $8, %rdi   /* forget about lr in stack tail */
  jmp   __cont_resume_impl /* it will just return from here */


_lh_get_sp:
__lh_get_sp:
  movq %rsp, %rax
  addq $8, %rax
  ret

#endif
; #elif defined(__x86_64__) && defined(_WIN64)