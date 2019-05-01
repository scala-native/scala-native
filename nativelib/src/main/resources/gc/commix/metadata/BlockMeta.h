#ifndef IMMIX_BLOCKHEADER_H
#define IMMIX_BLOCKHEADER_H

#define LAST_HOLE -1

#include <stdint.h>
#include <string.h>
#include <stdbool.h>
#include "../GCTypes.h"
#include "../Constants.h"
#include "../Log.h"

#include <stdio.h>

#define FLAG_MASK 0x1F
#define INCREMENT_AGE 0x20

typedef enum {
    block_free = 0x0,
    block_simple = 0x1,
    block_superblock_start = 0x2,
    block_superblock_tail = 0x3,
    block_marked = 0x5,              // 0x4 | block_simple
    block_superblock_start_marked = 0x6,   // 0x4 | block_superblock_start
    block_superblock_tail_marked = 0x7,    // 0x4 | block_superblock_tail
    block_superblock_start_me = 0xb, // block_superblock_tail | 0x8
    block_coalesce_me = 0x13         // block_superblock_tail | 0x10
} BlockFlag;

typedef struct {
    union {
        struct {
            uint8_t flags;
            int8_t first;
        } simple;
        struct {
            uint8_t flags;
            int32_t size : BLOCK_COUNT_BITS;
        } superblock;
    } block;
#ifdef DEBUG_ASSERT
    int32_t nextBlock : BLOCK_COUNT_BITS;
    uint8_t debugFlag; // only for debugging
#else
    int32_t nextBlock;
#endif
} BlockMeta;

#ifdef DEBUG_ASSERT
typedef enum {
    dbg_must_sweep = 0x0,

    dbg_free = 0x1,
    dbg_partial_free = 0x2,
    dbg_not_free = 0x3,

    dbg_free_in_collection = 0x4,

    dbg_in_use = 0x5
} DebugFlag;
#endif

static inline int BlockMeta_GetFlags(BlockMeta *blockMeta) {
    return blockMeta->block.simple.flags & FLAG_MASK;
}

static inline bool BlockMeta_IsFree(BlockMeta *blockMeta) {
    // blockMeta->block.simple.flags == block_superblock_start ||
    // blockMeta->block.simple.flags == block_superblock_start_marked
    return BlockMeta_GetFlags(blockMeta) == block_free;
}
static inline bool BlockMeta_IsSimpleBlock(BlockMeta *blockMeta) {
    // blockMeta->block.simple.flags == block_simple ||
    // blockMeta->block.simple.flags == block_marked
    return (BlockMeta_GetFlags(blockMeta) & 0x3) == block_simple;
}
static inline bool BlockMeta_IsSuperblockStart(BlockMeta *blockMeta) {
    uint8_t flags = BlockMeta_GetFlags(blockMeta);
    return flags == block_superblock_start || flags == block_superblock_start_marked;
}
static inline bool BlockMeta_IsSuperblockTail(BlockMeta *blockMeta) {
    uint8_t flags = BlockMeta_GetFlags(blockMeta);
    return flags == block_superblock_tail || flags == block_superblock_tail_marked;
}
static inline bool BlockMeta_IsCoalesceMe(BlockMeta *blockMeta) {
    return BlockMeta_GetFlags(blockMeta) == block_coalesce_me;
}
static inline bool BlockMeta_IsSuperblockStartMe(BlockMeta *blockMeta) {
    return BlockMeta_GetFlags(blockMeta) == block_superblock_start_me;
}

static inline int BlockMeta_GetAge(BlockMeta *blockMeta) {
    return blockMeta->block.simple.flags >> 5;
}

static inline bool BlockMeta_IsOld(BlockMeta *blockMeta) {
    return BlockMeta_GetAge(blockMeta) == MAX_AGE_YOUNG_BLOCK;
}

