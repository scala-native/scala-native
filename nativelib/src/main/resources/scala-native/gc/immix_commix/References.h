#ifndef IMMIX_COMMIX_REFERENCES_H
#define IMMIX_COMMIX_REFERENCES_H

// State of java.lang.ref.Reference processing shared by Immix and Commix.
//
// The marker never marks the referent of a weak or phantom reference. It
// marks the referent of a soft reference like any other field, unless the
// collection clears soft references: the last one before an allocation fails
// with an OutOfMemoryError. References whose referent is not marked are
// collected while marking, and once it is done, the referents that nothing
// else marked are cleared.

#include <stdbool.h>
#include <stdatomic.h>
#include "immix_commix/headers/ObjectHeader.h"

// Set only while the world is stopped, before marking starts.
extern bool References_clearSoft;
// Whether the last collection marked a soft reference, so that one clearing
// soft references could free memory.
extern atomic_bool References_softFound;

// Whether the marker leaves the referent of an object of this reference kind
// for References to clear.
static inline bool References_deferReferent(int32_t kind) {
    return kind != 0 &&
           (kind != RTTI_REFERENCE_KIND_SOFT || References_clearSoft);
}

static inline void References_noteSoft(void) {
    // Many marker threads may find soft references: store only once.
    if (!atomic_load_explicit(&References_softFound, memory_order_relaxed))
        atomic_store_explicit(&References_softFound, true,
                              memory_order_relaxed);
}

// Called with the world stopped before marking.
static inline void References_StartCollection(bool clearSoft) {
    References_clearSoft = clearSoft;
    atomic_store_explicit(&References_softFound, false, memory_order_relaxed);
}

// Called with the world stopped once referents were cleared.
static inline void References_EndCollection(void) {
    References_clearSoft = false;
}

// Whether a collection clearing soft references may still free memory for an
// allocation that failed after a collection with `clearedSoft`.
static inline bool References_ShouldRetryClearingSoft(bool clearedSoft) {
    return !clearedSoft &&
           atomic_load_explicit(&References_softFound, memory_order_relaxed);
}

#endif // IMMIX_COMMIX_REFERENCES_H
