#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX)

#include "immix_commix/ObjectTracer.h"
#include <stdio.h>
#include <stdlib.h>

_Atomic(void *) ObjectTracer_rtti = NULL;
_Atomic(GC_ObjectTracerFn *) ObjectTracer_trace = NULL;

// These collectors know the class of the objects they mark: the owner
// itself is traced and no anchor is needed.
void **scalanative_GC_trace_owned(void *owner, GC_ObjectTracerFn *ownerTracer,
                                  GC_ObjectTracerFn *anchorTracer) {
    (void)anchorTracer;
    // The type descriptor is the first word of every object.
    void *rtti = *(void **)owner;
    void *traced = atomic_load(&ObjectTracer_rtti);
    if (traced == rtti)
        return NULL;
    // The tracer first: a marker that sees the type also sees the tracer.
    // Racing callers for the same class store the same values.
    atomic_store(&ObjectTracer_trace, ownerTracer);
    if (traced != NULL ||
        (!atomic_compare_exchange_strong(&ObjectTracer_rtti, &traced, rtti) &&
         traced != rtti)) {
        fprintf(stderr, "Only one class can be traced by the GC\n");
        abort();
    }
    return NULL;
}

#endif
