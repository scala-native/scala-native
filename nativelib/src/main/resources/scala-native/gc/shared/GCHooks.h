#ifndef GC_HOOKS_H
#define GC_HOOKS_H

#include <stdatomic.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>

/* Internal interface for runtime code that owns memory the collector cannot
 * find on its own (one-shot continuations, delimcc_oneshot.c).
 *
 * Callbacks run during a collection: they must not allocate, block or reach
 * a safepoint, and what they read may only change while no collection runs.
 *
 * Code that runs a thread on another stack keeps the stack bounds in its
 * ThreadInfo those of the running stack, see `threadStackScanBottom`.
 */

// Scans the readable, word aligned [low, high) conservatively.
typedef void GC_ScanRangeFn(void *ctx, void *low, void *high);

/* Dynamic roots: memory that is always alive and whose bounds change too
 * often for `scalanative_GC_add_roots`. A provider is called once per
 * collection, with all mutator threads stopped, and is never removed. */
typedef void GC_RootProviderFn(void *data, GC_ScanRangeFn *scan, void *ctx);

// Returns false when no more providers can be added.
bool scalanative_GC_add_root_provider(GC_RootProviderFn *provider, void *data);

/* Object tracer: scans memory that is alive only while its owner object is.
 * Called when the collector marks the owner, possibly by several marker
 * threads at once. */
typedef void GC_ObjectTracerFn(void **fields, GC_ScanRangeFn *scan, void *ctx);

/* Has the memory owned by `owner` traced, in the way the collector supports:
 *
 * - Returns NULL: `ownerTracer` runs for every marked instance of the exact
 *   class of `owner`, with `fields` at its first field.
 * - Returns an anchor: two zero words to fill in and keep reachable from
 *   `owner`. `anchorTracer` runs when the anchor is marked and its second
 *   word is not zero, with `fields` at these words. Such a collector is
 *   conservative and can mark an anchor that died earlier: the tracer has
 *   to tell from the two words whether what they describe still exists.
 *
 * Aborts when it can do neither. */
void **scalanative_GC_trace_owned(void *owner, GC_ObjectTracerFn *ownerTracer,
                                  GC_ObjectTracerFn *anchorTracer);

/* Mark cycles, numbered from 1. A tracer that records the cycle it ran in
 * lets its owner tell which objects a completed cycle did not reach. */
extern atomic_uint_fast64_t scalanative_GC_markCycle;
extern atomic_uint_fast64_t scalanative_GC_completedMarkCycle;

// The cycle being marked, or the last one that was.
static inline uint_fast64_t scalanative_GC_mark_cycle(void) {
    return atomic_load(&scalanative_GC_markCycle);
}

// The last cycle whose marking completed.
static inline uint_fast64_t scalanative_GC_completed_mark_cycle(void) {
    return atomic_load(&scalanative_GC_completedMarkCycle);
}

/* A collector that stops threads at any instruction, not only at
 * safepoints, sees every intermediate state. Its users pause collections
 * around each change of what the callbacks read and around a switch to
 * another stack, where they also name the stack that now runs. */
#ifdef SCALANATIVE_GC_STOPS_THREADS_ANYWHERE
// These nest, across threads.
void scalanative_GC_pause_collections(void);
void scalanative_GC_resume_collections(void);
// The bottom of the stack the calling thread runs on. Set while paused.
void *scalanative_GC_current_stack_bottom(void);
void scalanative_GC_set_current_stack_bottom(void *bottom);
#else
static inline void scalanative_GC_pause_collections(void) {}
static inline void scalanative_GC_resume_collections(void) {}
static inline void *scalanative_GC_current_stack_bottom(void) { return NULL; }
static inline void scalanative_GC_set_current_stack_bottom(void *bottom) {
    (void)bottom;
}
#endif

// =============================
// For collectors

// Around the marking of one collection.
static inline void GCHooks_markStarted(void) {
    atomic_fetch_add(&scalanative_GC_markCycle, 1);
}

static inline void GCHooks_markCompleted(void) {
    atomic_store(&scalanative_GC_completedMarkCycle,
                 atomic_load(&scalanative_GC_markCycle));
}

// Calls the root providers; with all mutator threads stopped.
void GCHooks_scanDynamicRoots(GC_ScanRangeFn *scan, void *ctx);

#endif
