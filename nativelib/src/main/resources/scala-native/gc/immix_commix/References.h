#ifndef IMMIX_COMMIX_REFERENCES_H
#define IMMIX_COMMIX_REFERENCES_H

// java.lang.ref.Reference handling shared by Immix and Commix. Weak and
// phantom referents are never marked through the reference; soft ones are,
// except in the collection retried before an OutOfMemoryError. Unmarked
// referents are cleared after marking.

#include <stdbool.h>
#include <stdatomic.h>
#include <stdint.h>
#include "immix_commix/headers/ObjectHeader.h"

// Written only while the world is stopped
extern bool References_clearSoft;
// The last collection found a soft reference
extern atomic_bool References_softFound;
// Number of collections that cleared soft referents
extern atomic_uint_fast64_t References_softClearings;

// The referent is left unmarked, to be cleared if nothing else marks it
static inline bool References_deferReferent(int32_t kind) {
    return kind != 0 &&
           (kind != RTTI_REFERENCE_KIND_SOFT || References_clearSoft);
}

// Soft referents are marked unless soft references are being cleared
static inline bool References_markSoftReferent(int32_t kind) {
    return kind == RTTI_REFERENCE_KIND_SOFT && !References_clearSoft;
}

// The referent of a soft reference, noting that clearing it could free memory
static inline Field_t References_softReferent(const Object *object) {
    Field_t referent = *Reference_ReferentField(object);
    // Store once, many marker threads may get here
    if (referent != NULL &&
        !atomic_load_explicit(&References_softFound, memory_order_relaxed))
        atomic_store_explicit(&References_softFound, true,
                              memory_order_relaxed);
    return referent;
}

static inline bool References_SoftFound(void) {
    return atomic_load_explicit(&References_softFound, memory_order_relaxed);
}

static inline uint64_t References_SoftClearings(void) {
    return atomic_load_explicit(&References_softClearings,
                                memory_order_acquire);
}

static inline void References_StartCollection(bool clearSoft) {
    References_clearSoft = clearSoft;
    if (clearSoft)
        atomic_fetch_add_explicit(&References_softClearings, 1,
                                  memory_order_release);
    atomic_store_explicit(&References_softFound, false, memory_order_relaxed);
}

// After a failed allocation: true to retry it once, with soft references
// cleared, false to throw an OutOfMemoryError
static inline bool References_RetryClearingSoft(bool *clearSoft) {
    if (*clearSoft || !References_SoftFound())
        return false;
    *clearSoft = true;
    return true;
}

#endif // IMMIX_COMMIX_REFERENCES_H
