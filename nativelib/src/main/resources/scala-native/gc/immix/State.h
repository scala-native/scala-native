#ifndef IMMIX_STATE_H
#define IMMIX_STATE_H

#include "Heap.h"
#include "ThreadUtil.h"
#include "MutatorThread.h"
#include "Safepoint.h"

extern Heap heap;
extern Stack stack;
extern Stack weakRefStack;
extern BlockAllocator blockAllocator;
extern MutatorThreads mutatorThreads;
extern thread_local MutatorThread *currentMutatorThread;
extern safepoint_t scalanative_gc_safepoint;

#endif // IMMIX_STATE_H
