#include <stdio.h>
#include <setjmp.h>
#include "Marker.h"
#include "Object.h"
#include "Log.h"
#include "State.h"
#include "datastructures/Stack.h"
#include "headers/ObjectHeader.h"
#include "Block.h"
#include "StackoverflowHandler.h"

extern int __object_array_id;
extern word_t *__modules;
extern int __modules_size;
extern word_t **__stack_bottom;

void Marker_mark(Heap *heap, Stack *stack);
void largeHeapOverflowHeapScan(Heap *heap, Stack *stack);
bool smallHeapOverflowHeapScan(Heap *heap, Stack *stack);

void markObject(Heap *heap, Stack *stack, Object *object) {
    assert(!Object_isMarked(&object->header));
    assert(Object_size(&object->header) != 0);
    Object_mark(object);
    if (!overflow) {
        overflow = Stack_push(stack, object);
    }
}

void markConservative(Heap *heap, Stack *stack, word_t *address) {
    assert(heap_isWordInHeap(heap, address));
    Object *object = NULL;
    if (heap_isWordInSmallHeap(heap, address)) {
        object = Object_getObject(address);
        assert(
            object == NULL ||
            Line_containsObject(&Block_getBlockHeader((word_t *)object)
                                     ->lineHeaders[Block_getLineIndexFromWord(
                                         Block_getBlockHeader((word_t *)object),
                                         (word_t *)object)]));
#ifdef DEBUG_PRINT
        if (object == NULL) {
            printf("Not found: %p\n", address);
        }
#endif
    } else {
        object = Object_getLargeObject(heap->largeAllocator, address);
    }

    if (object != NULL && !Object_isMarked(&object->header)) {
        markObject(heap, stack, object);
    }
}

void Marker_mark(Heap *heap, Stack *stack) {
    while (!Stack_isEmpty(stack)) {
        Object *object = Stack_pop(stack);

        if (object->rtti->rt.id == __object_array_id) {
            // remove header and rtti from size
            size_t size =
                Object_size(&object->header) - OBJECT_HEADER_SIZE - WORD_SIZE;
            size_t nbWords = size / WORD_SIZE;
            for (int i = 0; i < nbWords; i++) {

                word_t *field = object->fields[i];
                Object *fieldObject = Object_fromMutatorAddress(field);
                if (heap_isObjectInHeap(heap, fieldObject) &&
                    !Object_isMarked(&fieldObject->header)) {
                    markObject(heap, stack, fieldObject);
                }
            }
        } else {
            int64_t *ptr_map = object->rtti->refMapStruct;
            int i = 0;
            while (ptr_map[i] != -1) {
                word_t *field = object->fields[ptr_map[i]];
                Object *fieldObject = Object_fromMutatorAddress(field);
                if (heap_isObjectInHeap(heap, fieldObject) &&
                    !Object_isMarked(&fieldObject->header)) {
                    markObject(heap, stack, fieldObject);
                }
                ++i;
            }
        }
    }
    StackOverflowHandler_checkForOverflow();
}

void markProgramStack(Heap *heap, Stack *stack) {
    // Dumps registers into 'regs' which is on stack
    jmp_buf regs;
    setjmp(regs);
    word_t *dummy;

    word_t **current = &dummy;
    word_t **stackBottom = __stack_bottom;

    while (current <= stackBottom) {

        word_t *stackObject = (*current) - WORDS_IN_OBJECT_HEADER;
        if (heap_isWordInHeap(heap, stackObject)) {
            markConservative(heap, stack, stackObject);
        }
        current += 1;
    }
}

void markModules(Heap *heap, Stack *stack) {
    word_t **modules = &__modules;
    int nb_modules = __modules_size;

    for (int i = 0; i < nb_modules; i++) {
        Object *object = Object_fromMutatorAddress(modules[i]);
        if (heap_isObjectInHeap(heap, object) &&
            !Object_isMarked(&object->header)) {
            markObject(heap, stack, object);
        }
    }
}

void Marker_markRoots(Heap *heap, Stack *stack) {

    markProgramStack(heap, stack);

    markModules(heap, stack);

    Marker_mark(heap, stack);
}
