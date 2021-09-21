#include "WeakRefStack.h"
#include "datastructures/Stack.h"
#include "metadata/ObjectMeta.h"
#include "headers/ObjectHeader.h"
#include "State.h"
#include <stdbool.h>

extern word_t *__modules;
bool visited = false;

// A collection of marked WeakReferences.
// Used to correctly set "NULL" values in place of cleaned objects
// and to call other handler functions with WeakRefStack_CallHandlers.

void WeakRefStack_Init(size_t size) { Stack_Init(&weakRefStack, size); }

void WeakRefStack_Push(Object *object) { Stack_Push(&weakRefStack, object); }

void WeakRefStack_Nullify() {
    visited = false;
    Bytemap *bytemap = heap.bytemap;
    while (!Stack_IsEmpty(&weakRefStack)) {
        Object *object = Stack_Pop(&weakRefStack);
        int64_t fieldOffset = __weak_ref_field_offset;
        word_t *refObject = object->fields[fieldOffset];
        if (Heap_IsWordInHeap(&heap, refObject)) {
            ObjectMeta *objectMeta = Bytemap_Get(bytemap, refObject);
            if (!ObjectMeta_IsMarked(objectMeta)) {
                // WeakReferences should have the held referent
                // field set to null if collected
                object->fields[fieldOffset] = NULL;
                visited = true;
            }
        }
    }
}

void WeakRefStack_CallHandlers(Heap *heap) {
    if (visited && __weak_ref_registry_module_offset != -1 &&
        __weak_ref_registry_field_offset != -1) {
        visited = false;
        word_t **modules = &__modules;
        Object *registry = (Object *)modules[__weak_ref_registry_module_offset];
        word_t *field = registry->fields[__weak_ref_registry_field_offset];
        void (*handlerFn)() = (void *)field;

        handlerFn();
    }
}