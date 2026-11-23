#ifndef IMMIX_COMMIX_OBJECT_H
#define IMMIX_COMMIX_OBJECT_H

#include "immix_commix/headers/ObjectHeader.h"
#include "immix_commix/datastructures/Bytemap.h"
#include "immix_commix/CollectorState.h"

word_t *Object_LastWord(Object *object);
Object *Object_GetUnmarkedObject(Heap *heap, word_t *address);
void Object_Mark(Heap *heap, Object *object, ObjectMeta *objectMeta);
// Marks the block and the lines the object occupies, unless it is large.
void Object_MarkLines(Heap *heap, Object *object);
// The young object (allocated since the last collection, or marked by the
// current one) that contains `word`, if any.
Object *Object_GetYoungObject(Heap *heap, word_t *word);

#endif // IMMIX_COMMIX_OBJECT_H
