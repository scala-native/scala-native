
#if defined(SCALANATIVE_GC_COMMIX)

#include "commix/Object.h"
#include "GreyPacket.h"
#include "immix_commix/Log.h"
#include <string.h>

bool GreyPacket_Push(GreyPacket *packet, Stack_Type value) {
    assert(value != NULL);
    if (packet->size >= GREY_PACKET_ITEMS) {
        return false;
    } else {
        packet->items[packet->size++] = value;
        return true;
    }
}
Stack_Type GreyPacket_Pop(GreyPacket *packet) {
    assert(packet->size > 0);
    return packet->items[--packet->size];
}

void GreyPacket_MoveItems(GreyPacket *src, GreyPacket *dst, int count) {
    assert(dst->size + count < GREY_PACKET_ITEMS);
    assert(src->size >= count);
    void *target = (void *)&dst->items[dst->size];
    void *first = (void *)&src->items[src->size - count];
    memcpy(target, first, count * sizeof(Stack_Type));
    dst->size += count;
    src->size -= count;
}

bool GreyPacket_IsEmpty(GreyPacket *packet) { return packet->size == 0; }

void GreyList_Init(GreyList *list) {
    assert(sizeof(GreyPacketRef) == sizeof(uint64_t));
    list->head.atom = GreyPacketRef_Empty();
}

uint32_t GreyList_Size(GreyList *list) {
    GreyPacketRef head;
    head.atom = list->head.atom;
    return UInt24_toUInt32(head.sep.size);
}

void GreyList_Push(GreyList *list, word_t *greyPacketsStart,
                   GreyPacket *packet) {
    UInt24 packetIdx = GreyPacket_IndexOf(greyPacketsStart, packet);
    GreyPacketRef newHead;
    newHead.sep.idx = packetIdx;
    newHead.sep.timesPoped = (uint16_t)packet->timesPoped;
    GreyPacketRef head;
    head.atom = list->head.atom;
    do {
        // head will be replaced with actual value if
        // atomic_compare_exchange_strong fails
        newHead.sep.size = UInt24_plus(head.sep.size, 1);
        UInt24 nextIdx = head.sep.idx;
        if (UInt24_equals(nextIdx, GREYLIST_LAST)) {
            packet->next.atom = GreyPacketRef_Empty();
        } else {
            packet->next.atom = head.atom;
        }
    } while (!atomic_compare_exchange_strong(
        &list->head.atom, (uint64_t *)&head.atom, newHead.atom));
}

void GreyList_PushAll(GreyList *list, word_t *greyPacketsStart,
                      GreyPacket *first, uint_fast32_t size) {
    UInt24 packetIdx = GreyPacket_IndexOf(greyPacketsStart, first);
    GreyPacketRef newHead;
    newHead.sep.idx = packetIdx;
    newHead.sep.timesPoped = (uint16_t)first->timesPoped;
    GreyPacket *last = first + (size - 1);
    GreyPacketRef head;
    head.atom = list->head.atom;
    do {
        // head will be replaced with actual value if
        // atomic_compare_exchange_strong fails
        newHead.sep.size = UInt24_plus(head.sep.size, size);
        UInt24 nextIdx = head.sep.idx;
        if (UInt24_equals(nextIdx, GREYLIST_LAST)) {
            last->next.atom = GreyPacketRef_Empty();
        } else {
            last->next.atom = head.atom;
        }
    } while (!atomic_compare_exchange_strong(
        &list->head.atom, (uint64_t *)&head.atom, newHead.atom));
}

GreyPacket *GreyList_Pop(GreyList *list, word_t *greyPacketsStart) {
    GreyPacketRef head;
    head.atom = list->head.atom;
    GreyPacketRef nextValue;
    UInt24 headIdx;
    GreyPacket *res;
    do {
        // head will be replaced with actual value if
        // atomic_compare_exchange_strong fails
        headIdx = head.sep.idx;
        assert(!UInt24_equals(headIdx, GREYLIST_NEXT));
        if (UInt24_equals(headIdx, GREYLIST_LAST)) {
            return NULL;
        }
        res = GreyPacket_FromIndex(greyPacketsStart, headIdx);
        GreyPacketRef next;
        next.atom = res->next.atom;
        if (next.atom != 0) {
            nextValue.atom = next.atom;
        } else {
            nextValue.sep.idx = UInt24_plus(headIdx, 1);
            nextValue.sep.size = UInt24_plus(head.sep.size, -1);
        }
    } while (!atomic_compare_exchange_strong(
        &list->head.atom, (uint64_t *)&head.atom, nextValue.atom));
    res->timesPoped += 1;
    return res;
}

#endif
