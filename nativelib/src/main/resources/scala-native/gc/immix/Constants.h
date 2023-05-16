#ifndef IMMIX_CONSTANTS_H
#define IMMIX_CONSTANTS_H

#include "CommonConstants.h"
#include "metadata/BlockMeta.h"

#define METADATA_PER_BLOCK                                                     \
    (sizeof(BlockMeta) + LINE_COUNT * LINE_METADATA_SIZE +                     \
     WORDS_IN_BLOCK / ALLOCATION_ALIGNMENT_WORDS)
#define SPACE_USED_PER_BLOCK (BLOCK_TOTAL_SIZE + METADATA_PER_BLOCK)
#define MAX_HEAP_SIZE ((uint64_t)SPACE_USED_PER_BLOCK * MAX_BLOCK_COUNT)

#define MIN_HEAP_SIZE (1 * 1024 * 1024ULL)
#define DEFAULT_MIN_HEAP_SIZE (128 * SPACE_USED_PER_BLOCK)
#define UNLIMITED_HEAP_SIZE (~((size_t)0))

#define STATS_MEASUREMENTS 100

#endif // IMMIX_CONSTANTS_H
