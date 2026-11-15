#ifndef DELIMCC_MULTISHOT_H
#define DELIMCC_MULTISHOT_H
#include "delimcc.h"
#include <stdbool.h>
#include <stdlib.h>
#include <setjmp.h>

/* Exception escapes from resumed continuations. setjmp/longjmp also save and
 * restore signal state (the signal mask and, on Darwin, the sigaltstack
 * flag); _setjmp/_longjmp only save registers. The escape stays on the same
 * thread, so restoring signal state is not needed, and a stale sigaltstack
 * flag breaks stack overflow handling. */
#ifdef _WIN32
#define SN_CONTINUATION_SETJMP(env) setjmp(env)
#define SN_CONTINUATION_LONGJMP(env, val) longjmp(env, val)
#else
#define SN_CONTINUATION_SETJMP(env) _setjmp(env)
#define SN_CONTINUATION_LONGJMP(env, val) _longjmp(env, val)
#endif

#ifdef __cplusplus
extern "C" {
#endif

// Allocates GC-visible memory on behalf of `alloc_arg`, the value given to
// `suspend`.
typedef void *ContinuationAllocFn(unsigned long size, void *alloc_arg);

/* Multi-shot (delimcc_multishot.c): the captured stack is copied out and
 * copied back on every resume, possibly to another address. */
void scalanative_continuation_multishot_init(ContinuationAllocFn *alloc_f);
void *scalanative_continuation_multishot_boundary(ContinuationBody *, void *);
void *scalanative_continuation_multishot_suspend(ContinuationBoundaryLabel b,
                                                 SuspendFn *f, void *arg,
                                                 void *alloc_arg);
void *scalanative_continuation_multishot_resume(Continuation *continuation,
                                                void *arg);
// Drops the handler chain of the current thread.
void scalanative_continuation_multishot_handlers_reset(void);
bool scalanative_continuation_multishot_on_current_stack(
    ContinuationBoundaryLabel label);
void *scalanative_continuation_multishot_boundary_probe(void);

/* Exception type compatible with eh.c (void* = Scala object). */
typedef void *Exception;

/*
 * Multi-shot only.
 * Exception escape from resumed continuations: when a resumed body throws and
 * no handler in the continuation catches it, we longjmp back to the resumer
 * instead of aborting (eh.c) or terminating (eh.cpp). The resumer can then
 * return Failure(exception) to the Scala side. Local try/catch inside the
 * continuation body is unaffected (unwinding finds those first).
 *
 * Use scalanative_continuation_exception_handler_set() immediately before
 * calling scalanative_continuation_resume(); resume clears the handler on both
 * normal return and exception escape.
 */

/* Thread-local state for exception escape; shared by eh.c and eh.cpp to handle
 * throwing exceptions from resumed continuations */
typedef struct ContinuationExceptionHandler {
    jmp_buf *env;
    Exception *exception_slot;
} ContinuationExceptionHandler;

/* Set the exception escape handler for the next resume. env is the address of
 * a jmp_buf (from setjmp); exception_slot is where eh.c / eh.cpp will store the
 * exception object when it longjmps. Pass NULL for both to clear the handler.
 */
void scalanative_continuation_exception_handler_set(
    ContinuationExceptionHandler handler);
ContinuationExceptionHandler scalanative_continuation_exception_handler(void);

/* Fallback escape route used by eh.c when _Unwind reaches end-of-stack and no
 * continuation exception handler is installed. Returns non-zero if escape was
 * handled (function does not return in that case), zero otherwise.
 */
int scalanative_continuation_exception_escape(Exception exception);

/* Clear the exception escape handler. Called by resume on both return paths. */
inline static void scalanative_continuation_exception_handler_clear(void) {
    ContinuationExceptionHandler handler = {NULL, NULL};
    scalanative_continuation_exception_handler_set(handler);
}

#ifdef SCALANATIVE_DELIMCC_DEBUG // Debug flag for delimcc

// Frees a continuation. Used only if malloc is used as the implementation of
// alloc function.
void scalanative_continuation_free(Continuation *continuation);

#endif // SCALANATIVE_DELIMCC_DEBUG

#ifdef __cplusplus
}
#endif
#endif // DELIMCC_MULTISHOT_H
