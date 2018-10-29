#ifndef IMMIX_BLOCKHEADER_H
#define IMMIX_BLOCKHEADER_H

#define LAST_HOLE -1

#include <stdint.h>
#include "LineMeta.h"
#include "../GCTypes.h"
#include "../Constants.h"
#include "../Log.h"

typedef enum {
    block_free = 0x0,
    block_simple = 0x1,
    block_superblock_start = 0x2,
    block_superblock_middle = 0x3,
    block_marked = 0x5
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
    int32_t nextBlock;
} BlockMeta;

static inline bool BlockMeta_IsFree(BlockMeta *blockMeta) {
    return blockMeta->block.simple.flags == block_free;
}
static inline bool BlockMeta_IsSimpleBlock(BlockMeta *blockMeta) {
    return (blockMeta->block.simple.flags & block_simple) != 0;
}
static inline bool BlockMeta_IsSuperblockStart(BlockMeta *blockMeta) {
    return blockMeta->block.simple.flags == block_superblock_start;
}
static inline bool BlockMeta_IsSuperblockMiddle(BlockMeta *blockMeta) {
    return blockMeta->block.simple.flags == block_superblock_middle;
}

static inline uint32_t BlockMeta_SuperblockSize(BlockMeta *blockMeta) {
    return blockMeta->block.superblock.size;
}

static inline bool BlockMeta_ContainsLargeObjects(BlockMeta *blockMeta) {
    return BlockMeta_IsSuperblockStart(blockMeta) ||
           BlockMeta_IsSuperblockMiddle(blockMeta);
}

static inline void BlockMeta_SetSuperblockSize(BlockMeta *blockMeta,
                                               int32_t superblockSize) {
    assert(!BlockMeta_IsSuperblockStart(blockMeta) || superblockSize > 0);
    assert(!BlockMeta_IsSimpleBlock(blockMeta));

    blockMeta->block.superblock.size = superblockSize;
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
    while (BlockMeta_IsSuperblockMiddle(current)) {
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

static inline word_t *BlockMeta_GetBlockStart(word_t *blockMetaStart,
                                              word_t *heapStart,
                                              BlockMeta *blockMeta) {
    uint32_t index = BlockMeta_GetBlockIndex(blockMetaStart, blockMeta);
    return heapStart + (WORDS_IN_BLOCK * index);
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
