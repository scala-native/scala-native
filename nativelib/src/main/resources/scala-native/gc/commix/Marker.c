#if defined(SCALANATIVE_GC_COMMIX)
#include "shared/GCTypes.h"
#include <stdint.h>
#include <stdio.h>
#include <setjmp.h>
#include "Marker.h"
#include "Object.h"
#include "immix_commix/Log.h"
#include "State.h"
#include "immix_commix/headers/ObjectHeader.h"
#include "datastructures/GreyPacket.h"
#include "GCThread.h"
#include "shared/ThreadUtil.h"
#include "SyncGreyLists.h"

extern word_t *__modules;
extern int __modules_size;

#define LAST_FIELD_OFFSET -1

// Marking uses multiple threads in parallel. Note that there is no need to
// synchronize on any of the mark bytes in BlockMeta, LineMeta or ObjectMeta,
// because marking is idempotent. If we mark an object that has been already
// marked, it stays marked. When we check if the object has been marked, it
// could return an older state with allocated. This can cause us to re-trace
// a single object multiple times, but it is not as bad as the overhead caused
// by something like test-and-set or compare-and-swap. Marking uses grey packets
// to synchronize between threads. A grey packet is a fixed size list that
// contains pointers to objects for marking. Grey packets are kept in it own
// separate memory region (see heap->greyPacketsStart).
//
// Marking starts with the application (mutator) thread tracing the roots which
// produces the initial grey packets. See `Marker_MarkRoots`.
// Each marker has a grey packet with references to check ("in" packet).
// When it finds a new unmarked object the marker puts a pointer to
// it in the "out" packet. When the "in" packet is empty it gets
// another from the full packet list and returns the empty one to the empty
// packet list. Similarly, when the "out" packet get full, marker gets another
// empty packet and pushes the full one on the full packet list.
// Marking is done when all the packets are empty in the empty packet list or
// with only WeakRefereces in the WeakReference packet list. weakRefOut packets
// are used to hold weakReferences found during the marking phase. This way, in
// nullify phase it can be checked if their held objects were marked or not and
// their fields can be set accordingly.
//
// An object can have different number of outgoing pointers. Therefore, the
// number of objects to check per packet varies and packets take different
// amount of time to process. Marking cannot complete until the thread with the
// most work is done. Very large packets (in terms of work) can slow the marking
// down. To mitigate this we count the number of objects traced. If it goes over
// a threshold (MARK_MAX_WORK_PER_PACKET) then we transfer half of the remaining
// items to a packet and add that packet to the full packet list. We can also
// get object arrays larger than most packets. The object arrays are split into
// special fixed size (ARRAY_SPLIT_THRESHOLD) range packets which are added to
// full packets. The remainder is processed immediately. See
// `Marker_splitIncomingPacket` and `Marker_splitObjectArray`
//
// Depending on the number of full packets in the list `Marker_MarkAndScale` can
// start new threads. This is done on the master gc thread after each packet
// processed. The mutator and GC marking for the entire marking phase
// see `Marker_MarkUtilDone`, but other threads stop themselves when they cannot
// find any more full packets. If we left these threads running they would
// continuously query for new packets spending CPU resources, slowing other
// threads and not doing any work.

static inline GreyPacket *Marker_takeEmptyPacket(Heap *heap, Stats *stats) {
    return SyncGreyLists_takeEmptyPacket(heap, stats);
}

static inline GreyPacket *Marker_takeFullPacket(Heap *heap, Stats *stats) {
    GreyPacket *packet = SyncGreyLists_takeNotEmptyPacket(
        heap, stats, &heap->mark.full, mark_waiting);

    assert(packet == NULL || packet->type == grey_packet_refrange ||
           packet->size > 0);
    return packet;
}

static inline void Marker_giveEmptyPacket(Heap *heap, Stats *stats,
                                          GreyPacket *packet) {
    SyncGreyLists_giveEmptyPacket(heap, stats, packet);
}

static inline void Marker_giveFullPacket(Heap *heap, Stats *stats,
                                         GreyPacket *packet) {
    assert(packet->type == grey_packet_refrange || packet->size > 0);
    SyncGreyLists_giveNotEmptyPacket(heap, stats, &heap->mark.full, packet);
}

static inline void Marker_giveWeakRefPacket(Heap *heap, Stats *stats,
                                            GreyPacket *packet) {
    if (!GreyPacket_IsEmpty(packet)) {
        SyncGreyLists_giveNotEmptyPacket(heap, stats, &heap->mark.foundWeakRefs,
                                         packet);
    } else {
        SyncGreyLists_giveEmptyPacket(heap, stats, packet);
    }
}

