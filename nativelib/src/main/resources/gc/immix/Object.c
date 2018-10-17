#include <stddef.h>
#include <stdio.h>
#include "Object.h"
#include "Block.h"
#include "Log.h"
#include "utils/MathUtils.h"

Object *Object_NextLargeObject(Object *object) {
    size_t size = Object_ChunkSize(object);
    assert(size != 0);
    return (Object *)((ubyte_t *)object + size);
}

word_t *Object_LastWord(Object *object) {
    size_t size = Object_Size(object);
    assert(size < LARGE_BLOCK_SIZE);
    word_t *last = (word_t *)((ubyte_t *)object + size) - 1;
    return last;
}

static inline bool Object_isAligned(word_t *word) {
    return ((word_t)word & ALLOCATION_ALIGNMENT_INVERSE_MASK) == (word_t)word;
}

Object *Object_getInnerPointer(word_t *blockStart, word_t *word,
                               ObjectMeta *wordMeta) {
    word_t *current = word;
    ObjectMeta *currentMeta = wordMeta;
    while (current >= blockStart && ObjectMeta_IsFree(currentMeta)) {
        current -= ALLOCATION_ALIGNMENT_WORDS;
        currentMeta = Bytemap_PreviousWord(currentMeta);
    }
    Object *object = (Object *)current;
    if (ObjectMeta_IsAllocated(currentMeta) &&
        word < current + Object_Size(object) / WORD_SIZE) {
#ifdef DEBUG_PRINT
        if ((word_t *)current != word) {
            printf("inner pointer: %p object: %p\n", word, current);
            fflush(stdout);
        }
#endif
        return object;
    } else {
        return NULL;
    }
}

Object *Object_GetUnmarkedObject(Heap *heap, word_t *word) {
    BlockMeta *blockMeta =
        Block_GetBlockMeta(heap->blockMetaStart, heap->heapStart, word);
    word_t *blockStart = Block_GetBlockStartForWord(word);

    if (!Object_isAligned(word)) {
#ifdef DEBUG_PRINT
        printf("Word not aligned: %p aligning to %p\n", word,
               (word_t *)((word_t)word & ALLOCATION_ALIGNMENT_INVERSE_MASK));
        fflush(stdout);
#endif
        word = (word_t *)((word_t)word & ALLOCATION_ALIGNMENT_INVERSE_MASK);
    }

    ObjectMeta *wordMeta = Bytemap_Get(heap->smallBytemap, word);
    if (ObjectMeta_IsPlaceholder(wordMeta) || ObjectMeta_IsMarked(wordMeta)) {
        return NULL;
    } else if (ObjectMeta_IsAllocated(wordMeta)) {
        return (Object *)word;
    } else {
        return Object_getInnerPointer(blockStart, word, wordMeta);
    }
}

Object *Object_getLargeInnerPointer(word_t *word, ObjectMeta *wordMeta) {
    word_t *current = (word_t *)((word_t)word & LARGE_BLOCK_MASK);
    ObjectMeta *currentMeta = wordMeta;

    while (ObjectMeta_IsFree(currentMeta)) {
        current -= ALLOCATION_ALIGNMENT_WORDS;
        currentMeta = Bytemap_PreviousWord(currentMeta);
    }

    Object *object = (Object *)current;
    if (ObjectMeta_IsAllocated(currentMeta) &&
        word < (word_t *)object + Object_ChunkSize(object) / WORD_SIZE) {
#ifdef DEBUG_PRINT
        printf("large inner pointer: %p, object: %p\n", word, object);
        fflush(stdout);
#endif
        return object;
    } else {
        return NULL;
    }
}

Object *Object_GetLargeUnmarkedObject(Bytemap *bytemap, word_t *word) {
    if (((word_t)word & LARGE_BLOCK_MASK) != (word_t)word) {
        word = (word_t *)((word_t)word & LARGE_BLOCK_MASK);
    }
    ObjectMeta *wordMeta = Bytemap_Get(bytemap, word);
    if (ObjectMeta_IsPlaceholder(wordMeta) || ObjectMeta_IsMarked(wordMeta)) {
        return NULL;
    } else if (ObjectMeta_IsAllocated(wordMeta)) {
        return (Object *)word;
    } else {
        Object *object = Object_getLargeInnerPointer(word, wordMeta);
        assert(object == NULL ||
               (word >= (word_t *)object &&
                word < (word_t *)Object_NextLargeObject(object)));
        return object;
    }
}

void Object_Mark(Heap *heap, Object *object, ObjectMeta *objectMeta) {
    // Mark the object itself
    ObjectMeta_SetMarked(objectMeta);

    if (Heap_IsWordInSmallHeap(heap, (word_t *)object)) {
        // Mark the block
        BlockMeta *blockMeta = Block_GetBlockMeta(
            heap->blockMetaStart, heap->heapStart, (word_t *)object);
        word_t *blockStart = Block_GetBlockStartForWord((word_t *)object);
        BlockMeta_Mark(blockMeta);

        // Mark all Lines
        word_t *lastWord = Object_LastWord(object);

        assert(blockMeta == Block_GetBlockMeta(heap->blockMetaStart,
                                               heap->heapStart, lastWord));
        LineMeta *firstLineMeta = Heap_LineMetaForWord(heap, (word_t *)object);
        LineMeta *lastLineMeta = Heap_LineMetaForWord(heap, lastWord);
        assert(firstLineMeta <= lastLineMeta);
        for (LineMeta *lineMeta = firstLineMeta; lineMeta <= lastLineMeta;
             lineMeta++) {
            Line_Mark(lineMeta);
        }
    }
}

size_t Object_ChunkSize(Object *object) {
    if (object->rtti == NULL) {
        Chunk *chunk = (Chunk *)object;
        return chunk->size;
    } else {
        return MathUtils_RoundToNextMultiple(Object_Size(object),
                                             MIN_BLOCK_SIZE);
    }
}
