#include <stdio.h>
#include <setjmp.h>
#include "Marker.h"
#include "Object.h"
#include "Log.h"
#include "Heap.h"
#include "Allocator.h"
#include "stats/AllocatorStats.h"

#define UNW_LOCAL_ONLY
#include <libunwind.h>

extern int __object_array_id;
extern word_t* __modules;
extern int __modules_size;



void markObject(Heap* heap, Stack* stack, ObjectHeader* object) {
    assert(!Object_isMarked(object));
    assert(Object_size(object) != 0);
    Object_mark(object);
    Stack_push(stack, object);
#ifdef ALLOCATOR_STATS
    heap->allocator->stats->liveObjectCount++;
#endif
}

void markConservative(Heap *heap, Stack *stack, word_t *address) {
    assert(heap_isWordInHeap(heap, address));
    ObjectHeader* object = NULL;
    if(heap_isWordInSmallHeap(heap, address)) {
        object = Object_getObject(address);
        assert(object == NULL || Line_containsObject(&Block_getBlockHeader((word_t *) object)->lineHeaders[
                Block_getLineIndexFromWord(Block_getBlockHeader((word_t *) object), (word_t *) object)]));
#ifdef DEBUG_PRINT
        if(object == NULL) {
            printf("Not found: %p\n", address);
        }
#endif
    } else {
        object = Object_getLargeObject(heap->largeAllocator, address);
    }

    if(object != NULL && !Object_isMarked(object)) {
        markObject(heap, stack, object);
    }
}


void mark(Heap *heap, Stack *stack) {
    while(!Stack_isEmpty(stack)) {
        ObjectHeader* object = Stack_pop(stack);

        if(object->rtti->rt.id == __object_array_id) {
            // remove header and rtti from size
            size_t size = Object_size(object) - OBJECT_HEADER_SIZE - WORD_SIZE;
            size_t nbWords = size / WORD_SIZE;
            for(int i = 0; i < nbWords; i++) {

                word_t* field = object->fields[i];
                ObjectHeader* fieldObject = Object_fromMutatorAddress(field);
                if(heap_isObjectInHeap(heap, fieldObject) && !Object_isMarked(fieldObject)) {
                    markObject(heap, stack, fieldObject);
                }

            }
        } else {
            int64_t* ptr_map = object->rtti->refMapStruct;
            int i=0;
            while(ptr_map[i] != -1) {
                word_t* field = object->fields[ptr_map[i]/WORD_SIZE - 1];
                ObjectHeader* fieldObject = Object_fromMutatorAddress(field);
                if(heap_isObjectInHeap(heap, fieldObject) && !Object_isMarked(fieldObject)) {
                    markObject(heap, stack, fieldObject);
                }
                ++i;
            }
        }
    }
}

void markProgramStack(Heap *heap, Stack *stack) {
    unw_cursor_t cursor;
    unw_context_t context;
    unw_getcontext(&context);
    unw_init_local(&cursor, &context);
    unw_word_t top = LONG_MAX, bottom = 0;
    unw_word_t rsp;

    while (unw_step(&cursor) > 0) {
        unw_get_reg(&cursor, UNW_X86_64_RSP, &rsp);

        if(rsp < top) {
            top = rsp;
        }
        if(rsp > bottom) {
            bottom = rsp;
        }

    }
    unw_word_t p = top;

    while(p < bottom) {

        word_t* pp = (*(word_t**)p) - WORDS_IN_OBJECT_HEADER;
        if(heap_isWordInHeap(heap, pp)) {
            markConservative(heap, stack, pp);
        }
        p += 8;
    }
}

void markModules(Heap *heap, Stack *stack) {
    word_t** modules= &__modules;
    int nb_modules = __modules_size;

    for(int i = 0; i < nb_modules; i++) {
        ObjectHeader* object = Object_fromMutatorAddress(modules[i]);
        if(heap_isObjectInHeap(heap, object) && !Object_isMarked(object)) {
            markObject(heap, stack, object);
        }
    }
}

void Mark_roots(Heap *heap, Stack *stack) {
#ifdef ALLOCATOR_STATS
    heap->allocator->stats->liveObjectCount = 0;
#endif

    // Dumps registers into 'regs' which is on stack
    jmp_buf regs;
    setjmp(regs);

    markProgramStack(heap, stack);

    markModules(heap, stack);

    mark(heap, stack);

}