static inline void Marker_markLockWords(Heap *heap, Stats *stats,
                                        GreyPacket **outHolder,
                                        GreyPacket **outWeakRefHolder,
                                        Object *object);

void Marker_markObject(Heap *heap, Stats *stats, GreyPacket **outHolder,
                       GreyPacket **outWeakRefHolder, Bytemap *bytemap,
                       Object *object, ObjectMeta *objectMeta) {
    assert(ObjectMeta_IsAllocated(objectMeta) ||
           ObjectMeta_IsMarked(objectMeta));
    assert(object->rtti != NULL);
    assert(Object_Size(object) != 0);

    Marker_markLockWords(heap, stats, outHolder, outWeakRefHolder, object);

    Object_Mark(heap, object, objectMeta);
    GreyPacket *out;
    if (Object_IsWeakReference(object)) {
        out = *outWeakRefHolder;
        if (!GreyPacket_Push(out, object)) {
            Marker_giveWeakRefPacket(heap, stats, out);
            *outWeakRefHolder = out = Marker_takeEmptyPacket(heap, stats);
            GreyPacket_Push(out, object);
        }
    }
    out = *outHolder;
    if (!GreyPacket_Push(out, object)) {
        Marker_giveFullPacket(heap, stats, out);
        *outHolder = out = Marker_takeEmptyPacket(heap, stats);
        GreyPacket_Push(out, object);
    }
}

static inline bool Marker_markField(Heap *heap, Stats *stats,
                                    GreyPacket **outHolder,
                                    GreyPacket **outWeakRefHolder,
                                    Field_t field) {
    if (Heap_IsWordInHeap(heap, field)) {
        ObjectMeta *fieldMeta = Bytemap_Get(heap->bytemap, field);
        if (ObjectMeta_IsAllocated(fieldMeta)) {
            Marker_markObject(heap, stats, outHolder, outWeakRefHolder,
                              heap->bytemap, (Object *)field, fieldMeta);
            return true;
        }
    }
    return false;
}

/* If compiling with enabled lock words check if object monitor is inflated and
 * can be marked. Otherwise, in singlethreaded mode this funciton is no-op
 */
static inline void Marker_markLockWords(Heap *heap, Stats *stats,
                                        GreyPacket **outHolder,
                                        GreyPacket **outWeakRefHolder,
                                        Object *object) {
#ifdef USES_LOCKWORD
    if (object != NULL) {
        Field_t rttiLock = object->rtti->rt.lockWord;
        if (Field_isInflatedLock(rttiLock)) {
            Field_t field = Field_allignedLockRef(rttiLock);
            Marker_markField(heap, stats, outHolder, outWeakRefHolder, field);
        }

        Field_t objectLock = object->lockWord;
        if (Field_isInflatedLock(objectLock)) {
            Field_t field = Field_allignedLockRef(objectLock);
            Marker_markField(heap, stats, outHolder, outWeakRefHolder, field);
        }
    }
#endif
}

void Marker_markConservative(Heap *heap, Stats *stats, GreyPacket **outHolder,
                             GreyPacket **outWeakRefHolder, word_t *address) {
    assert(Heap_IsWordInHeap(heap, address));
    if (Bytemap_isPtrAligned(address)) {
        Object *object = Object_GetUnmarkedObject(heap, address);
        Bytemap *bytemap = heap->bytemap;
        if (object != NULL) {
            ObjectMeta *objectMeta = Bytemap_Get(bytemap, (word_t *)object);
            if (ObjectMeta_IsAllocated(objectMeta)) {
                Marker_markObject(heap, stats, outHolder, outWeakRefHolder,
                                  bytemap, object, objectMeta);
            }
        }
    }
}

NO_SANITIZE int Marker_markRange(Heap *heap, Stats *stats,
                                 GreyPacket **outHolder,
                                 GreyPacket **outWeakRefHolder, word_t **from,
                                 size_t wordsLength, const size_t stride) {
    int objectsTraced = 0;
    const intptr_t alignmentMask = ~(sizeof(word_t) - 1);
    ubyte_t *alignedFrom = (ubyte_t *)((intptr_t)from & alignmentMask);
    ubyte_t *to = alignedFrom + (wordsLength + 1) * sizeof(word_t);
    ubyte_t *limit = (ubyte_t *)((uintptr_t)to & alignmentMask);
    for (ubyte_t *current = alignedFrom; current <= limit; current += stride) {
        word_t *field = *(word_t **)current;
        if (Heap_IsWordInHeap(heap, field)) {
            Marker_markConservative(heap, stats, outHolder, outWeakRefHolder,
                                    field);
            objectsTraced += 1;
        }
    }
    return objectsTraced;
}

