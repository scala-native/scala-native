#include <stdio.h>
#include <memory.h>
#include "Block.h"
#include "Object.h"
#include "Log.h"

extern int __object_array_id;

INLINE void recycleUnmarkedBlock(Allocator *allocator,
                                 BlockHeader *blockHeader) {
    memset(blockHeader, 0, LINE_SIZE);
    BlockList_addLast(&allocator->freeBlocks, blockHeader);
    Block_setFlag(blockHeader, block_free);
}

INLINE void recycleMarkedLine(BlockHeader *blockHeader, LineHeader *lineHeader,
                              int lineIndex) {
    Line_unmark(lineHeader);
    // If the line contains an object
    if (Line_containsObject(lineHeader)) {
        // Unmark all objects in line
        Object *object = Line_getFirstObject(lineHeader);
        word_t *lineEnd =
            Block_getLineAddress(blockHeader, lineIndex) + WORDS_IN_LINE;
        while (object != NULL && (word_t *)object < lineEnd) {
            ObjectHeader *objectHeader = &object->header;
            if (Object_isMarked(objectHeader)) {
                Object_setAllocated(objectHeader);
            } else {
                Object_setFree(objectHeader);
            }
            object = Object_nextObject(object);
        }
    }
}

void Block_recycle(Allocator *allocator, BlockHeader *blockHeader) {

    if (!Block_isMarked(blockHeader)) {
        recycleUnmarkedBlock(allocator, blockHeader);

    } else {
        assert(Block_isMarked(blockHeader));
        Block_unmark(blockHeader);
        int16_t lineIndex = 0;
        int lastRecyclable = -1;
        while (lineIndex < LINE_COUNT) {
            LineHeader *lineHeader =
                Block_getLineHeader(blockHeader, lineIndex);
            if (Line_isMarked(lineHeader)) {
                // Unmark line
                recycleMarkedLine(blockHeader, lineHeader, lineIndex);
                lineIndex++;
            } else {
                if (lastRecyclable == -1) {
                    blockHeader->header.first = lineIndex;
                } else {
                    Block_getFreeLineHeader(blockHeader, lastRecyclable)->next =
                        lineIndex;
                }
                lastRecyclable = lineIndex;
                Line_setEmpty(lineHeader);
                lineIndex++;
                uint8_t size = 1;
                while (lineIndex < LINE_COUNT &&
                       !Line_isMarked(lineHeader = Block_getLineHeader(
                                          blockHeader, lineIndex))) {
                    size++;
                    lineIndex++;
                    Line_setEmpty(lineHeader);
                }
                Block_getFreeLineHeader(blockHeader, lastRecyclable)->size =
                    size;
            }
        }
        if (lastRecyclable == -1) {
            Block_setFlag(blockHeader, block_unavailable);

        } else {
            Block_getFreeLineHeader(blockHeader, lastRecyclable)->next =
                LAST_HOLE;
            Block_setFlag(blockHeader, block_recyclable);
            BlockList_addLast(&allocator->recycledBlocks, blockHeader);

            assert(blockHeader->header.first != -1);
        }
    }
}

// This method is used in case of overflow during the marking phase.
// It sweeps through the block starting at `currentOverflowAddress` until if
// finds a marked block with unmarked children.
// It updates the value of `currentOverflowAddress` while sweeping through the
// block
// Once a block is found it adds it to the stack and returns `true`. If no block
// is found it returns `false`.
bool block_overflowHeapScan(BlockHeader *block, Heap *heap, Stack *stack,
                            word_t **currentOverflowAddress) {
    word_t *blockEnd = Block_getBlockEnd(block);
    if (!Block_isMarked(block)) {
        *currentOverflowAddress = blockEnd;
        return false;
    }

    int lineIndex;

    if (*currentOverflowAddress == (word_t *)block) {
        lineIndex = 0;
    } else {
        lineIndex = Block_getLineIndexFromWord(block, *currentOverflowAddress);
    }
    while (lineIndex < LINE_COUNT) {
        LineHeader *lineHeader = Block_getLineHeader(block, lineIndex);

        if (Line_isMarked(lineHeader) && Line_containsObject(lineHeader)) {
            Object *object = Line_getFirstObject(lineHeader);
            word_t *lineEnd =
                Block_getLineAddress(block, lineIndex) + WORDS_IN_LINE;
            while (object != NULL && (word_t *)object < lineEnd) {
                ObjectHeader *objectHeader = &object->header;
                if (Object_isMarked(objectHeader)) {
                    if (object->rtti->rt.id == __object_array_id) {
                        // remove header and rtti from size
                        size_t size = Object_size(&object->header) -
                                      OBJECT_HEADER_SIZE - WORD_SIZE;
                        size_t nbWords = size / WORD_SIZE;
                        for (int i = 0; i < nbWords; i++) {

                            word_t *field = object->fields[i];
                            Object *fieldObject =
                                Object_fromMutatorAddress(field);
                            if (heap_isObjectInHeap(heap, fieldObject) &&
                                !Object_isMarked(&fieldObject->header)) {
                                Stack_push(stack, object);
                                *currentOverflowAddress = (word_t *)object;
                                return true;
                            }
                        }
                    } else {
                        int64_t *ptr_map = object->rtti->refMapStruct;
                        int i = 0;
                        while (ptr_map[i] != -1) {
                            word_t *field = object->fields[ptr_map[i]];
                            Object *fieldObject =
                                Object_fromMutatorAddress(field);
                            if (heap_isObjectInHeap(heap, fieldObject) &&
                                !Object_isMarked(&fieldObject->header)) {
                                Stack_push(stack, object);
                                *currentOverflowAddress = (word_t *)object;
                                return true;
                            }
                            ++i;
                        }
                    }
                }
                object = Object_nextObject(object);
            }
        }
        lineIndex++;
    }
    *currentOverflowAddress = blockEnd;
    return false;
}

void Block_print(BlockHeader *block) {
    printf("%p ", block);
    if (Block_isFree(block)) {
        printf("FREE\n");
    } else if (Block_isUnavailable(block)) {
        printf("UNAVAILABLE\n");
    } else {
        int lineIndex = block->header.first;
        while (lineIndex != LAST_HOLE) {
            FreeLineHeader *freeLineHeader =
                Block_getFreeLineHeader(block, lineIndex);
            printf("[index: %d, size: %d] -> ", lineIndex,
                   freeLineHeader->size);
            lineIndex = freeLineHeader->next;
        }
        printf("\n");
    }
    fflush(stdout);
}