#ifndef IMMIX_BLOCKHEADER_H
#define IMMIX_BLOCKHEADER_H

#define LAST_HOLE -1

#include <stdint.h>
#include <string.h>
#include "LineMeta.h"
#include "../GCTypes.h"
#include "../Constants.h"
#include "../Log.h"

typedef enum {
    block_free = 0x0,
    block_simple = 0x1,
    block_superblock_start = 0x2,
    block_superblock_tail = 0x3,
    block_marked = 0x5,              // 0x4 | block_simple
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

static inline bool BlockMeta_IsFree(BlockMeta *blockMeta) {
    return blockMeta->block.simple.flags == block_free;
}
static inline bool BlockMeta_IsSimpleBlock(BlockMeta *blockMeta) {
    // blockMeta->block.simple.flags == block_simple ||
    // blockMeta->block.simple.flags == block_marked
    return (blockMeta->block.simple.flags & 0x3) == block_simple;
}
static inline bool BlockMeta_IsSuperblockStart(BlockMeta *blockMeta) {
    return blockMeta->block.simple.flags == block_superblock_start;
}
static inline bool BlockMeta_IsSuperblockTail(BlockMeta *blockMeta) {
    return blockMeta->block.simple.flags == block_superblock_tail;
}
static inline bool BlockMeta_IsCoalesceMe(BlockMeta *blockMeta) {
    return blockMeta->block.simple.flags == block_coalesce_me;
}
static inline bool BlockMeta_IsSuperblockStartMe(BlockMeta *blockMeta) {
    return blockMeta->block.simple.flags == block_superblock_start_me;
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
    combined.flags = blockFlag;
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
    blockMeta->block.simple.flags = blockFlag;
}

static inline void BlockMeta_Clear(BlockMeta *blockMeta) {
    memset(blockMeta, 0, sizeof(BlockMeta));
}

static inline bool BlockMeta_IsMarked(BlockMeta *blockMeta) {
    return blockMeta->block.simple.flags == block_marked;
}

static inline void BlockMeta_Unmark(BlockMeta *blockMeta) {
    blockMeta->block.simple.flags = block_simple;
}

static inline void BlockMeta_Mark(BlockMeta *blockMeta) {
    blockMeta->block.simple.flags = block_marked;
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
