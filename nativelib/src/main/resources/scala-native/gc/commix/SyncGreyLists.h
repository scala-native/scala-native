#ifndef SYNC_GREY_LISTS_H
#define SYNC_GREY_LISTS_H

#include "Heap.h"
#include "Stats.h"
#include "datastructures/GreyPacket.h"

void SyncGreyLists_giveNotEmptyPacket(Heap *heap, Stats *stats,
                                      GreyList *greyList, GreyPacket *packet);
GreyPacket *SyncGreyLists_takeNotEmptyPacket(Heap *heap, Stats *stats,
                                             GreyList *greyList,
                                             eventType waitingEventType);
void SyncGreyLists_giveEmptyPacket(Heap *heap, Stats *stats,
                                   GreyPacket *packet);
GreyPacket *SyncGreyLists_takeEmptyPacket(Heap *heap, Stats *stats);

#endif // SYNC_GREY_LISTS_H
