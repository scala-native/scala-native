#ifndef IMMIX_BLOCKRANGE_H
#define IMMIX_BLOCKRANGE_H

#include "../GCTypes.h"
#include "../Constants.h"
#include <stdbool.h>
#include <stdatomic.h>

/*
    Combines two 32 bit values in a single 64 bit atomic number.
    This enables atomic updates on the block range.
*/

typedef atomic_uint_fast64_t BlockRange;
typedef uint_fast64_t BlockRangeVal;

#define NO_BLOCK_INDEX (~((uint32_t)0))
// first = limit = 0
// size = 0
#define EMPTY_RANGE 0L

static inline BlockRangeVal BlockRange_Pack(uint32_t first, uint32_t limit) {
    return ((uint64_t)limit << 32) | (uint64_t)first;
}

static inline uint32_t BlockRange_First(BlockRangeVal blockRange) {
    return (uint32_t)(blockRange);
}

static inline uint32_t BlockRange_Limit(BlockRangeVal blockRange) {
    return (uint32_t)(blockRange >> 32);
}

static inline void BlockRange_Clear(BlockRange *blockRange) {
    *blockRange = EMPTY_RANGE;
}

static inline bool BlockRange_IsEmpty(BlockRangeVal blockRange) {
    return BlockRange_First(blockRange) >= BlockRange_Limit(blockRange);
}

static inline BlockRangeVal
BlockRange_AppendLastOrReplace(BlockRange *blockRange, uint32_t first,
                               uint32_t count) {
    BlockRangeVal old = *blockRange;
    BlockRangeVal newValue;
    BlockRangeVal returnVal = EMPTY_RANGE;
    do {
        // old will be replaced with actual value if
        // atomic_compare_exchange_strong fails
        if (BlockRange_IsEmpty(old)) {
            newValue = BlockRange_Pack(first, first + count);
            returnVal = EMPTY_RANGE;
        } else if (BlockRange_Limit(old) == first) {
            newValue = BlockRange_Pack(BlockRange_First(old), first + count);
            returnVal = EMPTY_RANGE;
        } else {
            newValue = BlockRange_Pack(first, first + count);
            returnVal = old;
        }
    } while (!atomic_compare_exchange_strong(blockRange, (BlockRangeVal *)&old,
                                             newValue));
    return returnVal;
}

static inline uint32_t BlockRange_Size(BlockRangeVal blockRange) {
    if (BlockRange_IsEmpty(blockRange)) {
        return 0;
    } else {
        return BlockRange_Limit(blockRange) - BlockRange_First(blockRange);
    }
}

static inline uint32_t BlockRange_PollFirst(BlockRange *blockRange,
                                            uint32_t count) {
    BlockRangeVal old = *blockRange;
    uint32_t first;
    BlockRangeVal newValue;
    do {
        // old will be replaced with actual value if
        // atomic_compare_exchange_strong fails
        first = BlockRange_First(old);
        uint32_t limit = BlockRange_Limit(old);
        if (BlockRange_Size(old) < count) {
            return NO_BLOCK_INDEX;
        }
        newValue = BlockRange_Pack(first + count, limit);
    } while (!atomic_compare_exchange_strong(blockRange, (BlockRangeVal *)&old,
                                             newValue));
    return first;
}

#endif // IMMIX_BLOCKRANGE_H