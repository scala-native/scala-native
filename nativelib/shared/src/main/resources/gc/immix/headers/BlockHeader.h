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

static inline bool Block_IsRecyclable(BlockHeader *blockHeader) {
    return blockHeader->header.flags == block_recyclable;
}
static inline bool Block_IsUnavailable(BlockHeader *blockHeader) {
    return blockHeader->header.flags == block_unavailable;
}
static inline bool Block_IsFree(BlockHeader *blockHeader) {
    return blockHeader->header.flags == block_free;
}
static inline void Block_SetFlag(BlockHeader *blockHeader,
                                 BlockFlag blockFlag) {
    blockHeader->header.flags = blockFlag;
}

static inline bool Block_IsMarked(BlockHeader *blockHeader) {
    return blockHeader->header.mark == 1;
}

static inline void Block_Unmark(BlockHeader *blockHeader) {
    blockHeader->header.mark = 0;
}

static inline void Block_Mark(BlockHeader *blockHeader) {
    blockHeader->header.mark = 1;
}

static inline BlockHeader *Block_GetBlockHeader(word_t *word) {
    return (BlockHeader *)((word_t)word & BLOCK_SIZE_IN_BYTES_INVERSE_MASK);
}

static inline word_t *Block_GetLineAddress(BlockHeader *blockHeader,
                                           int lineIndex) {
    assert(lineIndex < LINE_COUNT);
    return (word_t *)((ubyte_t *)blockHeader + BLOCK_METADATA_ALIGNED_SIZE +
                      (lineIndex * LINE_SIZE));
}

static inline word_t *Block_GetLineWord(BlockHeader *blockHeader, int lineIndex,
                                        int wordIndex) {
    assert(wordIndex < WORDS_IN_LINE);
    return &Block_GetLineAddress(blockHeader, lineIndex)[wordIndex];
}

static inline FreeLineHeader *Block_GetFreeLineHeader(BlockHeader *blockHeader,
                                                      int lineIndex) {
    return (FreeLineHeader *)Block_GetLineAddress(blockHeader, lineIndex);
}

static inline BlockHeader *
Block_BlockHeaderFromLineHeader(LineHeader *lineHeader) {
    return Block_GetBlockHeader((word_t *)lineHeader);
}

static inline word_t *Block_GetFirstWord(BlockHeader *blockHeader) {
    return (word_t *)((ubyte_t *)blockHeader + BLOCK_METADATA_ALIGNED_SIZE);
}

static inline word_t *Block_GetBlockEnd(BlockHeader *blockHeader) {
    return Block_GetFirstWord(blockHeader) + (WORDS_IN_LINE * LINE_COUNT);
}

static inline uint32_t
Block_GetLineIndexFromLineHeader(BlockHeader *blockHeader,
                                 LineHeader *lineHeader) {
    return (uint32_t)(lineHeader - blockHeader->lineHeaders);
}

static inline uint32_t Block_GetLineIndexFromWord(BlockHeader *blockHeader,
                                                  word_t *word) {
    word_t *firstWord = Block_GetFirstWord(blockHeader);
    return (uint32_t)((word_t)word - (word_t)firstWord) >> LINE_SIZE_BITS;
}

static inline LineHeader *Block_GetLineHeader(BlockHeader *blockHeader,
                                              int lineIndex) {
    return &blockHeader->lineHeaders[lineIndex];
}

#endif // IMMIX_BLOCKHEADER_H
