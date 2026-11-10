#if defined(SCALANATIVE_GC_COMMIX)

#include "WeakReferences.h"
#include "immix_commix/headers/ObjectHeader.h"
#include "GCThread.h"
#include "SyncGreyLists.h"
#include "nativeThreadTLS.h"
#include "shared/ThreadUtil.h"
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <stdatomic.h>

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

// WeakReferences found while no grey packet was free, see Marker.c. Rare, so
// a spin lock is enough.
static struct {
    Object **items;
    size_t size;
    size_t capacity;
} overflow;
static atomic_flag overflowLock = ATOMIC_FLAG_INIT;

void WeakReferences_AddOverflow(Object *object) {
    while (atomic_flag_test_and_set_explicit(&overflowLock,
                                             memory_order_acquire)) {
        thread_yield();
    }
    if (overflow.size == overflow.capacity) {
        size_t capacity =
            overflow.capacity == 0 ? GREY_PACKET_ITEMS : overflow.capacity * 2;
        Object **items = realloc(overflow.items, capacity * sizeof(Object *));
        if (items == NULL) {
            Heap_exitWithOutOfMemory("for the weak reference overflow list");
        }
        overflow.items = items;
        overflow.capacity = capacity;
    }
    overflow.items[overflow.size++] = object;
    atomic_flag_clear_explicit(&overflowLock, memory_order_release);
}

bool WeakReferences_HasOverflow() { return overflow.size > 0; }

static void WeakReferences_nullifyObject(Heap *heap, Object *object) {
    assert(Object_IsWeakReference(object));

    Object **weakRefReferantField =
        (Object **)((int8_t *)object + __weak_ref_field_offset);
    word_t *weakRefReferant = (word_t *)*weakRefReferantField;
    if (Heap_IsWordInHeap(heap, weakRefReferant)) {
        ObjectMeta *objectMeta = Bytemap_Get(heap->bytemap, weakRefReferant);
        if (ObjectMeta_IsAllocated(objectMeta) &&
            !ObjectMeta_IsMarked(objectMeta)) {
            *weakRefReferantField = NULL;
            // idempotent operation - does not need to be synchronized
            collectedWeakReferences = true;
        }
    }
}

static void WeakReferences_NullifyPacket(Heap *heap, Stats *stats,
                                         GreyPacket *weakRefsPacket) {
    while (!GreyPacket_IsEmpty(weakRefsPacket)) {
        WeakReferences_nullifyObject(heap, GreyPacket_Pop(weakRefsPacket));
    }
}

// Called by a single thread after marking has finished.
void WeakReferences_NullifyOverflow(Heap *heap) {
    for (size_t i = 0; i < overflow.size; i++) {
        WeakReferences_nullifyObject(heap, overflow.items[i]);
    }
    free(overflow.items);
    overflow.items = NULL;
    overflow.size = 0;
    overflow.capacity = 0;
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
    // Only after a collection that cleared referents
    if (collectedWeakReferences && gcFinishedCallback != NULL &&
        currentThreadInfo.isInitialized) {
        collectedWeakReferences = false;
        gcFinishedCallback();
    }
}

#endif
