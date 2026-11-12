#include "shared/SuspendedStacks.h"

StackSlotBlock *scalanative_GC_stackSlots = NULL;
void *scalanative_GC_suspendedStacksRtti = NULL;
atomic_uint_fast64_t scalanative_GC_markCycle = 0;
atomic_uint_fast64_t scalanative_GC_completedMarkCycle = 0;
