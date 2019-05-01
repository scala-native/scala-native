#ifndef IMMIX_OBJECT_H
#define IMMIX_OBJECT_H

#include "headers/ObjectHeader.h"
#include "datastructures/Bytemap.h"
#include "LargeAllocator.h"
#include "Heap.h"

word_t *Object_LastWord(Object *object);
Object *Object_GetUnmarkedObject(Heap *heap, word_t *address, bool collectingOld);
void Object_Mark(Heap *heap, Object *object, ObjectMeta *objectMeta, bool collectingOld);

#endif // IMMIX_OBJECT_H
