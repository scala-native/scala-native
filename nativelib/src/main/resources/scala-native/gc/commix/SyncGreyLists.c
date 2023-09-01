#if defined(SCALANATIVE_GC_COMMIX)

#include "SyncGreyLists.h"

void SyncGreyLists_giveNotEmptyPacket(Heap *heap, Stats *stats,
                                      GreyList *greyList, GreyPacket *packet) {
    // make all the contents visible to other threads
    atomic_thread_fence(memory_order_acquire);
    uint32_t greyListSize = GreyList_Size(greyList);
    assert(greyListSize <= heap->mark.total);
    Stats_RecordTimeSync(stats, start_ns);
    GreyList_Push(greyList, heap->greyPacketsStart, packet);
    Stats_RecordTimeSync(stats, end_ns);
    Stats_RecordEventSync(stats, event_sync, start_ns, end_ns);
}

GreyPacket *SyncGreyLists_takeNotEmptyPacket(Heap *heap, Stats *stats,
                                             GreyList *greyList,
                                             eventType waitingEventType) {
    Stats_RecordTimeSync(stats, start_ns);
    GreyPacket *packet = GreyList_Pop(greyList, heap->greyPacketsStart);
    if (packet != NULL) {
        atomic_thread_fence(memory_order_release);
    }
    Stats_RecordTimeSync(stats, end_ns);
    Stats_RecordEventSync(stats, event_sync, stats->packet_waiting_start_ns,
                          end_ns);
    if (packet == NULL) {
        Stats_NoNotEmptyPacket(stats, start_ns, end_ns);
    } else {
        Stats_GotNotEmptyPacket(stats, end_ns, waitingEventType);
    }
    return packet;
}

void SyncGreyLists_giveEmptyPacket(Heap *heap, Stats *stats,
                                   GreyPacket *packet) {
    assert(packet->size == 0);
    // no memfence needed see SyncGreyLists_takeEmptyPacket
    Stats_RecordTimeSync(stats, start_ns);
    GreyList_Push(&heap->mark.empty, heap->greyPacketsStart, packet);
    Stats_RecordTimeSync(stats, end_ns);
    Stats_RecordEventSync(stats, event_sync, start_ns, end_ns);
}

GreyPacket *SyncGreyLists_takeEmptyPacket(Heap *heap, Stats *stats) {
    Stats_RecordTimeSync(stats, start_ns);
    GreyPacket *packet =
        GreyList_Pop(&heap->mark.empty, heap->greyPacketsStart);
    Stats_RecordTimeSync(stats, end_ns);
    Stats_RecordEventSync(stats, event_sync, start_ns, end_ns);
    if (packet != NULL) {
        // Another thread setting size = 0 might not have arrived, just write it
        // now. Avoiding a memfence.
        packet->size = 0;
        packet->type = grey_packet_reflist;
    }
    assert(packet != NULL);
    return packet;
}

#endif
