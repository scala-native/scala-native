#include <stddef.h>
#include <stdio.h>
#include "Object.h"
#include "Log.h"
#include "utils/MathUtils.h"

word_t *Object_LastWord(Object *object) {
    size_t size = Object_Size(object);
    assert(size < LARGE_BLOCK_SIZE);
    word_t *last =
        (word_t *)((ubyte_t *)object + size) - ALLOCATION_ALIGNMENT_WORDS;
    return last;
}

Object *Object_getInnerPointer(Heap *heap, BlockMeta *blockMeta, word_t *word,
                               ObjectMeta *wordMeta, bool collectingOld) {
    int stride;
    word_t *blockStart;
    if (BlockMeta_ContainsLargeObjects(blockMeta)) {
        stride = MIN_BLOCK_SIZE / ALLOCATION_ALIGNMENT;
        BlockMeta *superblockStart =
            BlockMeta_GetSuperblockStart(heap->blockMetaStart, blockMeta);
        blockStart = BlockMeta_GetBlockStart(heap->blockMetaStart,
                                             heap->heapStart, superblockStart);
    } else {
        stride = 1;
        blockStart = Block_GetBlockStartForWord(word);
    }

    word_t *current = word;
    ObjectMeta *currentMeta = wordMeta;
    while (current >= blockStart && ObjectMeta_IsFree(currentMeta)) {
        current -= ALLOCATION_ALIGNMENT_WORDS * stride;
        currentMeta -= stride;
    }
    Object *object = (Object *)current;
    if (ObjectMeta_IsAlive(currentMeta, collectingOld) &&
        word < current + Object_Size(object) / WORD_SIZE) {
        return object;
    } else {
        return NULL;
    }
}

Object *Object_GetUnmarkedObject(Heap *heap, word_t *word, bool collectingOld) {
    BlockMeta *blockMeta =
        Block_GetBlockMeta(heap->blockMetaStart, heap->heapStart, word);

    if (BlockMeta_ContainsLargeObjects(blockMeta)) {
        word = (word_t *)((word_t)word & LARGE_BLOCK_MASK);
    } else {
        word = (word_t *)((word_t)word & ALLOCATION_ALIGNMENT_INVERSE_MASK);
    }

    ObjectMeta *wordMeta = Bytemap_Get(heap->bytemap, word);
    if (ObjectMeta_IsPlaceholder(wordMeta) || !ObjectMeta_IsAlive(wordMeta, collectingOld)) {
        return NULL;
    } else if (ObjectMeta_IsAlive(wordMeta, collectingOld)) {
        return (Object *)word;
    } else {
        return Object_getInnerPointer(heap, blockMeta, word, wordMeta, collectingOld);
    }
}

void Object_Mark(Heap *heap, Object *object, ObjectMeta *objectMeta, bool collectingOld) {
    // Mark the object itself. The double check is necessary to not overwrite
    // the remembered state
    uint8_t state = *objectMeta;
    if (collectingOld) {
        if (state == om_marked) {
            ObjectMeta_SetAllocated(objectMeta);
        } else if (state == om_marked_rem)  {
            ObjectMeta_SetAllocatedRem(objectMeta);
        }
    } else {
        if (state == om_allocated) {
            ObjectMeta_SetMarked(objectMeta);
        }
    }

    BlockMeta *blockMeta = Block_GetBlockMeta(
        heap->blockMetaStart, heap->heapStart, (word_t *)object);
    assert((collectingOld && BlockMeta_IsOld(blockMeta)) || (!collectingOld && !BlockMeta_IsOld(blockMeta)));
    if (!BlockMeta_ContainsLargeObjects(blockMeta)) {
        BlockMeta_Mark(blockMeta);
    } else {
        blockMeta = BlockMeta_GetSuperblockStart(heap->blockMetaStart, blockMeta);
        BlockMeta_MarkSuperblock(blockMeta);
    }
}
