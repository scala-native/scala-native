#include "State.h"

Heap heap = {};
Allocator allocator = {};
LargeAllocator largeAllocator = {};
BlockAllocator blockAllocator = {};
GC_Roots *roots = NULL;
