#ifndef IMMIX_LINE_H
#define IMMIX_LINE_H

#include "headers/ObjectHeader.h"
#include "headers/LineHeader.h"
#include "Block.h"

static INLINE Object *Line_GetFirstObject(Bytemap *bytemap, word_t *blockStart, uint32_t lineIndex) {
    word_t *lineStart = Block_GetLineAddress(blockStart, lineIndex);
    word_t *lineEnd = lineStart + WORDS_IN_LINE;
    word_t *current = lineStart;
    while(current < lineEnd) {
        if (!Bytemap_IsFree(bytemap, current)) {
            return (Object *) current;
        }
        current += 1; /* 1 WORD*/
    }

    return NULL;
}

#endif // IMMIX_LINE_H