int Marker_markRegularObject(Heap *heap, Stats *stats, Object *object,
                             GreyPacket **outHolder,
                             GreyPacket **outWeakRefHolder, Bytemap *bytemap) {
    int objectsTraced = 0;
    int32_t *refFieldOffsets = object->rtti->refFieldOffsets;
    for (int i = 0; refFieldOffsets[i] != LAST_FIELD_OFFSET; i++) {
        size_t fieldOffset = (size_t)refFieldOffsets[i];
        Field_t *fieldRef = (Field_t *)((int8_t *)object + fieldOffset);
        Field_t fieldReferant = *fieldRef;
        if (Object_IsReferantOfWeakReference(object, fieldOffset)) {
            continue;
        }
        objectsTraced += Marker_markField(heap, stats, outHolder,
                                          outWeakRefHolder, fieldReferant);
    }
    if (object->rtti->rt.id == __boxed_ptr_id) {
        // Boxed ptr always has a single field
        word_t *rawPtr = object->fields[0];
        if (Heap_IsWordInHeap(heap, rawPtr)) {
            Marker_markConservative(heap, stats, outHolder, outWeakRefHolder,
                                    rawPtr);
            objectsTraced += 1;
        }
    }
    return objectsTraced;
}

int Marker_splitObjectArray(Heap *heap, Stats *stats, GreyPacket **outHolder,
                            GreyPacket **outWeakRefHolder, word_t **fields,
                            size_t length) {
    word_t **limit = fields + length;
    word_t **lastBatch =
        fields + (length / ARRAY_SPLIT_BATCH) * ARRAY_SPLIT_BATCH;

    assert(lastBatch <= limit);
    for (word_t **batchFields = fields; batchFields < limit;
         batchFields += ARRAY_SPLIT_BATCH) {
        GreyPacket *slice = Marker_takeEmptyPacket(heap, stats);
        assert(slice != NULL);
        slice->type = grey_packet_refrange;
        slice->items[0] = (Stack_Type)batchFields;
        // no point writing the size, because it is constant
        Marker_giveFullPacket(heap, stats, slice);
    }

    size_t lastBatchSize = limit - lastBatch;
    int objectsTraced = 0;
    if (lastBatchSize > 0) {
        objectsTraced =
            Marker_markRange(heap, stats, outHolder, outWeakRefHolder,
                             lastBatch, lastBatchSize, sizeof(word_t));
    }
    return objectsTraced;
}

static int Marker_markObjectArray(Heap *heap, Stats *stats, Object *object,
                                  GreyPacket **outHolder,
                                  GreyPacket **outWeakRefHolder) {
    ArrayHeader *arrayHeader = (ArrayHeader *)object;
    size_t length = arrayHeader->length;
    word_t **fields = (word_t **)(arrayHeader + 1);
    int objectsTraced;
    if (length <= ARRAY_SPLIT_THRESHOLD) {
        objectsTraced =
            Marker_markRange(heap, stats, outHolder, outWeakRefHolder, fields,
                             length, sizeof(word_t));
    } else {
        // object array is two large, split it into pieces for multiple threads
        // to handle
        objectsTraced = Marker_splitObjectArray(
            heap, stats, outHolder, outWeakRefHolder, fields, length);
    }
    return objectsTraced;
}

static int Marker_markBlobArray(Heap *heap, Stats *stats, Object *object,
                                GreyPacket **outHolder,
                                GreyPacket **outWeakRefHolder) {
    ArrayHeader *arrayHeader = (ArrayHeader *)object;
    size_t bytesLength = BlobArray_ScannableLimit(arrayHeader);
    size_t objectsLength = bytesLength / sizeof(word_t);
    word_t **blobStart = (word_t **)(arrayHeader + 1);
    int objectsTraced;
    // From that point we can treat it similary as object array
    if (objectsLength <= ARRAY_SPLIT_THRESHOLD) {
        objectsTraced =
            Marker_markRange(heap, stats, outHolder, outWeakRefHolder,
                             blobStart, objectsLength, sizeof(word_t));
    } else {
        // object array is two large, split it into pieces for multiple threads
        // to handle
        objectsTraced = Marker_splitObjectArray(
            heap, stats, outHolder, outWeakRefHolder, blobStart, objectsLength);
    }
    return objectsTraced;
}

