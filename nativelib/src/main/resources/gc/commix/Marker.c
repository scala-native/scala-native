#include <stdio.h>
#include <setjmp.h>
#include "Marker.h"
#include "Object.h"
#include "Log.h"
#include "State.h"
#include "headers/ObjectHeader.h"
#include "datastructures/GreyPacket.h"
#include "GCThread.h"
#include <sched.h>

extern word_t *__modules;
extern int __modules_size;
extern word_t **__stack_bottom;

#define LAST_FIELD_OFFSET -1

static inline GreyPacket *Marker_takeEmptyPacket(Heap *heap, Stats *stats) {
    Stats_RecordTimeSync(stats, start_ns);
    GreyPacket *packet = GreyList_Pop(&heap->mark.empty, heap->greyPacketsStart);
    Stats_RecordTimeSync(stats, end_ns);
    Stats_RecordEventSync(stats, event_sync, start_ns, end_ns);
    if (packet != NULL) {
        // Another thread setting size = 0 might not arrived, just write it now.
        // Avoiding a memfence.
        packet->size = 0;
        packet->type = grey_packet_reflist;
    }
    assert(packet != NULL);
    return packet;
}

static inline GreyPacket *Marker_takeFullPacket(Heap *heap, Stats *stats) {
    Stats_RecordTimeSync(stats, start_ns);
    GreyPacket *packet = GreyList_Pop(&heap->mark.full, heap->greyPacketsStart);
    if (packet != NULL) {
        atomic_thread_fence(memory_order_release);
    }
    Stats_RecordTimeSync(stats, end_ns);
    Stats_RecordEventSync(stats, event_sync, stats->mark_waiting_start_ns, end_ns);
    if (packet == NULL) {
        Stats_MarkerNoFullPacket(stats, start_ns, end_ns);
    } else {
        Stats_MarkerGotFullPacket(stats, end_ns);
    }
    assert(packet == NULL || packet->type == grey_packet_refrange || packet->size > 0);
    return packet;
}

static inline void Marker_giveEmptyPacket(Heap *heap, Stats *stats, GreyPacket *packet) {
    assert(packet->size == 0);
    // no memfence needed see Marker_takeEmptyPacket
    Stats_RecordTimeSync(stats, start_ns);
    GreyList_Push(&heap->mark.empty, heap->greyPacketsStart, packet);
    Stats_RecordTimeSync(stats, end_ns);
    Stats_RecordEventSync(stats, event_sync, start_ns, end_ns);
}

static inline void Marker_giveFullPacket(Heap *heap, Stats *stats, GreyPacket *packet) {
    assert(packet->type == grey_packet_refrange || packet->size > 0);
    // make all the contents visible to other threads
    atomic_thread_fence(memory_order_acquire);
    assert(GreyList_Size(&heap->mark.full) <= heap->mark.total);
    Stats_RecordTimeSync(stats, start_ns);
    GreyList_Push(&heap->mark.full, heap->greyPacketsStart, packet);
    Stats_RecordTimeSync(stats, end_ns);
    Stats_RecordEventSync(stats, event_sync, start_ns, end_ns);
}

void Marker_markObject(Heap *heap, Stats *stats, GreyPacket **outHolder, Bytemap *bytemap,
                       Object *object, ObjectMeta *objectMeta) {
    assert(ObjectMeta_IsAllocated(objectMeta) || ObjectMeta_IsMarked(objectMeta));

    assert(Object_Size(object) != 0);
    Object_Mark(heap, object, objectMeta);

    GreyPacket *out = *outHolder;
    if (!GreyPacket_Push(out, object)) {
        Marker_giveFullPacket(heap, stats, out);
        *outHolder = out = Marker_takeEmptyPacket(heap, stats);
        GreyPacket_Push(out, object);
    }
}

void Marker_markConservative(Heap *heap, Stats *stats, GreyPacket **outHolder, word_t *address) {
    assert(Heap_IsWordInHeap(heap, address));
    Object *object = Object_GetUnmarkedObject(heap, address);
    Bytemap *bytemap = heap->bytemap;
    if (object != NULL) {
        ObjectMeta *objectMeta = Bytemap_Get(bytemap, (word_t *)object);
        assert(ObjectMeta_IsAllocated(objectMeta));
        if (ObjectMeta_IsAllocated(objectMeta)) {
            Marker_markObject(heap, stats, outHolder, bytemap, object, objectMeta);
        }
    }
}

