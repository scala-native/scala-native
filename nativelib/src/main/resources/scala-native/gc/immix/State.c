#include "State.h"

Heap heap = {};
Stack stack = {};
Stack weakRefStack = {};
BlockAllocator blockAllocator = {};
MutatorThreads mutatorThreads = NULL;
thread_local MutatorThread *currentMutatorThread = NULL;
safepoint_t scalanative_gc_safepoint = NULL;
GC_Roots *roots = NULL;
