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

static inline bool Block_isRecyclable(BlockHeader *blockHeader) {
    return blockHeader->header.flags == block_recyclable;
}
static inline bool Block_isUnavailable(BlockHeader *blockHeader) {
    return blockHeader->header.flags == block_unavailable;
}
static inline bool Block_isFree(BlockHeader *blockHeader) {
    return blockHeader->header.flags == block_free;
}
static inline void Block_setFlag(BlockHeader *blockHeader, BlockFlag blockFlag) {
    blockHeader->header.flags = blockFlag;
}

static inline bool Block_isMarked(BlockHeader *blockHeader) {
    return blockHeader->header.mark == 1;
}

static inline void Block_unmark(BlockHeader *blockHeader) {
    blockHeader->header.mark = 0;
}

static inline void Block_mark(BlockHeader *blockHeader) {
    blockHeader->header.mark = 1;
}

static inline BlockHeader* Block_getBlockHeader(word_t *word) {
    return (BlockHeader*)((word_t)word & BLOCK_SIZE_IN_BYTES_INVERSE_MASK);

}

static inline word_t* Block_getLineAddress(BlockHeader *blockHeader, int lineIndex) {
    assert(lineIndex < LINE_COUNT);
    return (word_t*)((ubyte_t*)blockHeader + BLOCK_METADATA_ALIGNED_SIZE + (lineIndex * LINE_SIZE));
}

static inline word_t* Block_getLineWord(BlockHeader *blockHeader, int lineIndex, int wordIndex) {
    assert(wordIndex < WORDS_IN_LINE);
    return &Block_getLineAddress(blockHeader, lineIndex)[wordIndex];
}


static inline FreeLineHeader* Block_getFreeLineHeader(BlockHeader *blockHeader, int lineIndex) {
    return (FreeLineHeader*) Block_getLineAddress(blockHeader, lineIndex);
}


static inline BlockHeader* Block_blockHeaderFromLineHeader(LineHeader *lineHeader) {
    return Block_getBlockHeader((word_t *) lineHeader);
}

static inline word_t* Block_getFirstWord(BlockHeader *blockHeader) {
    return (word_t*)((ubyte_t*)blockHeader + BLOCK_METADATA_ALIGNED_SIZE);
}


static inline word_t* Block_getBlockEnd(BlockHeader *blockHeader) {
    return Block_getFirstWord(blockHeader) + (WORDS_IN_LINE * LINE_COUNT);
}

static inline uint32_t Block_getLineIndexFromLineHeader(BlockHeader *blockHeader, LineHeader *lineHeader) {
    return (uint32_t) (lineHeader - blockHeader->lineHeaders);
}

static inline uint32_t Block_getLineIndexFromWord(BlockHeader *blockHeader, word_t *word) {
    word_t* firstWord = Block_getFirstWord(blockHeader);
    return (uint32_t)((word_t)word - (word_t)firstWord) >> LINE_SIZE_BITS;
}

static inline LineHeader* Block_getLineHeader(BlockHeader *blockHeader, int lineIndex) {
    return &blockHeader->lineHeaders[lineIndex];
}


#endif //IMMIX_BLOCKHEADER_H
