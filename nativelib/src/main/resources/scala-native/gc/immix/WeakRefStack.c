#if defined(SCALANATIVE_GC_IMMIX)

#include "WeakRefStack.h"
#include "datastructures/Stack.h"
#include "metadata/ObjectMeta.h"
#include "immix_commix/headers/ObjectHeader.h"
#include "State.h"
#include <stdbool.h>

bool visited = false;
void (*handlerFn)() = NULL;

// A collection of marked WeakReferences.
// Used to correctly set "NULL" values in place of cleaned objects
// and to call other handler functions with WeakRefStack_CallHandlers.

void WeakRefStack_Nullify(void) {
    visited = false;
    Bytemap *bytemap = heap.bytemap;
    while (!Stack_IsEmpty(&weakRefStack)) {
        Object *object = Stack_Pop(&weakRefStack);
        Object **weakRefReferantField =
            (Object **)((int8_t *)object + __weak_ref_field_offset);
        word_t *weakRefReferant = (word_t *)*weakRefReferantField;
        if (Heap_IsWordInHeap(&heap, weakRefReferant)) {
            ObjectMeta *objectMeta = Bytemap_Get(bytemap, weakRefReferant);
            if (ObjectMeta_IsAllocated(objectMeta) &&
                !ObjectMeta_IsMarked(objectMeta)) {
                // WeakReferences should have the held referent
                // field set to null if collected
                *weakRefReferantField = NULL;
                visited = true;
            }
        }
    }
}

void WeakRefStack_SetHandler(void *handler) { handlerFn = handler; }

void WeakRefStack_CallHandlers(void) {
    if (visited && handlerFn != NULL) {
        visited = false;
        handlerFn();
    }
}

#endif
