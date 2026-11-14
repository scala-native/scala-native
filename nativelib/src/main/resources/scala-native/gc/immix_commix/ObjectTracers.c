#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX)

#include "immix_commix/ObjectTracers.h"
#include <stdio.h>
#include <stdlib.h>

ObjectTracer ObjectTracers_table[MAX_OBJECT_TRACERS];
atomic_size_t ObjectTracers_count = 0;
static atomic_flag lock = ATOMIC_FLAG_INIT;

// These collectors know the class of the objects they mark: the owner
// itself is traced and no anchor is needed.
void **scalanative_GC_trace_owned(void *owner, GC_ObjectTracerFn *ownerTracer,
                                  GC_ObjectTracerFn *anchorTracer) {
    (void)anchorTracer;
    // The type descriptor is the first word of every object.
    void *rtti = *(void **)owner;
    if (ObjectTracers_find(rtti) != NULL)
        return NULL;
    while (atomic_flag_test_and_set(&lock)) {
    }
    if (ObjectTracers_find(rtti) == NULL) {
        size_t count = atomic_load(&ObjectTracers_count);
        if (count == MAX_OBJECT_TRACERS) {
            fprintf(stderr, "Too many classes traced by the GC\n");
            abort();
        }
        ObjectTracers_table[count] = (ObjectTracer){rtti, ownerTracer};
        atomic_store(&ObjectTracers_count, count + 1);
    }
    atomic_flag_clear(&lock);
    return NULL;
}

#endif