static inline bool BlockMeta_IsOldSweep(BlockMeta *blockMeta) {
    uint8_t flags = blockMeta->block.simple.flags;
    uint8_t state = flags & FLAG_MASK;
    int age = flags >> 5;
    // Two cases :
    //  - The block is not marked. Then it is old if its age is the limit.
    //  - The block is marked, then it is old if its age is at least limit - 1. Since we unmark the block AFTER
    //    incrementing its age, it might be marked but have an age equal to the limit

    if (state == block_marked || state == block_superblock_start_marked || state == block_superblock_tail_marked) {
        // age == MAX_AGE_YOUNG_BLOCK || age == (MAX_AGE_YOUNG_BLOCK - 1);
        return age >= MAX_AGE_YOUNG_BLOCK - 1;
    } else {
        assert(state == block_simple || state == block_superblock_start || state == block_superblock_start_me || state == block_superblock_tail);
        return age == MAX_AGE_YOUNG_BLOCK;
    }
}

static inline void BlockMeta_IncrementAge(BlockMeta *blockMeta) {
    blockMeta->block.simple.flags += INCREMENT_AGE;
    assert(BlockMeta_GetAge(blockMeta) != 0);
}

static inline void BlockMeta_SetOld(BlockMeta *blockMeta) {
    blockMeta->block.simple.flags = (INCREMENT_AGE * MAX_AGE_YOUNG_BLOCK) | BlockMeta_GetFlags(blockMeta);
    assert(BlockMeta_IsOld(blockMeta));
}

static inline uint32_t BlockMeta_SuperblockSize(BlockMeta *blockMeta) {
    return blockMeta->block.superblock.size;
}

static inline bool BlockMeta_ContainsLargeObjects(BlockMeta *blockMeta) {
    return BlockMeta_IsSuperblockStart(blockMeta) ||
           BlockMeta_IsSuperblockTail(blockMeta);
}

static inline void BlockMeta_SetFlagAndSuperblockSize(BlockMeta *blockMeta,
                                                      BlockFlag blockFlag,
                                                      int32_t superblockSize) {
    assert(blockFlag != block_superblock_start || superblockSize > 0);
    assert(blockFlag != block_coalesce_me || superblockSize > 0);
    assert(blockFlag != block_simple);
    struct {
        uint8_t flags;
        int32_t size : BLOCK_COUNT_BITS;
    } combined;
    combined.flags = (blockMeta->block.superblock.flags & ~FLAG_MASK) | blockFlag;
    combined.size = superblockSize;

    *((int32_t *)&blockMeta->block.superblock) = *((int32_t *)&combined);
}

static inline void BlockMeta_SetFirstFreeLine(BlockMeta *blockMeta,
                                              int8_t freeLine) {
    assert(BlockMeta_IsSimpleBlock(blockMeta));
    assert(freeLine == LAST_HOLE || (freeLine >= 0 && freeLine < LINE_COUNT));
    blockMeta->block.simple.first = freeLine;
}

static inline int8_t BlockMeta_FirstFreeLine(BlockMeta *blockMeta) {
    assert(BlockMeta_IsSimpleBlock(blockMeta));

    return blockMeta->block.simple.first;
}

static inline void BlockMeta_SetFlag(BlockMeta *blockMeta,
                                     BlockFlag blockFlag) {
    blockMeta->block.simple.flags = (blockMeta->block.simple.flags & ~FLAG_MASK) | blockFlag;
}

static inline void BlockMeta_Clear(BlockMeta *blockMeta) {
    memset(blockMeta, 0, sizeof(BlockMeta));
}

static inline bool BlockMeta_IsMarked(BlockMeta *blockMeta) {
    uint8_t flags = BlockMeta_GetFlags(blockMeta);
    return flags == block_marked || flags == block_superblock_start_marked || flags == block_superblock_tail_marked;
}

static inline void BlockMeta_Unmark(BlockMeta *blockMeta) {
    blockMeta->block.simple.flags = (blockMeta->block.simple.flags & ~FLAG_MASK) | block_simple;
}

static inline void BlockMeta_Mark(BlockMeta *blockMeta) {
    blockMeta->block.simple.flags = (blockMeta->block.simple.flags & ~FLAG_MASK) | block_marked;
}

static inline void BlockMeta_UnmarkSuperblock(BlockMeta *blockMeta) {
    blockMeta->block.simple.flags = (blockMeta->block.simple.flags & ~FLAG_MASK) | block_superblock_start;
    uint32_t superblockSize = BlockMeta_SuperblockSize(blockMeta);
    if (superblockSize > 1) {
        BlockMeta *lastBlock = blockMeta + superblockSize - 1;
        lastBlock->block.simple.flags = (lastBlock->block.simple.flags & ~FLAG_MASK) | block_superblock_tail;
    }
}

