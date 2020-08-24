#ifndef IMMIX_STATE_H
#define IMMIX_STATE_H

#include "Heap.h"
#include "ThreadManager.h"

extern Heap heap;
extern Stack stack;
extern Allocator allocator;
extern LargeAllocator largeAllocator;
extern BlockAllocator blockAllocator;
extern ThreadManager threadManager;

#endif // IMMIX_STATE_H
