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

extern word_t *__modules;
extern int __modules_size;
extern word_t **__stack_bottom;

#define LAST_FIELD_OFFSET -1

void Marker_Mark(Heap *heap, Stack *stack);
void StackOverflowHandler_largeHeapOverflowHeapScan(Heap *heap, Stack *stack);
bool StackOverflowHandler_smallHeapOverflowHeapScan(Heap *heap, Stack *stack);

void Marker_markObject(Heap *heap, Stack *stack, Bytemap *bytemap, Object *object) {
    assert(Bytemap_IsAllocated(bytemap, (word_t*) object));

    assert(Object_Size(object) != 0);
    Object_Mark(heap, object);
    if (!overflow) {
        overflow = Stack_Push(stack, object);
    }
}

void Marker_markConservative(Heap *heap, Stack *stack, word_t *address) {
    assert(Heap_IsWordInHeap(heap, address));
    Object *object = NULL;
    Bytemap *bytemap;
    if (Heap_IsWordInSmallHeap(heap, address)) {
        object = Object_GetUnmarkedObject(heap, address);
        bytemap = heap->smallBytemap;
#ifdef DEBUG_PRINT
        if (object == NULL) {
            printf("Not found: %p\n", address);
        }
#endif
    } else {
        object = Object_GetLargeUnmarkedObject(&largeAllocator, address);
        bytemap = heap->largeBytemap;
    }
    assert(object == NULL || Bytemap_IsAllocated(bytemap,(word_t*) object));

    if (object != NULL) {
        if (Bytemap_IsAllocated(bytemap, (word_t*) object)) {
            Marker_markObject(heap, stack, bytemap, object);
        }
    }
}

void Marker_Mark(Heap *heap, Stack *stack) {
    while (!Stack_IsEmpty(stack)) {
        Object *object = Stack_Pop(stack);

        if (Object_IsArray(object)) {
            if (object->rtti->rt.id == __object_array_id) {
                ArrayHeader *arrayHeader = (ArrayHeader *) object;
                size_t length = arrayHeader -> length;
                word_t **fields = (word_t **) (arrayHeader + 1);
                for (int i = 0; i < length; i++) {
                    word_t *field = fields[i];
                    Object *fieldObject = (Object *) field;
                    Bytemap *bytemap = Heap_BytemapForWord(heap, (word_t*) fieldObject);
                    if (bytemap != NULL &&
                        Bytemap_IsAllocated(bytemap, (word_t*) fieldObject)) {
                        Marker_markObject(heap, stack, bytemap, fieldObject);
                    }
                }
            }
            // non-object arrays do not contain pointers
        } else {
            int64_t *ptr_map = object->rtti->refMapStruct;
            int i = 0;
            while (ptr_map[i] != LAST_FIELD_OFFSET) {
                word_t *field = object->fields[ptr_map[i]];
                Object *fieldObject = (Object *) field;
                Bytemap *bytemap = Heap_BytemapForWord(heap, (word_t*) fieldObject);
                if (bytemap != NULL &&
                    Bytemap_IsAllocated(bytemap, (word_t*) fieldObject)) {
                    Marker_markObject(heap, stack, bytemap, fieldObject);
                }
                ++i;
            }
        }
    }
    StackOverflowHandler_CheckForOverflow();
}

void Marker_markProgramStack(Heap *heap, Stack *stack) {
    // Dumps registers into 'regs' which is on stack
    jmp_buf regs;
    setjmp(regs);
    word_t *dummy;

    word_t **current = &dummy;
    word_t **stackBottom = __stack_bottom;

    while (current <= stackBottom) {

        word_t *stackObject = *current;
        if (Heap_IsWordInHeap(heap, stackObject)) {
            Marker_markConservative(heap, stack, stackObject);
        }
        current += 1;
    }
}

void Marker_markModules(Heap *heap, Stack *stack) {
    word_t **modules = &__modules;
    int nb_modules = __modules_size;

    for (int i = 0; i < nb_modules; i++) {
        Object *object = (Object *) modules[i];
        Bytemap *bytemap = Heap_BytemapForWord(heap, (word_t*) object);
        if (bytemap != NULL &&
            Bytemap_IsAllocated(bytemap, (word_t*) object)) {
            Marker_markObject(heap, stack, bytemap, object);
        }
    }
}

void Marker_MarkRoots(Heap *heap, Stack *stack) {

    Marker_markProgramStack(heap, stack);

    Marker_markModules(heap, stack);

    Marker_Mark(heap, stack);
}
