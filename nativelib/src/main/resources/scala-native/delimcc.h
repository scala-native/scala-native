#ifndef DELIMCC_H
#define DELIMCC_H
#include <stdint.h>

/* What the one-shot (delimcc_oneshot.h) and the multi-shot
 * (delimcc_multishot.h) continuation runtimes share. */

#ifdef __cplusplus
extern "C" {
#endif

typedef uintptr_t ContinuationBoundaryLabel;

typedef struct Continuation Continuation;

// ContinuationBody = BoundaryLabel -> any -> any
typedef void *ContinuationBody(ContinuationBoundaryLabel, void *);

// SuspendFn = Continuation -> any -> any
typedef void *SuspendFn(Continuation *, void *);

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

#ifdef __cplusplus
}
#endif
#endif // DELIMCC_H
