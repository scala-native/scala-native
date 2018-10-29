#include <stdio.h>
#include "StackoverflowHandler.h"
#include "State.h"
#include "Block.h"
#include "Object.h"
#include "Marker.h"

extern int __object_array_id;

#define LAST_FIELD_OFFSET -1

void StackOverflowHandler_largeBlockScan(Heap *heap, Stack *stack,
                                         word_t *blockStart, word_t *blockEnd);
void StackOverflowHandler_blockScan(Heap *heap, Stack *stack,
                                    word_t *blockStart);

void StackOverflowHandler_CheckForOverflow() {
    if (overflow) {
        overflow = false;
        Stack_DoubleSize(&stack);

#ifdef PRINT_STACK_OVERFLOW
        printf("Stack grew to %zu bytes\n",
               stack.nb_words * sizeof(Stack_Type));
        fflush(stdout);
#endif

        word_t *blockMetaEnd = heap.blockMetaEnd;
        BlockMeta *currentBlock = (BlockMeta *)heap.blockMetaStart;
        word_t *blockStart = heap.heapStart;

        while ((word_t *)currentBlock < blockMetaEnd) {
            assert(!BlockMeta_IsSuperblockMiddle(currentBlock));
            int size;
            if (BlockMeta_IsSuperblockStart(currentBlock)) {
                size = BlockMeta_SuperblockSize(currentBlock);
                assert(size > 0);
                StackOverflowHandler_largeBlockScan(&heap, &stack, blockStart,
                                                    blockStart +
                                                        size * WORDS_IN_BLOCK);
            } else {
                size = 1;
                if (BlockMeta_IsMarked(currentBlock)) {
                    StackOverflowHandler_blockScan(&heap, &stack, blockStart);
                }
            }
            currentBlock += size;
            blockStart += size * WORDS_IN_BLOCK;
        }
    }
}

void StackOverflowHandler_mark(Heap *heap, Stack *stack, Object *object,
                               ObjectMeta *objectMeta) {

    if (ObjectMeta_IsMarked(objectMeta)) {
        Bytemap *bytemap = heap->bytemap;
        if (Object_IsArray(object)) {
            if (object->rtti->rt.id == __object_array_id) {
                ArrayHeader *arrayHeader = (ArrayHeader *)object;
                size_t length = arrayHeader->length;
                word_t **fields = (word_t **)(arrayHeader + 1);
                for (int i = 0; i < length; i++) {
                    word_t *field = fields[i];
                    Object *fieldObject = (Object *)field;
                    if (Heap_IsWordInHeap(heap, field)) {
                        ObjectMeta *metaF = Bytemap_Get(bytemap, field);
                        if (ObjectMeta_IsAllocated(metaF)) {
                            Stack_Push(stack, object);
                            Marker_Mark(heap, stack);
                        }
                    }
                }
            }
            // non-object arrays do not contain pointers
        } else {
            int64_t *ptr_map = object->rtti->refMapStruct;
            int i = 0;
            while (ptr_map[i] != LAST_FIELD_OFFSET) {
                word_t *field = object->fields[ptr_map[i]];
                Object *fieldObject = (Object *)field;
                if (Heap_IsWordInHeap(heap, field)) {
                    ObjectMeta *metaF = Bytemap_Get(bytemap, field);
                    if (ObjectMeta_IsAllocated(metaF)) {
                        Stack_Push(stack, object);
                        Marker_Mark(heap, stack);
                    }
                }
                i++;
            }
        }
    }
}

void StackOverflowHandler_largeBlockScan(Heap *heap, Stack *stack,
                                         word_t *blockStart, word_t *blockEnd) {
    // We only need to look at the first object and the last block.
    // See LargeAllocator_Sweep
    ObjectMeta *firstObject = Bytemap_Get(heap->bytemap, blockStart);
    assert(!ObjectMeta_IsFree(firstObject));
    if (ObjectMeta_IsMarked(firstObject)) {
        StackOverflowHandler_mark(heap, stack, (Object *)blockStart,
                                  firstObject);
    }

    word_t *lastBlockStart = blockEnd - WORDS_IN_BLOCK;
    word_t *current = lastBlockStart + (MIN_BLOCK_SIZE / WORD_SIZE);
    ObjectMeta *currentMeta = Bytemap_Get(heap->bytemap, current);
    while (current < blockEnd) {
        StackOverflowHandler_mark(heap, stack, (Object *)current, currentMeta);

        current += MIN_BLOCK_SIZE / WORD_SIZE;
        currentMeta += MIN_BLOCK_SIZE / ALLOCATION_ALIGNMENT;
    }
}

void StackOverflowHandler_blockScan(Heap *heap, Stack *stack,
                                    word_t *blockStart) {
    Bytemap *bytemap = heap->bytemap;
    word_t *lineStart = blockStart;
    for (int lineIndex = 0; lineIndex < LINE_COUNT; lineIndex++) {

        word_t *lineStart = Block_GetLineAddress(blockStart, lineIndex);
        word_t *lineEnd = lineStart + WORDS_IN_LINE;

        if (Line_IsMarked(Heap_LineMetaForWord(heap, lineStart))) {
            word_t *cursor = lineStart;
            ObjectMeta *cursorMeta = Bytemap_Get(bytemap, cursor);
            while (cursor < lineEnd) {
                StackOverflowHandler_mark(heap, stack, (Object *)cursor,
                                          cursorMeta);

                cursor += ALLOCATION_ALIGNMENT_WORDS;
                cursorMeta += 1;
            }
        }

        lineStart = lineEnd;
    }
}
