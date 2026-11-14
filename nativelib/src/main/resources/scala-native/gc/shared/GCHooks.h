#ifndef GC_HOOKS_H
#define GC_HOOKS_H

#include <stdatomic.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>

/* How runtime code tells the collector about memory it cannot find on its
 * own. Internal to the runtime; a collector that does not implement it does
 * not support its users (one-shot continuations, delimcc_oneshot.c).
 *
 * Besides the callbacks below, code that makes a thread run on another stack
 * keeps the stack bounds in its ThreadInfo (nativeThreadTLS.h) those of the
 * stack that runs: the collector scans a stopped thread from its stack
 * pointer to that bottom, see `threadStackScanBottom`.
 *
 * Every callback runs while a collection is in progress: it must not
 * allocate, block, or reach a safepoint. What it reads must only change
 * while no collection runs.
 */

// Scans [low, high) conservatively: any word may be a reference. Both ends
// are word aligned and the whole range is readable.
typedef void GC_ScanRangeFn(void *ctx, void *low, void *high);

/* Dynamic roots: memory that is alive whatever the heap says, whose bounds
 * change too often to register each change with `scalanative_GC_add_roots`.
 * The collector calls every provider once per collection, with all mutator
 * threads stopped. */
typedef void GC_RootProviderFn(void *data, GC_ScanRangeFn *scan, void *ctx);

// Providers are never removed. Returns false when no more can be added.
bool scalanative_GC_add_root_provider(GC_RootProviderFn *provider, void *data);

/* Object tracer: memory owned by an object, alive only while the object is.
 * The collector calls the tracer when it marks an instance of the traced
 * traced class, with `fields` pointing to its first field. Several marker
 * threads may call it at once, also for the same object class.
 *
 * Only instances of exactly that class are traced, not of its subclasses.
 * The marker compares the class of every object it marks with each traced
 * one, so only a few can be. */
typedef void GC_ObjectTracerFn(void **fields, GC_ScanRangeFn *scan, void *ctx);

// Traces the instances of the class of `object`. Tracers are never removed.
// Returns false when no more classes can be traced, or when the collector
// does not know the class of the objects it marks: use a traced anchor then.
bool scalanative_GC_add_object_tracer(void *object, GC_ObjectTracerFn *tracer);

/* Traced anchor: for a collector that cannot trace by class (Boehm). A
 * collected object of two words, both zero, to keep reachable from the owner
 * of the memory. When the collector marks an anchor whose second word is not
 * zero, it calls `tracer` with the two words as `fields`.
 *
 * Such a collector is conservative: it can mark an anchor that became
 * unreachable earlier. The tracer has to tell from the two words alone
 * whether what they describe still exists. Free anchors have a zero second
 * word.
 *
 * Returns NULL when the collector traces by class instead, or when another
 * tracer already has anchors. */
void **scalanative_GC_alloc_traced_anchor(GC_ObjectTracerFn *tracer);

/* Mark cycles, numbered from 1. A tracer that records the cycle it ran in
 * lets its owner tell, after that cycle completed, which objects were not
 * reached. */
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

/* A collector that stops threads at any instruction, not only at safepoints
 * (Boehm), can observe every intermediate state of the code that uses these
 * hooks. Such code pauses collections around each change of what its
 * callbacks read, and around a switch to another stack, where it also names
 * the stack the thread now runs on. With the other collectors these calls
 * compile to nothing. */
#ifdef SCALANATIVE_GC_BOEHM
// No collection starts between these two calls. They nest, across threads.
void scalanative_GC_pause_collections(void);
void scalanative_GC_resume_collections(void);
// The bottom of the stack the calling thread runs on, as the collector knows
// it. Set with collections paused.
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

#define GC_MAX_OBJECT_TRACERS 4

typedef struct GC_ObjectTracer {
    void *rtti; // type descriptor (`object->rtti`) of the traced class
    GC_ObjectTracerFn *tracer;
} GC_ObjectTracer;

// An entry is written before the count that publishes it.
extern GC_ObjectTracer scalanative_GC_objectTracers[GC_MAX_OBJECT_TRACERS];
extern atomic_size_t scalanative_GC_objectTracersCount;

// The tracer of the class with the type descriptor `rtti`, NULL for almost
// every object: called for each one the marker visits.
static inline GC_ObjectTracerFn *GCHooks_objectTracer(void *rtti) {
    size_t count = atomic_load_explicit(&scalanative_GC_objectTracersCount,
                                        memory_order_relaxed);
    for (size_t i = 0; i < count; i++) {
        if (scalanative_GC_objectTracers[i].rtti == rtti)
            return scalanative_GC_objectTracers[i].tracer;
    }
    return NULL;
}

// Around the marking of one collection.
static inline void GCHooks_markStarted(void) {
    atomic_fetch_add(&scalanative_GC_markCycle, 1);
}

static inline void GCHooks_markCompleted(void) {
    atomic_store(&scalanative_GC_completedMarkCycle,
                 atomic_load(&scalanative_GC_markCycle));
}

// With all mutator threads stopped.
void GCHooks_scanDynamicRoots(GC_ScanRangeFn *scan, void *ctx);

#endif
