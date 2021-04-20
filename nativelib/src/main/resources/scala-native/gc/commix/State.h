#ifndef IMMIX_STATE_H
#define IMMIX_STATE_H

#include "Heap.h"
#include "Allocator.h"
#include "LargeAllocator.h"
#include "BlockAllocator.h"
#include "ThreadManager.h"

extern Heap heap;
extern Allocator allocator;
extern LargeAllocator largeAllocator;
extern BlockAllocator blockAllocator;
extern ThreadManager threadManager;

#endif // IMMIX_STATE_H
