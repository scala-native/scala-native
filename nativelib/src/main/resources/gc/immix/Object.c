#include <stddef.h>
#include <stdio.h>
#include "Object.h"
#include "headers/BlockHeader.h"
#include "Line.h"
#include "Log.h"
#include "utils/MathUtils.h"

ObjectHeader* Object_nextLargeObject(ObjectHeader *objectHeader) {
    size_t size = Object_chunkSize(objectHeader);
    assert(size != 0);
    return (ObjectHeader*)((ubyte_t *)objectHeader + size);
}

ObjectHeader* Object_nextObject(ObjectHeader *objectHeader) {
    size_t size = Object_size(objectHeader);
    assert(size < LARGE_BLOCK_SIZE);
    if(size == 0) {
        return NULL;
    }
    ObjectHeader* next = (ObjectHeader*)((ubyte_t *)objectHeader + size);
    assert(Block_getBlockHeader((word_t *) next) == Block_getBlockHeader((word_t *) objectHeader)
           || (ubyte_t*) Block_getBlockHeader((word_t *) next) ==
                                 (ubyte_t*) Block_getBlockHeader((word_t *) objectHeader) + BLOCK_TOTAL_SIZE);
    return next;
}

static inline bool isWordAligned(word_t* word) {
    return ((word_t)word & WORD_INVERSE_MASK) == (word_t)word;
}


ObjectHeader* getInLine(BlockHeader* blockHeader, int lineIndex, word_t* word) {
    assert(Line_containsObject(Block_getLineHeader(blockHeader, lineIndex)));

    ObjectHeader* current = Line_getFirstObject(Block_getLineHeader(blockHeader, lineIndex));
    ObjectHeader* next = Object_nextObject(current);

    word_t* lineEnd = Block_getLineAddress(blockHeader, lineIndex) + WORDS_IN_LINE;

    while(next != NULL && (word_t*) next < lineEnd && (word_t*)next <= word) {
        current = next;
        next = Object_nextObject(next);
    }

    if(Object_isAllocated(current) && word >= (word_t*)current && word < (word_t*)next) {
#ifdef DEBUG_PRINT
        if((word_t*)current != word) {
            printf("inner pointer: %p object: %p\n", word, current);
            fflush(stdout);
        }
#endif
        return current;
    } else {
#ifdef DEBUG_PRINT
        printf("ignoring %p\n", word);
        fflush(stdout);
#endif
        return NULL;
    }

}

ObjectHeader* Object_getObject(word_t *word) {
    BlockHeader* blockHeader = Block_getBlockHeader(word);

    //Check if the word points on the block header
    if(word < Block_getFirstWord(blockHeader)) {
#ifdef DEBUG_PRINT
        printf("Points on block header %p\n", word);
        fflush(stdout);
#endif
        return NULL;
    }

    if(!isWordAligned(word)) {
#ifdef DEBUG_PRINT
        printf("Word not aligned: %p aligning to %p\n", word, (word_t*)((word_t) word & WORD_INVERSE_MASK));
        fflush(stdout);
#endif
        word = (word_t*)((word_t) word & WORD_INVERSE_MASK);
    }

    int lineIndex = Block_getLineIndexFromWord(blockHeader, word);
    while(lineIndex > 0 && !Line_containsObject(Block_getLineHeader(blockHeader, lineIndex))) {
        lineIndex--;
    }

    if(Line_containsObject(Block_getLineHeader(blockHeader, lineIndex))) {
        return getInLine(blockHeader, lineIndex, word);
    } else {
#ifdef DEBUG_PRINT
        printf("Word points to empty line %p\n", word);
        fflush(stdout);
#endif
        return NULL;
    }

}


ObjectHeader* object_getLargeInnerPointer(LargeAllocator* allocator, word_t* word) {
    word_t* current = (word_t*)((word_t)word & LARGE_BLOCK_MASK);

    while(!Bitmap_getBit(allocator->bitmap, (ubyte_t *) current)) {
        current -= LARGE_BLOCK_SIZE/WORD_SIZE;
    }

    ObjectHeader* objectHeader = (ObjectHeader*) current;
    if(word < (word_t*)objectHeader + Object_chunkSize(objectHeader)/WORD_SIZE && objectHeader->rtti != NULL) {
#ifdef DEBUG_PRINT
        printf("large inner pointer: %p, object: %p\n", word, objectHeader);
        fflush(stdout);
#endif
        return objectHeader;
    } else {

        return NULL;
    }
}


ObjectHeader* Object_getLargeObject(LargeAllocator *allocator, word_t *word) {
    if(((word_t)word & LARGE_BLOCK_MASK) != (word_t)word) {
        word = (word_t*)((word_t)word & LARGE_BLOCK_MASK);
    }
    if(Bitmap_getBit(allocator->bitmap, (ubyte_t *) word) && Object_isAllocated((ObjectHeader *) word)) {
        return (ObjectHeader*) word;
    } else {
        ObjectHeader* object = object_getLargeInnerPointer(allocator, word);
        assert(object == NULL || (word >= (word_t*) object && word < (word_t*) Object_nextLargeObject(object)));
        return object;
    }
}

void Object_mark(ObjectHeader *objectHeader) {
    // Mark the object itself
    Object_markObjectHeader(objectHeader);

    if(!Object_isLargeObject(objectHeader)) {
        // Mark the block
        BlockHeader *blockHeader = Block_getBlockHeader((word_t *) objectHeader);
        Block_mark(blockHeader);

        // Mark all Lines
        int startIndex = Block_getLineIndexFromWord(blockHeader, (word_t *) objectHeader);
        word_t* lastWord = (word_t*) Object_nextObject(objectHeader) - 1;
        int endIndex = Block_getLineIndexFromWord(blockHeader, lastWord);
        assert(startIndex >= 0 && startIndex < LINE_COUNT);
        assert(endIndex >= 0 && endIndex < LINE_COUNT);
        assert(startIndex <= endIndex);
        for (int i = startIndex; i <= endIndex; i++) {
            LineHeader *lineHeader = Block_getLineHeader(blockHeader, i);
            Line_mark(lineHeader);
        }
    }

}

size_t Object_chunkSize(ObjectHeader *objectHeader) {
    return (Object_size(objectHeader) + MIN_BLOCK_SIZE - 1) / MIN_BLOCK_SIZE * MIN_BLOCK_SIZE;
}
