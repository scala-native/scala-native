#if defined(SCALANATIVE_GC_COMMIX)

#include "State.h"

Heap heap = {};
BlockAllocator blockAllocator = {};
MutatorThreads mutatorThreads = NULL;
thread_local MutatorThread *currentMutatorThread = NULL;
safepoint_t scalanative_gc_safepoint = NULL;
GC_Roots *roots = NULL;

#endif
