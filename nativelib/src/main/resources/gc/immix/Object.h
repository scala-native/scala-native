#ifndef IMMIX_OBJECT_H
#define IMMIX_OBJECT_H

#include "headers/ObjectHeader.h"
#include "datastructures/Bytemap.h"
#include "LargeAllocator.h"
#include "Heap.h"

Object *Object_NextLargeObject(Object *objectHeader);
word_t *Object_LastWord(Object *objectHeader);
Object *Object_GetUnmarkedObject(Heap *heap, word_t *address);
Object *Object_GetLargeUnmarkedObject(LargeAllocator *largeAllocator, word_t *address);
void Object_Mark(Heap *heap, Object *objectHeader);
size_t Object_ChunkSize(Object *objectHeader);

#endif // IMMIX_OBJECT_H
