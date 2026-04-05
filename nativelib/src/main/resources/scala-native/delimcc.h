#ifndef DELIMCC_H
#define DELIMCC_H
#include <stdlib.h>
#include <setjmp.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef unsigned long ContinuationBoundaryLabel;

typedef struct Continuation Continuation;

// ContinuationBody = ContBoundaryLabel -> any -> any
typedef void *ContinuationBody(ContinuationBoundaryLabel, void *);

// SuspendFn = Continuation -> any -> any
typedef void *SuspendFn(Continuation *, void *);

// Initializes the continuation helpers,
// set the allocation function for Continuations and stack fragments.
// without calling this, malloc is the default allocation function.
// The allocation function may take another parameter, as given in
// `scalanative_continuation_suspend`.
void scalanative_continuation_init(void *(*alloc_f)(unsigned long, void *));

// cont_boundary : ContinuationBody -> any -> any
// Installs a boundary handler and passes the boundary label associated with
// the handler to the ContinuationBody. Returns the return value of
// ContinuationBody (or the `scalanative_continuation_suspend` result
// corresponding to this handler).
void *scalanative_continuation_boundary(ContinuationBody *, void *);

// cont_suspend[T, R] : BoundaryLabel[T] -> (Continuation[T, R] -> T) -> R
// Suspends to the boundary handler corresponding to the given boundary label,
// reifying the suspended computation up to (and including) the handler as a
// Continuation struct, and passing it to the SuspendFn (alongside with `arg`),
// returning its result to the caller of scalanative_continuation_boundary.
//
// The reified computation is stored into memory allocated with `alloc_f(size,
// alloc_arg)`, the function set up by `scalanative_continuation_init`.
void *scalanative_continuation_suspend(ContinuationBoundaryLabel b,
                                       SuspendFn *f, void *arg,
                                       void *alloc_arg);

// resume[T, R] : Continuation[T, R] -> R -> Result
// Resumes the given Continuation under the resume call, passing back the
// argument into the suspended computation and returns its result.
void *scalanative_continuation_resume(Continuation *continuation, void *arg);

/* Exception type compatible with eh.c (void* = Scala object). */
typedef void *Exception;

/*
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
#endif // DELIMCC_H
