#ifndef IMMIX_OBJECT_H
#define IMMIX_OBJECT_H

#include "headers/ObjectHeader.h"
#include "LargeAllocator.h"

Object *Object_NextLargeObject(Object *objectHeader);
Object *Object_NextObject(Object *objectHeader);
Object *Object_GetObject(word_t *address);
Object *Object_GetLargeObject(LargeAllocator *largeAllocator, word_t *address);
void Object_Mark(Object *objectHeader);
size_t Object_ChunkSize(Object *objectHeader);

#endif // IMMIX_OBJECT_H
