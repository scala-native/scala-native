#include "State.h"

Heap heap = {};
Stack stack = {};
Stack weakRefStack = {};
Allocator allocator = {};
LargeAllocator largeAllocator = {};
BlockAllocator blockAllocator = {};
GC_Roots* gcRoots = NULL;