static inline void Marker_splitIncomingPacket(Heap *heap, Stats *stats,
                                              GreyPacket *in) {
    int toMove = in->size / 2;
    if (toMove > 0) {
        GreyPacket *slice = Marker_takeEmptyPacket(heap, stats);
        assert(slice != NULL);
        GreyPacket_MoveItems(in, slice, toMove);
        Marker_giveFullPacket(heap, stats, slice);
    }
}

static inline void Marker_RetakeIfNull(Heap *heap, Stats *stats,
                                       GreyPacket **outHolder) {
    if (*outHolder == NULL) {
        GreyPacket *fresh = Marker_takeEmptyPacket(heap, stats);
        assert(fresh != NULL);
        *outHolder = fresh;
    }
}

void Marker_markPacket(Heap *heap, Stats *stats, GreyPacket *in,
                       GreyPacket **outHolder, GreyPacket **outWeakRefHolder) {
    Bytemap *bytemap = heap->bytemap;
    Marker_RetakeIfNull(heap, stats, outHolder);
    Marker_RetakeIfNull(heap, stats, outWeakRefHolder);
    int objectsTraced = 0;
    while (!GreyPacket_IsEmpty(in)) {
        Object *object = GreyPacket_Pop(in);
        if (Object_IsArray(object)) {
            const int arrayId = object->rtti->rt.id;
            if (arrayId == __object_array_id) {
                objectsTraced += Marker_markObjectArray(
                    heap, stats, object, outHolder, outWeakRefHolder);
            } else if (arrayId == __blob_array_id) {
                objectsTraced += Marker_markBlobArray(
                    heap, stats, object, outHolder, outWeakRefHolder);
            }
            // non-object arrays do not contain pointers
        } else {
            objectsTraced += Marker_markRegularObject(
                heap, stats, object, outHolder, outWeakRefHolder, bytemap);
        }
        if (objectsTraced > MARK_MAX_WORK_PER_PACKET) {
            // the packet has a lot of work split the remainder in two
            Marker_splitIncomingPacket(heap, stats, in);
            objectsTraced = 0;
        }
    }
}

void Marker_markRangePacket(Heap *heap, Stats *stats, GreyPacket *in,
                            GreyPacket **outHolder,
                            GreyPacket **outWeakRefHolder) {
    Bytemap *bytemap = heap->bytemap;
    Marker_RetakeIfNull(heap, stats, outHolder);
    Marker_RetakeIfNull(heap, stats, outWeakRefHolder);
    word_t **fields = (word_t **)in->items[0];
    Marker_markRange(heap, stats, outHolder, outWeakRefHolder, fields,
                     ARRAY_SPLIT_BATCH, sizeof(word_t));
    in->type = grey_packet_reflist;
    in->size = 0;
}

static inline void Marker_markBatch(Heap *heap, Stats *stats, GreyPacket *in,
                                    GreyPacket **outHolder,
                                    GreyPacket **outWeakRefHolder) {
    Stats_RecordTimeBatch(stats, start_ns);
    switch (in->type) {
    case grey_packet_reflist:
        Marker_markPacket(heap, stats, in, outHolder, outWeakRefHolder);
        break;
    case grey_packet_refrange:
        Marker_markRangePacket(heap, stats, in, outHolder, outWeakRefHolder);
        break;
    }
    Stats_RecordTimeBatch(stats, end_ns);
    Stats_RecordEventBatches(stats, event_mark_batch, start_ns, end_ns);
}

void Marker_Mark(Heap *heap, Stats *stats) {
    GreyPacket *in = Marker_takeFullPacket(heap, stats);
    GreyPacket *out = NULL;
    GreyPacket *weakRefOut = NULL;
    while (in != NULL) {
        Marker_markBatch(heap, stats, in, &out, &weakRefOut);

        assert(out != NULL);
        assert(GreyPacket_IsEmpty(in));
        GreyPacket *next = Marker_takeFullPacket(heap, stats);
        if (next != NULL) {
            Marker_giveEmptyPacket(heap, stats, in);
        } else {
            if (!GreyPacket_IsEmpty(out)) {
                // use the out packet as source
                next = out;
                out = in;
            } else {
                // next == NULL, exits
                Marker_giveEmptyPacket(heap, stats, in);
                Marker_giveEmptyPacket(heap, stats, out);
                Marker_giveWeakRefPacket(heap, stats, weakRefOut);
            }
        }
        in = next;
    }
}

