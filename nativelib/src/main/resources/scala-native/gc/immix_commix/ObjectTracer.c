#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX)

#include "immix_commix/ObjectTracer.h"
#include "immix_commix/headers/ObjectHeader.h"
#include <stdio.h>
#include <stdlib.h>

// Zero-initialized; clang 16 rejects NULL as the initializer of an atomic
// function pointer.
_Atomic(GC_ObjectTracerFn *) ObjectTracer_trace;

// These collectors know the class of the objects they mark: the owner
// itself is traced and no anchor is needed.
void **scalanative_GC_trace_owned(void *owner, GC_ObjectTracerFn *ownerTracer,
                                  GC_ObjectTracerFn *anchorTracer) {
    (void)anchorTracer;
    // The markers recognize traced objects by a flag of their class, set by
    // the toolchain.
    if (!Rtti_HasFlag(((Object *)owner)->rtti, RTTI_FLAG_HAS_OBJECT_TRACER)) {
        fprintf(stderr, "The class of the owner is not traced by the GC\n");
        abort();
    }
    // Callers racing to register store the same tracer.
    GC_ObjectTracerFn *current = NULL;
    if (!atomic_compare_exchange_strong(&ObjectTracer_trace, &current,
                                        ownerTracer) &&
        current != ownerTracer) {
        fprintf(stderr, "Only one object tracer is supported\n");
        abort();
    }
    return NULL;
}

#endif
