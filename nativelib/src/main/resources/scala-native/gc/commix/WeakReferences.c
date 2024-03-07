#if defined(SCALANATIVE_GC_COMMIX)

#include "WeakReferences.h"
#include "immix_commix/headers/ObjectHeader.h"
#include "GCThread.h"
#include "SyncGreyLists.h"
#include <stdio.h>
#include <stdbool.h>

// WeakReferences is a structure used for the gc_nullify phase.
// It collects WeakReference objects visited during marking phase.
// Later, during nullify phase, every WeakReference is checked if
// it is pointing to a marked object. If not, the referent field
// is set to NULL.
//
// Nullify phase is concurrent in the exact same way as the marking phase.
// Grey Packets are being distributed over different threads, until no
// more are available.

bool collectedWeakReferences = false;
void (*gcFinishedCallback)() = NULL;

static inline GreyPacket *WeakReferences_takeWeakRefPacket(Heap *heap,
                                                           Stats *stats) {
    return SyncGreyLists_takeNotEmptyPacket(
        heap, stats, &heap->mark.foundWeakRefs, nullify_waiting);
}

static void WeakReferences_NullifyPacket(Heap *heap, Stats *stats,
                                         GreyPacket *weakRefsPacket) {
    Bytemap *bytemap = heap->bytemap;
    while (!GreyPacket_IsEmpty(weakRefsPacket)) {
        Object *object = GreyPacket_Pop(weakRefsPacket);
        assert(Object_IsWeakReference(object));

        Object **weakRefReferantField =
            (Object **)((int8_t *)object + __weak_ref_field_offset);
        word_t *weakRefReferant = (word_t *)*weakRefReferantField;
        if (Heap_IsWordInHeap(heap, weakRefReferant)) {
            ObjectMeta *objectMeta = Bytemap_Get(bytemap, weakRefReferant);
            if (ObjectMeta_IsAllocated(objectMeta) &&
                !ObjectMeta_IsMarked(objectMeta)) {
                *weakRefReferantField = NULL;
                // idempotent operation - does not need to be synchronized
                collectedWeakReferences = true;
            }
        }
    }
}

void WeakReferences_Nullify(Heap *heap, Stats *stats) {
    GreyPacket *weakRefsPacket = WeakReferences_takeWeakRefPacket(heap, stats);

    while (weakRefsPacket != NULL) {
        WeakReferences_NullifyPacket(heap, stats, weakRefsPacket);
        SyncGreyLists_giveEmptyPacket(heap, stats, weakRefsPacket);
        weakRefsPacket = WeakReferences_takeWeakRefPacket(heap, stats);
    }
}

void WeakReferences_NullifyAndScale(Heap *heap, Stats *stats) {
    GreyPacket *weakRefsPacket = WeakReferences_takeWeakRefPacket(heap, stats);
    while (weakRefsPacket != NULL) {
        WeakReferences_NullifyPacket(heap, stats, weakRefsPacket);

        GreyPacket *next = WeakReferences_takeWeakRefPacket(heap, stats);
        SyncGreyLists_giveEmptyPacket(heap, stats, weakRefsPacket);
        if (next != NULL) {
            uint32_t remainingPackets = UInt24_toUInt32(next->next.sep.size);
            // Similarly to Marker_MarkAndScale, we add new worker threads
            // when enough packets are available, otherwise we risk additional
            // unnecessary overhead.
            GCThread_ScaleMarkerThreads(heap, remainingPackets);
        }
        weakRefsPacket = next;
    }
}

bool WeakReferences_IsNullifyDone(Heap *heap) {
    return GreyList_Size(&heap->mark.empty) == heap->mark.total;
}

void WeakReferences_NullifyUntilDone(Heap *heap, Stats *stats) {
    while (!WeakReferences_IsNullifyDone(heap)) {
        WeakReferences_Nullify(heap, stats);
        if (!WeakReferences_IsNullifyDone(heap)) {
            thread_yield();
        }
    }
}

void WeakReferences_SetGCFinishedCallback(void *handler) {
    gcFinishedCallback = handler;
}

void WeakReferences_InvokeGCFinishedCallback() {
    if (collectedWeakReferences && gcFinishedCallback != NULL) {
        gcFinishedCallback();
    }
}

#endif
