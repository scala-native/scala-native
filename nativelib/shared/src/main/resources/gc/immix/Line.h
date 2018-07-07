#ifndef IMMIX_LINE_H
#define IMMIX_LINE_H

#include "headers/ObjectHeader.h"
#include "headers/LineHeader.h"
#include "headers/BlockHeader.h"

static INLINE Object *Line_GetFirstObject(LineHeader *lineHeader) {
    assert(Line_ContainsObject(lineHeader));
    BlockHeader *blockHeader = Block_BlockHeaderFromLineHeader(lineHeader);
    uint8_t offset = Line_GetFirstObjectOffset(lineHeader);

    uint32_t lineIndex =
        Block_GetLineIndexFromLineHeader(blockHeader, lineHeader);

    return (Object *)Block_GetLineWord(blockHeader, lineIndex,
                                       offset / WORD_SIZE);
}

static INLINE void Line_Update(BlockHeader *blockHeader, word_t *objectStart) {

    int lineIndex = Block_GetLineIndexFromWord(blockHeader, objectStart);
    LineHeader *lineHeader = Block_GetLineHeader(blockHeader, lineIndex);

    if (!Line_ContainsObject(lineHeader)) {
        uint8_t offset = (uint8_t)((word_t)objectStart & LINE_SIZE_MASK);

        Line_SetOffset(lineHeader, offset);
    }
}

#endif // IMMIX_LINE_H
