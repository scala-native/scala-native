#include <stddef.h>
#include <stdio.h>
#include "Object.h"
#include "headers/BlockHeader.h"
#include "Line.h"
#include "Log.h"

ObjectHeader* objectNextLargeObject(ObjectHeader* objectHeader) {
    size_t size = object_chunkSize(objectHeader);
    assert(size != 0);
    return (ObjectHeader*)((ubyte_t *)objectHeader + size);
}

ObjectHeader* object_nextObject(ObjectHeader *objectHeader) {
    size_t size = object_size(objectHeader);
    assert(size < LARGE_BLOCK_SIZE);
    if(size == 0) {
        return NULL;
    }
    ObjectHeader* next = (ObjectHeader*)((ubyte_t *)objectHeader + size);
    assert(block_getBlockHeader((word_t*)next) == block_getBlockHeader((word_t*)objectHeader)
           || (ubyte_t*)block_getBlockHeader((word_t*)next) ==
                                 (ubyte_t*)block_getBlockHeader((word_t*)objectHeader) + BLOCK_TOTAL_SIZE);
    return next;
}

static inline bool isWordAligned(word_t* word) {
    return ((word_t)word & WORD_INVERSE_MASK) == (word_t)word;
}


ObjectHeader* getInLine(BlockHeader* blockHeader, int lineIndex, word_t* word) {
    assert(line_header_containsObject(&blockHeader->lineHeaders[lineIndex]));

    ObjectHeader* current = line_header_getFirstObject(&blockHeader->lineHeaders[lineIndex]);
    ObjectHeader* next = object_nextObject(current);

    word_t* lineEnd = block_getLineAddress(blockHeader, lineIndex) + WORDS_IN_LINE;

    while(next != NULL && (word_t*) next < lineEnd && (word_t*)next <= word) {
        current = next;
        next = object_nextObject(next);
    }

    if(object_isAllocated(current) && word >= (word_t*)current && word < (word_t*)next) {
#ifdef DEBUG_PRINT
        if((word_t*)current != word) {
            printf("inner pointer: %p object: %p\n", word, current);
            fflush(stdout);
        }
#endif
        return current;
    } else {
        return NULL;
    }

}

ObjectHeader* object_getObject(word_t* word) {
    BlockHeader* blockHeader = block_getBlockHeader(word);

    //Check if the word points on the block header
    if(word < block_getFirstWord(blockHeader)) {
#ifdef DEBUG_PRINT
        printf("Points on block header\n");
        fflush(stdout);
#endif
        return NULL;
    }

    if(!isWordAligned(word)) {
        word = (word_t*)((word_t) word & WORD_INVERSE_MASK);
    }

    int lineIndex = block_getLineIndexFromWord(blockHeader, word);
    while(lineIndex > 0 && !line_header_containsObject(&blockHeader->lineHeaders[lineIndex])) {
        lineIndex--;
    }

    if(line_header_containsObject(&blockHeader->lineHeaders[lineIndex])) {
        return getInLine(blockHeader, lineIndex, word);
    } else {
        return NULL;
    }

}


ObjectHeader* object_getLargeInnerPointer(LargeAllocator* allocator, word_t* word) {
    word_t* current = (word_t*)((word_t)word & LARGE_BLOCK_MASK);

    while(!bitmap_getBit(allocator->bitmap, (ubyte_t*)current)) {
        current -= LARGE_BLOCK_SIZE/WORD_SIZE;
    }

    ObjectHeader* objectHeader = (ObjectHeader*) current;
    if(word < (word_t*)objectHeader + object_chunkSize(objectHeader)/WORD_SIZE && objectHeader->rtti != NULL) {
#ifdef DEBUG_PRINT
        printf("large inner pointer: %p, object: %p\n", word, objectHeader);
        fflush(stdout);
#endif
        return objectHeader;
    } else {

        return NULL;
    }
}


ObjectHeader* object_getLargeObject(LargeAllocator* allocator, word_t* word) {
    if(((word_t)word & LARGE_BLOCK_MASK) != (word_t)word) {
        word = (word_t*)((word_t)word & LARGE_BLOCK_MASK);
    }
    if(bitmap_getBit(allocator->bitmap, (ubyte_t*) word) && object_isAllocated((ObjectHeader*) word)) {
        return (ObjectHeader*) word;
    } else {
        ObjectHeader* object = object_getLargeInnerPointer(allocator, word);
        assert(object == NULL || (word >= (word_t*) object && word < (word_t*) objectNextLargeObject(object)));
        return object;
    }
}

void object_mark(ObjectHeader* objectHeader) {
    // Mark the object itself
    object_markObjectHeader(objectHeader);

    if(!object_isLargeObject(objectHeader)) {
        // Mark the block
        BlockHeader *blockHeader = block_getBlockHeader((word_t *) objectHeader);
        block_mark(blockHeader);

        // Mark all Lines
        int startIndex = block_getLineIndexFromWord(blockHeader, (word_t*)objectHeader);
        word_t* lastWord = (word_t*) object_nextObject(objectHeader) - 1;
        int endIndex = block_getLineIndexFromWord(blockHeader, lastWord);
        assert(startIndex >= 0 && startIndex < LINE_COUNT);
        assert(endIndex >= 0 && endIndex < LINE_COUNT);
        assert(startIndex <= endIndex);
        for (int i = startIndex; i <= endIndex; i++) {
            LineHeader *lineHeader = &blockHeader->lineHeaders[i];
            line_header_mark(lineHeader);
        }
    }

}

size_t object_chunkSize(ObjectHeader* objectHeader) {
    return (object_size(objectHeader) + MIN_BLOCK_SIZE - 1) / MIN_BLOCK_SIZE * MIN_BLOCK_SIZE;
}
