#ifndef IMMIX_STATE_H
#define IMMIX_STATE_H

#include "Heap.h"
#include "BlockAllocator.h"
#include "shared/ThreadUtil.h"
#include "MutatorThread.h"
#include "immix_commix/GCRoots.h"

extern Heap heap;
extern BlockAllocator blockAllocator;
extern _Atomic(MutatorThreads) mutatorThreads;
extern atomic_int_fast32_t mutatorThreadsCount;
extern thread_local MutatorThread *currentMutatorThread;
extern GC_Roots *roots;

#endif // IMMIX_STATE_H