void Marker_MarkAndScale(Heap *heap, Stats *stats) {
    GreyPacket *in = Marker_takeFullPacket(heap, stats);
    GreyPacket *out = NULL;
    GreyPacket *weakRefOut = NULL;
    while (in != NULL) {
        Marker_markBatch(heap, stats, in, &out, &weakRefOut);

        assert(out != NULL);
        assert(GreyPacket_IsEmpty(in));
        GreyPacket *next = Marker_takeFullPacket(heap, stats);
        if (next != NULL) {
            Marker_giveEmptyPacket(heap, stats, in);
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
            if (!GreyPacket_IsEmpty(out)) {
                // use the out packet as source
                next = out;
                out = in;
            } else {
                // next == NULL, exits
                Marker_giveEmptyPacket(heap, stats, in);
                Marker_giveEmptyPacket(heap, stats, out);
                Marker_giveWeakRefPacket(heap, stats, weakRefOut);
            }
        }
        in = next;
    }
}

void Marker_MarkUntilDone(Heap *heap, Stats *stats) {
    while (!Marker_IsMarkDone(heap)) {
        Marker_Mark(heap, stats);
        if (!Marker_IsMarkDone(heap)) {
            thread_yield();
        }
    }
}

NO_SANITIZE void Marker_markProgramStack(MutatorThread *thread, Heap *heap,
                                         Stats *stats, GreyPacket **outHolder,
                                         GreyPacket **outWeakRefHolder) {
    word_t **stackBottom = thread->stackBottom;
    word_t **stackTop = NULL;
    do {
        // Can spuriously fail, very rare, yet deadly
        stackTop = (word_t **)atomic_load_explicit(&thread->stackTop,
                                                   memory_order_acquire);
    } while (stackTop == NULL);
    word_t **rangeStart = stackTop < stackBottom ? stackTop : stackBottom;
    word_t **rangeEnd = stackTop < stackBottom ? stackBottom : stackTop;
    size_t rangeSize = rangeEnd - rangeStart;
    Marker_markRange(heap, stats, outHolder, outWeakRefHolder, rangeStart,
                     rangeSize, sizeof(word_t));

    // Mark registers buffer
    size_t registersBufferBytes = sizeof(thread->registersBuffer);
    size_t registerBufferStride =
#if defined(CAPTURE_SETJMP)
        // Pointers in jmp_bufr might be non word-size aligned
        sizeof(uint32_t);
#else
        sizeof(word_t);
#endif
    Marker_markRange(heap, stats, outHolder, outWeakRefHolder,
                     (word_t **)&thread->registersBuffer,
                     registersBufferBytes / sizeof(word_t),
                     registerBufferStride);
}

void Marker_markModules(Heap *heap, Stats *stats, GreyPacket **outHolder,
                        GreyPacket **outWeakRefHolder) {
    word_t **modules = &__modules;
    int nb_modules = __modules_size;
    Bytemap *bytemap = heap->bytemap;
    word_t **limit = modules + nb_modules;
    for (word_t **current = modules; current < limit; current++) {
        Marker_markField(heap, stats, outHolder, outWeakRefHolder, *current);
    }
}

void Marker_markCustomRoots(Heap *heap, Stats *stats, GreyPacket **outHolder,
                            GreyPacket **outWeakRefHolder, GC_Roots *roots) {
    mutex_lock(&roots->modificationLock);
    for (GC_Root *it = roots->head; it != NULL; it = it->next) {
        size_t size = it->range.address_high - it->range.address_low;
        Marker_markRange(heap, stats, outHolder, outWeakRefHolder,
                         (word_t **)it->range.address_low, size,
                         sizeof(word_t));
    }
    mutex_unlock(&roots->modificationLock);
}

void Marker_MarkRoots(Heap *heap, Stats *stats) {
    atomic_thread_fence(memory_order_seq_cst);

    GreyPacket *out = Marker_takeEmptyPacket(heap, stats);
    GreyPacket *weakRefOut = Marker_takeEmptyPacket(heap, stats);
    MutatorThreads_foreach(mutatorThreads, node) {
        MutatorThread *thread = node->value;
        Marker_markProgramStack(thread, heap, stats, &out, &weakRefOut);
    }
    Marker_markModules(heap, stats, &out, &weakRefOut);
    Marker_markCustomRoots(heap, stats, &out, &weakRefOut, customRoots);
    Marker_giveFullPacket(heap, stats, out);
    Marker_giveWeakRefPacket(heap, stats, weakRefOut);
}

bool Marker_IsMarkDone(Heap *heap) {
    uint32_t emptySize = GreyList_Size(&heap->mark.empty);
    uint32_t weakRefSize = GreyList_Size(&heap->mark.foundWeakRefs);
    uint32_t size = emptySize + weakRefSize;
    return size == heap->mark.total;
}

#endif
