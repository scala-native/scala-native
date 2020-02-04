#include "State.h"

Heap heap;
Stack stack;
Allocator allocator;
LargeAllocator largeAllocator;
BlockAllocator blockAllocator;
ThreadList *threadList;
pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
void *suspendingThreadStackTop;
Semaphore semaphore;
