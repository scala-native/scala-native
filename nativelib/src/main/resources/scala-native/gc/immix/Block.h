#ifndef IMMIX_BLOCK_H
#define IMMIX_BLOCK_H

#include "immix_commix/metadata/BlockMeta.h"
#include "Allocator.h"

extern uint64_t Block_recycledFreeLines;

// A minor collection (full is false) of Sticky Immix skips the blocks nothing
// was allocated into since they were last swept.
void Block_Recycle(Allocator *allocator, BlockMeta *block, word_t *blockStart,
                   LineMeta *lineMetas, bool full);
#endif // IMMIX_BLOCK_H
