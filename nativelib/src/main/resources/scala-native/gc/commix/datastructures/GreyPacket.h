#ifndef IMMIX_GREYPACKET_H
#define IMMIX_GREYPACKET_H

#include <stdatomic.h>
#include <inttypes.h>
#include <stdbool.h>
#include "../Constants.h"
#include "shared/GCTypes.h"
#include "BlockRange.h"
#include "immix_commix/Log.h"
#include "immix_commix/headers/ObjectHeader.h"
#include "immix_commix/UInt24.h"

typedef Object *Stack_Type;

// UInt24 used instead of uint_32 bitset for cross-platform compatiblity, mainly
// due the lack of support for 3-byte alignment in MSVC
// https://docs.microsoft.com/en-us/cpp/c-language/padding-and-alignment-of-structure-members?redirectedfrom=MSDN&view=msvc-160
// It would result in size of 16 bytes on Windows and 8 on Unix
typedef union {
    struct __attribute__((packed)) {
        UInt24 idx;
        // Size is kept in the reference it is in sync with the grey list.
        // Otherwise the updates can get reordered causing the number
        // temporarily appearing larger than it is which will trigger
        // Marker_IsMarkDone prematurely.
        UInt24 size;
        uint16_t timesPoped; // used to avoid ABA problems when popping
    } sep;
    atomic_uint_least64_t atom;
} GreyPacketRef;

#define sizeof_field(s, m) (sizeof((((s *)0)->m)))
static_assert(sizeof_field(GreyPacketRef, sep) ==
                  sizeof_field(GreyPacketRef, atom),
              "GreyPacketRef sep and atom value should have the same size");

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

#define GREYLIST_NEXT (UInt24_fromUInt32(0))
#define GREYLIST_LAST (UInt24_fromUInt32(1))

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

static inline UInt24 GreyPacket_IndexOf(word_t *greyPacketsStart,
                                        GreyPacket *packet) {
    assert(packet != NULL);
    assert((void *)packet >= (void *)greyPacketsStart);
    return UInt24_fromUInt32((packet - (GreyPacket *)greyPacketsStart) + 2);
}

static inline GreyPacket *GreyPacket_FromIndex(word_t *greyPacketsStart,
                                               UInt24 idx) {
    uint32_t idxValue = UInt24_toUInt32(idx);
    assert(idxValue >= 2);
    return (GreyPacket *)greyPacketsStart + (idxValue - 2);
}

static inline uint64_t GreyPacketRef_Empty() {
    GreyPacketRef initial;
    initial.sep.idx = GREYLIST_LAST;
    initial.sep.size = UInt24_fromUInt32(0);
    initial.sep.timesPoped = 0;
    return initial.atom;
}

#endif // IMMIX_GREYPACKET_H