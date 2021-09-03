#include "WeakRefStack.h"
#include "datastructures/Stack.h"
#include "metadata/ObjectMeta.h"
#include "State.h"

// A collection of marked WeakReferences.
// Used to correctly set "NULL" values in place of cleaned objects

void WeakRefStack_Init(size_t size) {
    Stack_Init(&weakRefStack, size);
}
void WeakRefStack_Push(Object *object) {
    Stack_Push(&weakRefStack, object);
}
void WeakRefStack_Visit(Heap *heap) {
    Bytemap *bytemap = heap->bytemap;
    while (!Stack_IsEmpty(&weakRefStack)) {
        Object* object = Stack_Pop(&weakRefStack);
        // asserts
        int64_t fieldOffset = object->rtti->refMapStruct[0]; // 'reference' field of WeakReference
        word_t *refObject = object->fields[fieldOffset];
        if (Heap_IsWordInHeap(heap, refObject)) {
            ObjectMeta *objectMeta = Bytemap_Get(bytemap, refObject);
            if (!ObjectMeta_IsMarked(objectMeta)) {
                object->fields[fieldOffset] = NULL;
            }
        }
    }
}