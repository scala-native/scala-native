#ifndef IMMIX_STATE_H
#define IMMIX_STATE_H

#include "Heap.h"
#include "stddef.h"

typedef struct GC_Root {
    void *start;
    void* limit;
} GC_Root;

struct GC_Roots{
  GC_Root* node;
  struct GC_Roots* next;
};
typedef struct GC_Roots GC_Roots;

extern Heap heap;
extern Stack stack;
extern Stack weakRefStack;
extern Allocator allocator;
extern LargeAllocator largeAllocator;
extern BlockAllocator blockAllocator;
extern GC_Roots* gcRoots;


#endif // IMMIX_STATE_H
