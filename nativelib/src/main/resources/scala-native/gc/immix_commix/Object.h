#ifndef IMMIX_COMMIX_OBJECT_H
#define IMMIX_COMMIX_OBJECT_H

#include "immix_commix/headers/ObjectHeader.h"
#include "immix_commix/datastructures/Bytemap.h"
#include "immix_commix/CollectorState.h"

word_t *Object_LastWord(Object *object);
Object *Object_GetUnmarkedObject(Heap *heap, word_t *address);
void Object_Mark(Heap *heap, Object *object, ObjectMeta *objectMeta);

#endif // IMMIX_COMMIX_OBJECT_H
