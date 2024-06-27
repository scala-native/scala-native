#ifndef DELIMCC_H
#define DELIMCC_H

typedef unsigned long ContinuationBoundaryLabel;

typedef struct Continuation Continuation;

// ContinationBody = ContBoundaryLabel -> any -> any
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

#ifdef SCALANATIVE_DELIMCC_DEBUG // Debug flag for delimcc

// Frees a continuation. Used only if malloc is used as the implementation of
// alloc function.
void scalanative_continuation_free(Continuation *continuation);

#endif // SCALANATIVE_DELIMCC_DEBUG
#endif // DELIMCC_H
