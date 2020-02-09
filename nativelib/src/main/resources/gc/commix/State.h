#ifndef IMMIX_STATE_H
#define IMMIX_STATE_H

#include "Heap.h"
#include "Allocator.h"
#include "LargeAllocator.h"
#include "BlockAllocator.h"
#include "datastructures/ThreadList.h"
#include "semaphore/Semaphore.h"

extern Heap heap;
extern Allocator allocator;
extern LargeAllocator largeAllocator;
extern BlockAllocator blockAllocator;
extern ThreadList *threadList;
extern pthread_mutex_t mutex;
extern void *suspendingThreadStackTop;
extern Semaphore semaphore;

#endif // IMMIX_STATE_H
