#if defined(SCALANATIVE_GC_COMMIX)

#include "WeakRefGreyList.h"
#include "immix_commix/headers/ObjectHeader.h"
#include "GCThread.h"
#include "SyncGreyLists.h"
#include <stdio.h>
#include <stdbool.h>

// WeakRefGreyList is a structure used for the gc_nullify phase.
// It collects WeakReference objects visited during marking phase.
// Later, during nullify phase, every WeakReference is checked if
// it is pointing to a marked object. If not, the referent field
// is set to NULL.
//
// Nullify phase is concurrent in the exact same way as the marking phase.
// Grey Packets are being distributed over different threads, until no
// more are available.

extern word_t *__modules;
bool anyVisited = false;
void (*handlerFn)() = NULL;

static inline GreyPacket *WeakRefGreyList_takeWeakRefPacket(Heap *heap,
                                                            Stats *stats) {
    return SyncGreyLists_takeNotEmptyPacket(
        heap, stats, &heap->mark.foundWeakRefs, nullify_waiting);
}

static void WeakRefGreyList_NullifyPacket(Heap *heap, Stats *stats,
                                          GreyPacket *weakRefsPacket) {
    Bytemap *bytemap = heap->bytemap;
    while (!GreyPacket_IsEmpty(weakRefsPacket)) {
        Object *object = GreyPacket_Pop(weakRefsPacket);
        assert(Object_IsWeakReference(object));

        word_t fieldOffset = __weak_ref_field_offset;
        word_t *refObject = object->fields[fieldOffset];
        if (Heap_IsWordInHeap(heap, refObject)) {
            ObjectMeta *objectMeta = Bytemap_Get(bytemap, refObject);
            if (ObjectMeta_IsAllocated(objectMeta)) {
                if (!ObjectMeta_IsMarked(objectMeta)) {
                    object->fields[fieldOffset] = NULL;
                    // idempotent operation - does not need to be synchronized
                    anyVisited = true;
                }
            }
        }
    }
}

void WeakRefGreyList_Nullify(Heap *heap, Stats *stats) {
    GreyPacket *weakRefsPacket = WeakRefGreyList_takeWeakRefPacket(heap, stats);

    while (weakRefsPacket != NULL) {
        WeakRefGreyList_NullifyPacket(heap, stats, weakRefsPacket);
        SyncGreyLists_giveEmptyPacket(heap, stats, weakRefsPacket);
        weakRefsPacket = WeakRefGreyList_takeWeakRefPacket(heap, stats);
    }
}

void WeakRefGreyList_NullifyAndScale(Heap *heap, Stats *stats) {
    GreyPacket *weakRefsPacket = WeakRefGreyList_takeWeakRefPacket(heap, stats);
    while (weakRefsPacket != NULL) {
        WeakRefGreyList_NullifyPacket(heap, stats, weakRefsPacket);

        GreyPacket *next = WeakRefGreyList_takeWeakRefPacket(heap, stats);
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

bool WeakRefGreyList_IsNullifyDone(Heap *heap) {
    return GreyList_Size(&heap->mark.empty) == heap->mark.total;
}

void WeakRefGreyList_NullifyUntilDone(Heap *heap, Stats *stats) {
    while (!WeakRefGreyList_IsNullifyDone(heap)) {
        WeakRefGreyList_Nullify(heap, stats);
        if (!WeakRefGreyList_IsNullifyDone(heap)) {
            thread_yield();
        }
    }
}

void WeakRefGreyList_SetHandler(void *handler) { handlerFn = handler; }

void WeakRefGreyList_CallHandlers() {
    if (anyVisited && handlerFn != NULL) {
        anyVisited = false;

        handlerFn();
    }
}

#endif
