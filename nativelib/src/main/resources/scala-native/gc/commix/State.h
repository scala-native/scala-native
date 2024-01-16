#ifndef IMMIX_STATE_H
#define IMMIX_STATE_H

#include "Heap.h"
#include "BlockAllocator.h"
#include "shared/ThreadUtil.h"
#include "MutatorThread.h"
#include "immix_commix/GCRoots.h"

extern Heap heap;
extern BlockAllocator blockAllocator;
extern MutatorThreads mutatorThreads;
extern atomic_int_fast32_t mutatorThreadsCount;
extern SN_ThreadLocal MutatorThread *currentMutatorThread;
extern GC_Roots *customRoots;

#ifdef SCALANATIVE_MULTITHREADING_ENABLED
#include "GCThread.h"
extern struct GCWeakRefsHandlerThread *weakRefsHandlerThread;
#endif

#endif // IMMIX_STATE_H
