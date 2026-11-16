#ifndef IMMIX_COMMIX_OBJECT_TRACER_H
#define IMMIX_COMMIX_OBJECT_TRACER_H

#include "shared/GCHooks.h"

// The tracer registered through `scalanative_GC_trace_owned`, NULL until
// then. The markers call it for every object of a class flagged with
// RTTI_FLAG_HAS_OBJECT_TRACER.
extern _Atomic(GC_ObjectTracerFn *) ObjectTracer_trace;

#endif
