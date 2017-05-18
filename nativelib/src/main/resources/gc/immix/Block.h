#ifndef IMMIX_BLOCK_H
#define IMMIX_BLOCK_H

#include "headers/BlockHeader.h"
#include "Heap.h"
#include "Line.h"

#define LAST_HOLE -1


void Block_recycle(Allocator *, BlockHeader *);
void Block_print(BlockHeader* block);

#endif //IMMIX_BLOCK_H
