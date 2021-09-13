#include "WeakRefStack.h"
#include "datastructures/Stack.h"
#include "metadata/ObjectMeta.h"
#include "headers/ObjectHeader.h"
#include "State.h"

extern word_t *__modules;
int visited = 0;

// A collection of marked WeakReferences.
// Used to correctly set "NULL" values in place of cleaned objects
// and to call other handler functions.


void WeakRefStack_Init(size_t size) { Stack_Init(&weakRefStack, size); }

void WeakRefStack_Push(Object *object) { Stack_Push(&weakRefStack, object); }

void WeakRefStack_Nullify(Heap *heap) {
    visited = 0;
    Bytemap *bytemap = heap->bytemap;
    while (!Stack_IsEmpty(&weakRefStack)) {
        Object *object = Stack_Pop(&weakRefStack);
        int64_t fieldOffset =
            object->rtti->refMapStruct[__weak_ref_field_offset];
        word_t *refObject = object->fields[fieldOffset];
        if (Heap_IsWordInHeap(heap, refObject)) {
            ObjectMeta *objectMeta = Bytemap_Get(bytemap, refObject);
            if (!ObjectMeta_IsMarked(objectMeta)) {
                // WeakReferences should have the referant 
                // field set to null if collected
                object->fields[fieldOffset] = NULL;
                visited++;
            }
        }
    }
}

void WeakRefStack_CallHandlers(Heap *heap) {
    if (visited > 0 && __weak_ref_registry_module_offset != 0 &&
        __weak_ref_registry_field_offset != 0) {
        word_t **modules = &__modules;
        Object *registry = (Object *)modules[__weak_ref_registry_module_offset];
        word_t *field = registry->fields[__weak_ref_registry_field_offset];
        void (*fieldOffset)() = (void *)field;

        fieldOffset();
    }
}