static inline void BlockMeta_MarkSuperblock(BlockMeta *blockMeta) {
    blockMeta->block.simple.flags = (blockMeta->block.simple.flags & ~FLAG_MASK) | block_superblock_start_marked;
    uint32_t superblockSize = BlockMeta_SuperblockSize(blockMeta);
    if (superblockSize > 1) {
        BlockMeta *lastBlock = blockMeta + superblockSize - 1;
        lastBlock->block.simple.flags = (lastBlock->block.simple.flags & ~FLAG_MASK) | block_superblock_tail_marked;
    }
}

static inline bool BlockMeta_ShouldBeSwept(BlockMeta *blockMeta, bool collectingOld) {
    return (collectingOld && BlockMeta_IsOld(blockMeta)) || (!collectingOld && !BlockMeta_IsOld(blockMeta));
}

// Block specific

static inline word_t *Block_GetLineAddress(word_t *blockStart, int lineIndex) {
    assert(lineIndex >= 0);
    assert(lineIndex < LINE_COUNT);
    return blockStart + (WORDS_IN_LINE * lineIndex);
}

static inline word_t *Block_GetBlockEnd(word_t *blockStart) {
    return blockStart + (WORDS_IN_LINE * LINE_COUNT);
}

static inline uint32_t Block_GetLineIndexFromWord(word_t *blockStart,
                                                  word_t *word) {
    return (uint32_t)((word_t)word - (word_t)blockStart) >> LINE_SIZE_BITS;
}

static inline word_t *Block_GetLineWord(word_t *blockStart, int lineIndex,
                                        int wordIndex) {
    assert(wordIndex < WORDS_IN_LINE);
    return &Block_GetLineAddress(blockStart, lineIndex)[wordIndex];
}

static inline word_t *Block_GetBlockStartForWord(word_t *word) {
    return (word_t *)((word_t)word & BLOCK_SIZE_IN_BYTES_INVERSE_MASK);
}

static inline BlockMeta *BlockMeta_GetSuperblockStart(word_t *blockMetaStart,
                                                      BlockMeta *blockMeta) {
    BlockMeta *current = blockMeta;
    while (BlockMeta_IsSuperblockTail(current)) {
        current--;
        assert((word_t *)current >= blockMetaStart);
    }
    assert(BlockMeta_IsSuperblockStart(current));
    return current;
}

// Transitional Block<->BlockMeta
static inline uint32_t BlockMeta_GetBlockIndex(word_t *blockMetaStart,
                                               BlockMeta *blockMeta) {
    return blockMeta - (BlockMeta *)blockMetaStart;
}

static inline uint32_t Block_GetBlockIndexForWord(word_t *heapStart,
                                                  word_t *word) {
    word_t *blockStart = Block_GetBlockStartForWord(word);
    return (uint32_t)((blockStart - heapStart) / WORDS_IN_BLOCK);
}

static inline word_t *Block_GetStartFromIndex(word_t *heapStart,
                                              uint32_t index) {
    return heapStart + (WORDS_IN_BLOCK * index);
}

static inline word_t *BlockMeta_GetBlockStart(word_t *blockMetaStart,
                                              word_t *heapStart,
                                              BlockMeta *blockMeta) {
    uint32_t index = BlockMeta_GetBlockIndex(blockMetaStart, blockMeta);
    return Block_GetStartFromIndex(heapStart, index);
}

static inline BlockMeta *BlockMeta_GetFromIndex(word_t *blockMetaStart,
                                                uint32_t index) {
    return (BlockMeta *)blockMetaStart + index;
}

static inline BlockMeta *Block_GetBlockMeta(word_t *blockMetaStart,
                                            word_t *heapStart, word_t *word) {
    uint32_t index = Block_GetBlockIndexForWord(heapStart, word);
    return BlockMeta_GetFromIndex(blockMetaStart, index);
}
#endif // IMMIX_BLOCKHEADER_H
