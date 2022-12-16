#ifndef IMMIX_STATE_H
#define IMMIX_STATE_H

#include "Heap.h"
#include "Allocator.h"
#include "LargeAllocator.h"
#include "BlockAllocator.h"
#include "GCRoots.h"

extern Heap heap;
extern Allocator allocator;
extern LargeAllocator largeAllocator;
extern BlockAllocator blockAllocator;
extern GC_Roots *roots;

#endif // IMMIX_STATE_H