void Marker_markRange(Heap *heap, Stats *stats, GreyPacket* in, GreyPacket **outHolder, Bytemap *bytemap,
                      word_t **fields, size_t length) {
    word_t **limit = fields + length;
    for (word_t **current = fields; current < limit; current++) {
        word_t *field = *current;
        if (Heap_IsWordInHeap(heap, field)) {
            ObjectMeta *fieldMeta = Bytemap_Get(bytemap, field);
            if (ObjectMeta_IsAllocated(fieldMeta)) {
                Marker_markObject(heap, stats, outHolder, bytemap,
                                  (Object *)field, fieldMeta);
            }
        }
    }
}

void Marker_markPacket(Heap *heap, Stats *stats, GreyPacket* in, GreyPacket **outHolder) {
    Bytemap *bytemap = heap->bytemap;
    if (*outHolder == NULL) {
        GreyPacket *fresh = Marker_takeEmptyPacket(heap, stats);
        assert(fresh != NULL);
        *outHolder = fresh;
    }
    while (!GreyPacket_IsEmpty(in)) {
        Object *object = GreyPacket_Pop(in);

        if (Object_IsArray(object)) {
            if (object->rtti->rt.id == __object_array_id) {
                ArrayHeader *arrayHeader = (ArrayHeader *)object;
                size_t length = arrayHeader->length;
                word_t **fields = (word_t **)(arrayHeader + 1);
                if (length <= ARRAY_SPLIT_THRESHOLD) {
                    Marker_markRange(heap, stats, in, outHolder, bytemap, fields, length);
                } else {
                    if (GreyPacket_IsEmpty(in)) {
                        // last item - deal with it now

                        // leave the last batch for the current thread
                        word_t **limit = fields + length;
                        word_t **lastBatch = fields + (length / ARRAY_SPLIT_BATCH) * ARRAY_SPLIT_BATCH;

                        assert(lastBatch <= limit);
                        for (word_t **batchFields = fields; batchFields < limit; batchFields += ARRAY_SPLIT_BATCH) {
                            GreyPacket *slice = Marker_takeEmptyPacket(heap, stats);
                            assert(slice != NULL);
                            slice->type = grey_packet_refrange;
                            slice->items[0] = (Stack_Type) batchFields;
                            // no point writing the size, because it is constant
                            Marker_giveFullPacket(heap, stats, slice);
                        }

                        size_t lastBatchSize = limit - lastBatch;
                        if (lastBatchSize > 0) {
                            Marker_markRange(heap, stats , in, outHolder, bytemap, lastBatch, lastBatchSize);
                        }
                    } else {
                        // pass it on to someone else
                        GreyPacket *slice = Marker_takeEmptyPacket(heap, stats);
                        assert(slice != NULL);
                        GreyPacket_Push(slice, object);
                        Marker_giveFullPacket(heap, stats, slice);
                    }
                }
            }
            // non-object arrays do not contain pointers
        } else {
            int64_t *ptr_map = object->rtti->refMapStruct;
            for (int64_t *current = ptr_map; *current != LAST_FIELD_OFFSET; current++) {
                word_t *field = object->fields[*current];
                if (Heap_IsWordInHeap(heap, field)) {
                    ObjectMeta *fieldMeta = Bytemap_Get(bytemap, field);
                    if (ObjectMeta_IsAllocated(fieldMeta)) {
                        Marker_markObject(heap, stats, outHolder, bytemap, (Object *)field,
                                          fieldMeta);
                    }
                }
            }
        }
    }
}

void Marker_markRangePacket(Heap *heap, Stats *stats, GreyPacket* in, GreyPacket **outHolder) {
    Bytemap *bytemap = heap->bytemap;
    if (*outHolder == NULL) {
        GreyPacket *fresh = Marker_takeEmptyPacket(heap, stats);
        assert(fresh != NULL);
        *outHolder = fresh;
    }
    word_t **fields = (word_t **) in->items[0];
    Marker_markRange(heap, stats, in, outHolder, bytemap, fields, ARRAY_SPLIT_BATCH);
    in->type = grey_packet_reflist;
    in->size = 0;
}

