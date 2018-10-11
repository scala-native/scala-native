#ifndef IMMIX_BLOCKHEADER_H
#define IMMIX_BLOCKHEADER_H

#include <stdint.h>
#include "LineHeader.h"
#include "../GCTypes.h"
#include "../Constants.h"
#include "../Log.h"

typedef enum {
    block_free = 0x0,
    block_recyclable = 0x1,
    block_unavailable = 0x2
} BlockFlag;

typedef struct {
    struct {
        uint8_t mark;
        uint8_t flags;
        int16_t first;
        int32_t nextBlock;
    } header;
    LineHeader lineHeaders[LINE_COUNT];
} BlockHeader;

static inline bool BlockHeader_IsRecyclable(BlockHeader *blockHeader) {
    return blockHeader->header.flags == block_recyclable;
}
static inline bool BlockHeader_IsUnavailable(BlockHeader *blockHeader) {
    return blockHeader->header.flags == block_unavailable;
}
static inline bool BlockHeader_IsFree(BlockHeader *blockHeader) {
    return blockHeader->header.flags == block_free;
}

static inline void BlockHeader_SetFlag(BlockHeader *blockHeader,
                                 BlockFlag blockFlag) {
    blockHeader->header.flags = blockFlag;
}

static inline bool BlockHeader_IsMarked(BlockHeader *blockHeader) {
    return blockHeader->header.mark == 1;
}

static inline void BlockHeader_Unmark(BlockHeader *blockHeader) {
    blockHeader->header.mark = 0;
}

static inline void BlockHeader_Mark(BlockHeader *blockHeader) {
    blockHeader->header.mark = 1;
}

static inline uint32_t
BlockHeader_GetLineIndexFromLineHeader(BlockHeader *blockHeader,
                                       LineHeader *lineHeader) {
    return (uint32_t)(lineHeader - blockHeader->lineHeaders);
}

static inline LineHeader *BlockHeader_GetLineHeader(BlockHeader *blockHeader,
                                              int lineIndex) {
    return &blockHeader->lineHeaders[lineIndex];
}

// Block specific

static inline word_t *Block_GetLineAddress(word_t *blockStart, int lineIndex) {
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

static inline FreeLineHeader *Block_GetFreeLineHeader(word_t *blockStart,
                                                      int lineIndex) {
    return (FreeLineHeader *)Block_GetLineAddress(blockStart, lineIndex);
}

static inline word_t * Block_GetBlockStartForWord(word_t *word) {
    return (word_t *)((word_t)word & BLOCK_SIZE_IN_BYTES_INVERSE_MASK);
}

// Transitional Block<->BlockHeader
static inline uint32_t BlockHeader_GetBlockIndex(word_t *blockHeaderStart, BlockHeader *blockHeader) {
    return (uint32_t)((word_t *)blockHeader - blockHeaderStart) / WORDS_IN_BLOCK_METADATA;
}

static inline uint32_t Block_GetBlockIndexForWord(word_t* heapStart, word_t *word) {
    word_t *blockStart = Block_GetBlockStartForWord(word);
    return (uint32_t) ((blockStart - heapStart) / WORDS_IN_BLOCK);
}

static inline word_t *BlockHeader_GetBlockStart(word_t *blockHeaderStart, word_t *heapStart, BlockHeader *blockHeader) {
    uint32_t index = BlockHeader_GetBlockIndex(blockHeaderStart, blockHeader);
    return heapStart + (WORDS_IN_BLOCK * index);
}

static inline BlockHeader *BlockHeader_GetFromIndex(word_t *blockHeaderStart, uint32_t index) {
    return (BlockHeader *)(blockHeaderStart + (index * WORDS_IN_BLOCK_METADATA));
}

static inline BlockHeader *Block_GetBlockHeader(word_t *blockHeaderStart, word_t *heapStart, word_t *word) {
    uint32_t index = Block_GetBlockIndexForWord(heapStart, word);
    return BlockHeader_GetFromIndex(blockHeaderStart, index);
}


#endif // IMMIX_BLOCKHEADER_H
