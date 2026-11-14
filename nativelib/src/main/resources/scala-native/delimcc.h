#ifndef DELIMCC_H
#define DELIMCC_H
#include <stdbool.h>
#include <stdlib.h>
#include <stdint.h>
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

typedef uintptr_t ContinuationBoundaryLabel;

typedef struct Continuation Continuation;

// ContinuationBody = BoundaryLabel -> any -> any
typedef void *ContinuationBody(ContinuationBoundaryLabel, void *);

// SuspendFn = Continuation -> any -> any
typedef void *SuspendFn(Continuation *, void *);

// Allocates GC-visible memory on behalf of `alloc_arg`, the value given to
// `suspend`.
typedef void *ContinuationAllocFn(unsigned long size, void *alloc_arg);

/* Both runtimes share one shape:
 *
 * - init: sets the allocation function for continuations and the stack data
 *   they keep.
 * - boundary: runs the body with a fresh label and returns its result, or the
 *   result of the SuspendFn of a `suspend` to that label.
 * - suspend: captures the computation up to the boundary of `label` as a
 *   Continuation, passes it to the SuspendFn, and returns that result from
 *   the boundary. Returns the resume argument once resumed.
 * - resume: continues a Continuation with `arg` and returns the result of
 *   the boundary it was captured from.
 */

/* One-shot (delimcc_oneshot.c): every boundary runs on its own stack and
 * frames never move. Here a Continuation is opaque, needs no allocation
 * function, and must be resumed at most once. `boundary` returns NULL when
 * no stack can be allocated. Its SuspendFn calls `captured`
 * with the SuspendedStacks object that points to it. */
void *scalanative_continuation_oneshot_boundary(ContinuationBody *, void *);
void *scalanative_continuation_oneshot_suspend(ContinuationBoundaryLabel label,
                                               SuspendFn *f, void *arg);
// Returns `result` from the boundary of `label`; does not return.
void scalanative_continuation_oneshot_leave(ContinuationBoundaryLabel label,
                                            void *result);
void scalanative_continuation_oneshot_captured(Continuation *continuation,
                                               void *owner);
// `owner` is the SuspendedStacks object given to `captured`.
void *scalanative_continuation_oneshot_resume(void *owner, void *arg);
void scalanative_continuation_oneshot_discard(Continuation *continuation);
void scalanative_continuation_oneshot_watch_multishot(void *probe);
bool scalanative_continuation_oneshot_crosses_multishot(
    ContinuationBoundaryLabel label);

// For code compiled against 0.5.12 and older, which has `boundary` and
// `suspend` inlined; they run on the one-shot runtime.
void *scalanative_continuation_boundary(ContinuationBody *, void *);
void *scalanative_continuation_suspend(ContinuationBoundaryLabel label,
                                       SuspendFn *f, void *arg,
                                       void *alloc_arg);

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
#endif // DELIMCC_H
