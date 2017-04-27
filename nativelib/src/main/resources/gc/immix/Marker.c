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
    assert(!object_isMarked(object));
    assert(object_size(object) != 0);
    object_mark(object);
    stack_push(stack, object);
#ifdef ALLOCATOR_STATS
    heap->allocator->stats->liveObjectCount++;
#endif
}

void mark(Heap* heap, Stack* stack, word_t* address) {
    assert(heap_isWordInHeap(heap, address));
    ObjectHeader* object = NULL;
    if(heap_isWordInSmallHeap(heap, address)) {
        object = object_getObject(address);
        assert(object == NULL || line_header_containsObject(&block_getBlockHeader((word_t*)object)->lineHeaders[
                block_getLineIndexFromWord(block_getBlockHeader((word_t*)object), (word_t*)object)]));
    } else {
        object = object_getLargeObject(heap->largeAllocator, address);
    }

    if(object != NULL && !object_isMarked(object)) {
        markObject(heap, stack, object);
    }
}


void marker_mark(Heap* heap, Stack* stack) {
    while(!stack_isEmpty(stack)) {
        ObjectHeader* object = stack_pop(stack);

        if(object->rtti->rt.id == __object_array_id) {
            // remove header and rtti from size
            size_t size = object_size(object) - 2 * sizeof(word_t);
            size_t nbWords = size / sizeof(word_t);
            for(int i = 0; i < nbWords; i++) {

                word_t* field = object->fields[i];
                ObjectHeader* fieldObject = (ObjectHeader*)(field - 1);
                if(heap_isObjectInHeap(heap, fieldObject) && !object_isMarked(fieldObject)) {
                    markObject(heap, stack, fieldObject);
                }

            }
        } else {
            int64_t* ptr_map = object->rtti->refMapStruct;
            int i=0;
            while(ptr_map[i] != -1) {
                word_t* field = object->fields[ptr_map[i]/sizeof(word_t) - 1];
                ObjectHeader* fieldObject = (ObjectHeader*)(field - 1);
                if(heap_isObjectInHeap(heap, fieldObject) && !object_isMarked(fieldObject)) {
                    markObject(heap, stack, fieldObject);
                }
                ++i;
            }
        }
    }
}

void mark_roots_stack(Heap* heap, Stack* stack) {
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

        word_t* pp = (*(word_t**)p) - 1;
        if(heap_isWordInHeap(heap, pp)) {
            mark(heap, stack, pp);
        }
        p += 8;
    }
}

void mark_roots_modules(Heap* heap, Stack* stack) {
    word_t** module = &__modules;
    int nb_modules = __modules_size;

    for(int i=0; i < nb_modules; i++) {
        word_t* addr = module[i] - 1;
        if(heap_isWordInHeap(heap, addr)) {
            mark(heap, stack, addr);
        }
    }
}

void mark_roots(Heap* heap, Stack* stack) {
#ifdef ALLOCATOR_STATS
    heap->allocator->stats->liveObjectCount = 0;
#endif

    // Dumps registers into 'regs' which is on stack
    jmp_buf regs;
    setjmp(regs);

    mark_roots_stack(heap, stack);

    mark_roots_modules(heap, stack);

    marker_mark(heap, stack);

}