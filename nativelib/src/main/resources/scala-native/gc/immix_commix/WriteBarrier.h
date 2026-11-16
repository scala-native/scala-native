#ifndef IMMIX_COMMIX_WRITE_BARRIER_H
#define IMMIX_COMMIX_WRITE_BARRIER_H

// Object-remembering write barrier, enabled by SCALANATIVE_GC_WRITE_BARRIER.
//
// Before a reference is stored into a heap object, generated code checks the
// object's ObjectMeta byte. Only an old object that is not remembered yet
// (om_old_unlogged) takes the slow path, which remembers it in the storing
// thread's buffer and sets om_old_logged, so later stores into it stay on
// the fast path. The fast path reads the three variables below; objects
// outside the heap range (statics, zones, stacks) never take it.

#include <stddef.h>
#include <stdint.h>
#include "shared/GCTypes.h"
#include "immix_commix/metadata/ObjectMeta.h"

#ifdef SCALANATIVE_GC_WRITE_BARRIER

#if UINTPTR_MAX > 0xFFFFFFFFu && !defined(_WIN32)
// The heap is aligned to the barrier window, and the bytemap covers all of
// it, so the fast path indexes the bytemap with the masked object address and
// does not check the heap range. The bytemap's address space is reserved but
// only the part for the heap is ever written. Windows commits reserved memory
// explicitly, and 32-bit targets lack the address space, so both check the
// range instead. Must match GCBarrierWindowBits in Lower.
#define SCALANATIVE_GC_BARRIER_WINDOW_BITS 40
#define SCALANATIVE_GC_BARRIER_WINDOW                                          \
    ((size_t)1 << SCALANATIVE_GC_BARRIER_WINDOW_BITS)
#endif

extern word_t *scalanative_GC_barrier_heap_start;
// Size in bytes of the reserved heap range, which the bytemap covers.
extern size_t scalanative_GC_barrier_heap_size;
extern ObjectMeta *scalanative_GC_barrier_bytemap;

typedef struct {
    word_t **items;
    size_t size;
    size_t capacity;
} RememberedSet;

// The slow paths keep the caller's registers; must match
// writeBarrierPreservesMost in AbstractCodeGen.
#if (defined(__aarch64__) || defined(__x86_64__)) && !defined(_WIN32)
#define WRITE_BARRIER_SLOW_PATH __attribute__((preserve_most))
#else
#define WRITE_BARRIER_SLOW_PATH
#endif

WRITE_BARRIER_SLOW_PATH void scalanative_GC_write_barrier_slow(word_t *object);
WRITE_BARRIER_SLOW_PATH void scalanative_GC_write_barrier_slot(word_t *slot);
// Like scalanative_GC_write_barrier_slow, for a store into `slot` of `object`.
WRITE_BARRIER_SLOW_PATH void scalanative_GC_write_barrier_elem(word_t *object,
                                                               word_t *slot);

#ifdef SCALANATIVE_GC_STICKY
// Sticky Immix remembers stores into a large object array by card: it keeps
// the array unlogged, so that every store into it takes the slow path, which
// marks the card of the written slot dirty, and remembers the array, tagged,
// once per collection. A minor collection then scans only its dirty cards.
#define WRITE_BARRIER_CARD_SHIFT 9
#define WRITE_BARRIER_CARD_ARRAY_MIN_LENGTH 2048
#define WRITE_BARRIER_CARD_DIRTY 1
// Set on the card of the array's header while the array is remembered.
#define WRITE_BARRIER_CARD_QUEUED 2
// Low bit of a remembered set entry that is such an array.
#define WRITE_BARRIER_CARDED_TAG ((word_t)1)

extern uint8_t *WriteBarrier_cards;

static inline size_t WriteBarrier_CardIndex(void *address) {
    return ((word_t)address - (word_t)scalanative_GC_barrier_heap_start) >>
           WRITE_BARRIER_CARD_SHIFT;
}

static inline word_t *WriteBarrier_CardStart(size_t card) {
    return (word_t *)((ubyte_t *)scalanative_GC_barrier_heap_start +
                      (card << WRITE_BARRIER_CARD_SHIFT));
}

// Forgets all dirty cards of the heap's first `heapBytes` bytes.
void WriteBarrier_ClearCards(size_t heapBytes);
#endif

void WriteBarrier_Init(word_t *heapStart, size_t heapSize, ObjectMeta *data);
void RememberedSet_Push(RememberedSet *set, word_t *object);
void RememberedSet_Clear(RememberedSet *set);

// SCALANATIVE_GC_WRITE_BARRIER_VERIFY (Immix only) checks the barrier
// without a generational collector: every collection leaves its survivors
// old, and the next one aborts if an old object that was not remembered
// references an object allocated in between.
#ifdef SCALANATIVE_GC_WRITE_BARRIER_VERIFY
#ifndef SCALANATIVE_GC_IMMIX
#error "SCALANATIVE_GC_WRITE_BARRIER_VERIFY is supported only by Immix"
#endif
void WriteBarrier_VerifyBeforeCollection(void);
// With SCALANATIVE_GC_STICKY, also checks that no survivor references an
// object the collection freed.
void WriteBarrier_VerifyAfterCollection(void);
#endif

#endif // SCALANATIVE_GC_WRITE_BARRIER

#if defined(SCALANATIVE_GC_WRITE_BARRIER_VERIFY) &&                            \
    !defined(SCALANATIVE_GC_WRITE_BARRIER)
#error "SCALANATIVE_GC_WRITE_BARRIER_VERIFY requires the write barrier"
#endif

#endif // IMMIX_COMMIX_WRITE_BARRIER_H
