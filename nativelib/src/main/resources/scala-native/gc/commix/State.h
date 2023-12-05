#ifndef IMMIX_STATE_H
#define IMMIX_STATE_H

#include "Heap.h"
#include "BlockAllocator.h"
#include "shared/ThreadUtil.h"
#include "MutatorThread.h"
#include "shared/Safepoint.h"
#include "immix_commix/GCRoots.h"

extern Heap heap;
extern BlockAllocator blockAllocator;
extern MutatorThreads mutatorThreads;
extern atomic_int_fast32_t mutatorThreadsCount;
extern thread_local MutatorThread *currentMutatorThread;
extern safepoint_t scalanative_gc_safepoint;
extern GC_Roots *roots;

#endif // IMMIX_STATE_H
