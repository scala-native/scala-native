#ifndef DELIMCC_ONESHOT_H
#define DELIMCC_ONESHOT_H
#include "delimcc.h"
#include <stdbool.h>
#include <stdlib.h>

#ifdef __cplusplus
extern "C" {
#endif

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

#ifdef __cplusplus
}
#endif
#endif // DELIMCC_ONESHOT_H
