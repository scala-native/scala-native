#include "WeakRefGreyList.h"
#include "headers/ObjectHeader.h"
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
// Grey Packets are being spread over different threads.

extern word_t *__modules;
int anyVisited = false;

static inline GreyPacket *WeakRefGreyList_takeWeakRefPacket(Heap *heap,
                                                            Stats *stats) {
    return SyncGreyLists_takeNotEmptyPacket(heap, stats,
                                            &heap->mark.foundWeakRefs);
}

static void WeakRefGreyList_NullifyPacket(Heap *heap, Stats *stats,
                                          GreyPacket *weakRefsPacket) {
    Bytemap *bytemap = heap->bytemap;
    while (!GreyPacket_IsEmpty(weakRefsPacket)) {
        Object *object = GreyPacket_Pop(weakRefsPacket);

        word_t objOffset = object->rtti->refMapStruct[__weak_ref_field_offset];
        word_t *refObject = object->fields[objOffset];
        if (Heap_IsWordInHeap(heap, refObject)) {
            ObjectMeta *objectMeta = Bytemap_Get(bytemap, refObject);
            if (ObjectMeta_IsAllocated(objectMeta)) {
                if (!ObjectMeta_IsMarked(objectMeta)) {
                    object->fields[objOffset] = NULL;
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

        assert(GreyPacket_IsEmpty(weakRefsPacket));
        GreyPacket *next = WeakRefGreyList_takeWeakRefPacket(heap, stats);
        if (next != NULL) {
            SyncGreyLists_giveEmptyPacket(heap, stats, weakRefsPacket);
            uint32_t remainingFullPackets =
                UInt24_toUInt32(next->next.sep.size);
            // Make sure than enough worker threads are running
            // given the number of packets available.
            // They will automatically stop if they run out of full packets.
            // If too many threads are started only a fraction of them would
            // get a packet and do useful work. Others would add unnecessary
            // overhead by checking the list of full packets.
            GCThread_ScaleMarkerThreads(heap, remainingFullPackets);
        } else {
            SyncGreyLists_giveEmptyPacket(heap, stats, weakRefsPacket);
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

void WeakRefGreyList_CallHandlers() {
    if (anyVisited && __weak_ref_registry_module_offset != -1 &&
        __weak_ref_registry_field_offset != -1) {
        word_t **modules = &__modules;
        Object *registry = (Object *)modules[__weak_ref_registry_module_offset];
        word_t *field = registry->fields[__weak_ref_registry_field_offset];
        void (*fieldOffset)() = (void *)field;

        fieldOffset();
    }
    anyVisited = false;
}
