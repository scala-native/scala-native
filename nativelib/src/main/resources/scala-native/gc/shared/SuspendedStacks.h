#ifndef GC_SUSPENDED_STACKS_H
#define GC_SUSPENDED_STACKS_H

#include <stdatomic.h>
#include <stddef.h>
#include <stdint.h>

/* What the collector knows about the stacks of one-shot continuations
 * (delimcc_oneshot.c). Every such stack has a slot in a table of blocks; the
 * table changes only while no collection runs.
 *
 * - A stack that waits for a coroutine it started or resumed has `waitingTop`
 *   set: [waitingTop, bottom) is scanned as roots.
 * - The stacks captured by a suspended continuation are not roots. A
 *   `scala.scalanative.runtime.SuspendedStacks` object points to the first
 *   one; the marker scans the whole list when it visits that object and
 *   records the mark cycle in the slot of the first stack. A continuation
 *   that was not visited in a completed cycle is unreachable and can no
 *   longer be resumed.
 */
typedef struct StackSlot {
    _Atomic(void *) waitingTop;
    void *bottom;
    void *guardPage; // inaccessible, may lie inside [waitingTop, bottom)
    atomic_uint_fast64_t markedIn;
    // Owned by the continuation runtime.
    atomic_uint_fast64_t capturedIn;
    atomic_uint_fast64_t capture; // 0 while no continuation holds the stack
    void *captured;
} StackSlot;

#define STACK_SLOTS_PER_BLOCK 80

typedef struct StackSlotBlock {
    struct StackSlotBlock *next;
    size_t used;
    StackSlot slots[STACK_SLOTS_PER_BLOCK];
} StackSlotBlock;

extern StackSlotBlock *scalanative_GC_stackSlots;

typedef struct SuspendedStack {
    void *top;       // lowest address in use, NULL while not handed over
    void *bottom;    // highest address
    void *guardPage; // inaccessible, may lie inside [top, bottom)
    struct SuspendedStack *next;
    StackSlot *slot;
} SuspendedStack;

// Type descriptor (`object->rtti`) of the SuspendedStacks class. Set by the
// continuation runtime with the first such object; NULL until then.
extern void *scalanative_GC_suspendedStacksRtti;

// The cycle being marked, or the last one that was.
extern atomic_uint_fast64_t scalanative_GC_markCycle;
// The last cycle whose marking completed.
extern atomic_uint_fast64_t scalanative_GC_completedMarkCycle;

static inline void SuspendedStacks_markStarted(void) {
    atomic_fetch_add(&scalanative_GC_markCycle, 1);
}

static inline void SuspendedStacks_markCompleted(void) {
    atomic_store(&scalanative_GC_completedMarkCycle,
                 atomic_load(&scalanative_GC_markCycle));
}

#define FOREACH_STACK_SLOT(slot)                                               \
    for (StackSlotBlock *slot##Block = scalanative_GC_stackSlots;              \
         slot##Block != NULL; slot##Block = slot##Block->next)                 \
        for (StackSlot *slot = slot##Block->slots,                             \
                       *slot##End = slot + slot##Block->used;                  \
             slot < slot##End; slot++)

#endif
