#if defined(SCALANATIVE_GC_IMMIX)

#include "State.h"

Heap heap = {};
Stack stack = {};
Stack weakRefStack = {};
BlockAllocator blockAllocator = {};
_Atomic(MutatorThreads) mutatorThreads = NULL;
thread_local MutatorThread *currentMutatorThread = NULL;
safepoint_t scalanative_gc_safepoint = NULL;
GC_Roots *roots = NULL;

#endif
