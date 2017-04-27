#include "Line.h"
#include "headers/BlockHeader.h"


inline ObjectHeader* line_header_getFirstObject(LineHeader* lineHeader) {
    assert(line_header_containsObject(lineHeader));
    BlockHeader* blockHeader = block_blockHeaderFromLineHeader(lineHeader);
    uint8_t offset = line_header_getFirstObjectOffset(lineHeader);

    uint32_t lineIndex = block_getLineIndexFromLineHeader(blockHeader, lineHeader);

    return (ObjectHeader*)block_getLineWord(blockHeader, lineIndex, offset / WORD_SIZE);

}

inline void line_header_update(BlockHeader* blockHeader, word_t* objectStart) {

    int lineIndex = block_getLineIndexFromWord(blockHeader, objectStart);
    LineHeader* lineHeader = &blockHeader->lineHeaders[lineIndex];


    if(!line_header_containsObject(lineHeader)) {
        uint8_t offset = (uint8_t)((word_t)objectStart & LINE_SIZE_MASK);

        line_header_setOffset(lineHeader, offset);
    }
}


