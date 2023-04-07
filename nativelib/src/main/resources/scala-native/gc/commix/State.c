#if defined(SCALANATIVE_GC_COMMIX)

#include "State.h"

Heap heap = {};
BlockAllocator blockAllocator = {};
MutatorThreads mutatorThreads = NULL;
atomic_int_fast32_t mutatorThreadsCount = 0;
thread_local MutatorThread *currentMutatorThread = NULL;
safepoint_t scalanative_gc_safepoint = NULL;
GC_Roots *roots = NULL;

#endif
