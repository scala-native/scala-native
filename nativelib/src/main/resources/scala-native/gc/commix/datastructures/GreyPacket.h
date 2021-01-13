#ifndef IMMIX_GREYPACKET_H
#define IMMIX_GREYPACKET_H

#include <stdatomic.h>
#include <inttypes.h>
#include <stdbool.h>
#include "../Constants.h"
#include "../GCTypes.h"
#include "BlockRange.h"
#include "../Log.h"
#include "../headers/ObjectHeader.h"

typedef Object *Stack_Type;

typedef union {
    struct __attribute__((packed)) {
        uint32_t idx : BLOCK_COUNT_BITS;
        // Size is kept in the reference it is in sync with the grey list.
        // Otherwise the updates can get reordered causing the number
        // temporarily appearing larger than it is which will trigger
        // Marker_IsMarkDone prematurely.
        uint32_t size : BLOCK_COUNT_BITS;
        uint16_t timesPoped; // used to avoid ABA problems when popping
    } sep;
    atomic_uint_least64_t atom;
} GreyPacketRef;

typedef enum {
    grey_packet_reflist = 0x0,
    grey_packet_refrange = 0x1
} GreyPacketType;

typedef struct {
    GreyPacketRef next;
    atomic_uint_least32_t timesPoped; // used to avoid ABA problems when popping
    uint16_t size;
    uint16_t type;
    Stack_Type items[GREY_PACKET_ITEMS];
} GreyPacket;

#define GREYLIST_NEXT ((uint32_t)0)
#define GREYLIST_LAST ((uint32_t)1)

typedef struct {
    GreyPacketRef head;
} GreyList;

bool GreyPacket_Push(GreyPacket *packet, Stack_Type value);
Stack_Type GreyPacket_Pop(GreyPacket *packet);
bool GreyPacket_IsEmpty(GreyPacket *packet);
void GreyPacket_MoveItems(GreyPacket *src, GreyPacket *dst, int count);

void GreyList_Init(GreyList *list);
uint32_t GreyList_Size(GreyList *list);
void GreyList_Push(GreyList *list, word_t *greyPacketsStart,
                   GreyPacket *packet);
void GreyList_PushAll(GreyList *list, word_t *greyPacketsStart,
                      GreyPacket *first, uint_fast32_t size);
GreyPacket *GreyList_Pop(GreyList *list, word_t *greyPacketsStart);

static inline uint32_t GreyPacket_IndexOf(word_t *greyPacketsStart,
                                          GreyPacket *packet) {
    assert(packet != NULL);
    assert((void *)packet >= (void *)greyPacketsStart);
    return (uint32_t)(packet - (GreyPacket *)greyPacketsStart) + 2;
}

static inline GreyPacket *GreyPacket_FromIndex(word_t *greyPacketsStart,
                                               uint32_t idx) {
    assert(idx >= 2);
    return (GreyPacket *)greyPacketsStart + (idx - 2);
}

static inline uint64_t GreyPacketRef_Empty() {
    GreyPacketRef initial;
    initial.sep.idx = GREYLIST_LAST;
    initial.sep.size = 0;
    initial.sep.timesPoped = 0;
    return initial.atom;
}

#endif // IMMIX_GREYPACKET_H