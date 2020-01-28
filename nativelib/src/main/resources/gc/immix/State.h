#ifndef IMMIX_STATE_H
#define IMMIX_STATE_H

#include "Heap.h"
#include "datastructures/ThreadList.h"

extern Heap heap;
extern Stack stack;
extern Allocator allocator;
extern LargeAllocator largeAllocator;
extern BlockAllocator blockAllocator;
ThreadList *threadList;
pthread_mutex_t mutex;

#endif // IMMIX_STATE_H
