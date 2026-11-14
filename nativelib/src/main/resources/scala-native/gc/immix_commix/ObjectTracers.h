#ifndef IMMIX_COMMIX_OBJECT_TRACERS_H
#define IMMIX_COMMIX_OBJECT_TRACERS_H

#include "shared/GCHooks.h"

// The classes traced through `scalanative_GC_trace_owned`. The marker
// compares every object it visits with each of them, so there are only a few.
#define MAX_OBJECT_TRACERS 4

typedef struct ObjectTracer {
    void *rtti; // type descriptor (`object->rtti`) of the traced class
    GC_ObjectTracerFn *tracer;
} ObjectTracer;

// An entry is written before the count that publishes it.
extern ObjectTracer ObjectTracers_table[MAX_OBJECT_TRACERS];
extern atomic_size_t ObjectTracers_count;

// The tracer of the class with the type descriptor `rtti`; NULL for almost
// every object.
static inline GC_ObjectTracerFn *ObjectTracers_find(void *rtti) {
    size_t count =
        atomic_load_explicit(&ObjectTracers_count, memory_order_relaxed);
    for (size_t i = 0; i < count; i++) {
        if (ObjectTracers_table[i].rtti == rtti)
            return ObjectTracers_table[i].tracer;
    }
    return NULL;
}

#endif
