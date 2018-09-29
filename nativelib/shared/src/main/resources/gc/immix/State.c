#include "State.h"

Heap heap;
Stack stack;
Allocator allocator;
LargeAllocator largeAllocator;

// For stackoverflow handling
bool overflow = false;
word_t *currentOverflowAddress = NULL;
