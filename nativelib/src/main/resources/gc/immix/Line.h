#ifndef IMMIX_LINE_H
#define IMMIX_LINE_H


#include "headers/ObjectHeader.h"
#include "headers/LineHeader.h"
#include "headers/BlockHeader.h"

static INLINE ObjectHeader* Line_getFirstObject(LineHeader *lineHeader) {
    assert(Line_containsObject(lineHeader));
    BlockHeader* blockHeader = Block_blockHeaderFromLineHeader(lineHeader);
    uint8_t offset = Line_getFirstObjectOffset(lineHeader);

    uint32_t lineIndex = Block_getLineIndexFromLineHeader(blockHeader, lineHeader);

    return (ObjectHeader*) Block_getLineWord(blockHeader, lineIndex, offset / WORD_SIZE);

}

static INLINE void Line_update(BlockHeader *blockHeader, word_t *objectStart) {

    int lineIndex = Block_getLineIndexFromWord(blockHeader, objectStart);
    LineHeader* lineHeader = Block_getLineHeader(blockHeader, lineIndex);


    if(!Line_containsObject(lineHeader)) {
        uint8_t offset = (uint8_t)((word_t)objectStart & LINE_SIZE_MASK);

        Line_setOffset(lineHeader, offset);
    }
}

#endif //IMMIX_LINE_H
