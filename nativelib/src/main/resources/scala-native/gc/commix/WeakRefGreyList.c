#include "WeakRefGreyList.h"
#include "headers/ObjectHeader.h"

void WeakRefGreyList_GiveWeakRefPacket(Heap *heap, Stats *stats, // Rethink
                                       GreyPacket *packet) {
    assert(packet->type == grey_packet_refrange || packet->size > 0); // TODO
    // make all the contents visible to other threads
    atomic_thread_fence(memory_order_acquire);
    uint32_t greyListSize = GreyList_Size(&heap->mark.foundWeakRefs);
    assert(greyListSize <= heap->mark.total);
    Stats_RecordTimeSync(stats, start_ns);
    GreyList_Push(&heap->mark.foundWeakRefs, heap->greyPacketsStart, packet);
    Stats_RecordTimeSync(stats, end_ns);
    Stats_RecordEventSync(stats, event_sync, start_ns, end_ns);
}

void WeakRefGreyList_Visit(Heap *heap) {
    GreyPacket *weakRefsPacket =
        GreyList_Pop(&heap->mark.foundWeakRefs, heap->greyPacketsStart);
    Bytemap *bytemap = heap->bytemap;
    while (weakRefsPacket != NULL) {
        while (!GreyPacket_IsEmpty(weakRefsPacket)) {
            Object *object = GreyPacket_Pop(weakRefsPacket);
            word_t objOffset =
                object->rtti->refMapStruct[__weak_ref_field_offset];
            word_t *refObject = object->fields[objOffset];

            if (Heap_IsWordInHeap(heap, refObject)) {
                ObjectMeta *objectMeta = Bytemap_Get(bytemap, refObject);
                if (ObjectMeta_IsAllocated(objectMeta)) {
                    if (!ObjectMeta_IsMarked(objectMeta)) {
                        // WeakReferences should have the referant field set to
                        // null on collect
                        object->fields[objOffset] = NULL;
                    }
                }
            }
        }
        GreyList_Push(&heap->mark.empty, heap->greyPacketsStart,
                      weakRefsPacket);
        weakRefsPacket =
            GreyList_Pop(&heap->mark.foundWeakRefs, heap->greyPacketsStart);
    }
}
