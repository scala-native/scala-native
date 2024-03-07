#ifndef IMMIX_STATE_H
#define IMMIX_STATE_H

#include "Heap.h"
#include "shared/ThreadUtil.h"
#include "MutatorThread.h"
#include "immix_commix/GCRoots.h"
#include "stddef.h"

extern Heap heap;
extern Stack stack;
extern Stack weakRefStack;
extern BlockAllocator blockAllocator;
extern _Atomic(MutatorThreads) mutatorThreads;
extern SN_ThreadLocal MutatorThread *currentMutatorThread;
extern GC_Roots *customRoots;

#endif // IMMIX_STATE_H
