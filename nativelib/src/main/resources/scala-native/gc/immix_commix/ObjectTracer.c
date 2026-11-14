#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX)

#include "immix_commix/ObjectTracer.h"
#include <stdio.h>
#include <stdlib.h>

void *ObjectTracer_rtti = NULL;
GC_ObjectTracerFn *ObjectTracer_trace = NULL;

// These collectors know the class of the objects they mark: the owner
// itself is traced and no anchor is needed.
void **scalanative_GC_trace_owned(void *owner, GC_ObjectTracerFn *ownerTracer,
                                  GC_ObjectTracerFn *anchorTracer) {
    (void)anchorTracer;
    // The type descriptor is the first word of every object.
    void *rtti = *(void **)owner;
    if (ObjectTracer_rtti == rtti)
        return NULL;
    if (ObjectTracer_rtti != NULL) {
        fprintf(stderr, "Only one class can be traced by the GC\n");
        abort();
    }
    // The tracer first: a marker that sees the type also sees the tracer.
    // Racing callers store the same values.
    ObjectTracer_trace = ownerTracer;
    ObjectTracer_rtti = rtti;
    return NULL;
}

#endif
