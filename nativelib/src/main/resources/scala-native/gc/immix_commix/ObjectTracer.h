#ifndef IMMIX_COMMIX_OBJECT_TRACER_H
#define IMMIX_COMMIX_OBJECT_TRACER_H

#include "shared/GCHooks.h"

// The one class traced through `scalanative_GC_trace_owned`: its type
// descriptor (`object->rtti`) and tracer, NULL until set. The marker compares
// every object it visits with it.
extern _Atomic(void *) ObjectTracer_rtti;
extern _Atomic(GC_ObjectTracerFn *) ObjectTracer_trace;

#endif