static inline void Marker_markBatch(Heap *heap, Stats *stats, GreyPacket* in, GreyPacket **outHolder) {
    Stats_RecordTimeBatch(stats, start_ns);
    switch (in->type) {
        case grey_packet_reflist:
            Marker_markPacket(heap, stats, in, outHolder);
            break;
        case grey_packet_refrange:
            Marker_markRangePacket(heap, stats, in, outHolder);
            break;
    }
    Stats_RecordTimeBatch(stats, end_ns);
    Stats_RecordEventBatches(stats, event_mark_batch, start_ns, end_ns);
}

void Marker_Mark(Heap *heap, Stats *stats) {
    GreyPacket* in = Marker_takeFullPacket(heap, stats);
    GreyPacket *out = NULL;
    while (in != NULL) {
        Marker_markBatch(heap, stats, in, &out);

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
            }
        }
        in = next;
    }
}

void Marker_MarkAndScale(Heap *heap, Stats *stats) {
    GreyPacket* in = Marker_takeFullPacket(heap, stats);
    GreyPacket *out = NULL;
    while (in != NULL) {
        Marker_markBatch(heap, stats, in, &out);

        assert(out != NULL);
        assert(GreyPacket_IsEmpty(in));
        GreyPacket *next = Marker_takeFullPacket(heap, stats);
        if (next != NULL) {
            Marker_giveEmptyPacket(heap, stats, in);
            uint32_t remainingFullPackets = next->next.sep.size;
            if (remainingFullPackets > MARK_SPAWN_THREADS_MIN_PACKETS) {
                int maxThreads = heap->gcThreads.count;
                int activeThreads = GCThread_ActiveCount(heap);
                int targetThreadCount = (remainingFullPackets - MARK_SPAWN_THREADS_MIN_PACKETS) / MARK_MIN_PACKETS_PER_THREAD;
                if (targetThreadCount > maxThreads) {
                    targetThreadCount = maxThreads;
                }
                int toSpawn = targetThreadCount - activeThreads;
                if (toSpawn > 0) {
                    GCThread_WakeWorkers(heap, toSpawn);
                }
            }
        } else {
            if (!GreyPacket_IsEmpty(out)) {
                // use the out packet as source
                next = out;
                out = in;
            } else {
                // next == NULL, exits
                Marker_giveEmptyPacket(heap, stats, in);
                Marker_giveEmptyPacket(heap, stats, out);
            }
        }
        in = next;
    }
}

void Marker_MarkUtilDone(Heap *heap, Stats *stats) {
    while (!Marker_IsMarkDone(heap)) {
        Marker_Mark(heap, stats);
        if (!Marker_IsMarkDone(heap)) {
            sched_yield();
        }
    }
}

void Marker_markProgramStack(Heap *heap, Stats *stats, GreyPacket **outHolder) {
    // Dumps registers into 'regs' which is on stack
    jmp_buf regs;
    setjmp(regs);
    word_t *dummy;

    word_t **current = &dummy;
    word_t **stackBottom = __stack_bottom;

    while (current <= stackBottom) {

        word_t *stackObject = *current;
        if (Heap_IsWordInHeap(heap, stackObject)) {
            Marker_markConservative(heap, stats, outHolder, stackObject);
        }
        current += 1;
    }
}

void Marker_markModules(Heap *heap, Stats *stats, GreyPacket **outHolder) {
    word_t **modules = &__modules;
    int nb_modules = __modules_size;
    Bytemap *bytemap = heap->bytemap;
    word_t **limit = modules + nb_modules;
    for (word_t **current = modules; current < limit; current++) {
        Object *object = (Object *) *current;
        if (Heap_IsWordInHeap(heap, (word_t *)object)) {
            // is within heap
            ObjectMeta *objectMeta = Bytemap_Get(bytemap, (word_t *)object);
            if (ObjectMeta_IsAllocated(objectMeta)) {
                Marker_markObject(heap, stats, outHolder, bytemap, object, objectMeta);
            }
        }
    }
}

void Marker_MarkRoots(Heap *heap, Stats *stats) {
    GreyPacket *out = Marker_takeEmptyPacket(heap, stats);
    Marker_markProgramStack(heap, stats, &out);
    Marker_markModules(heap, stats , &out);
    Marker_giveFullPacket(heap, stats, out);
}

bool Marker_IsMarkDone(Heap *heap) {
    return GreyList_Size(&heap->mark.empty) == heap->mark.total;
}
