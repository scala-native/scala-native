#ifndef IMMIX_OBJECT_H
#define IMMIX_OBJECT_H

#include "headers/ObjectHeader.h"
#include "LargeAllocator.h"

Object *Object_nextLargeObject(Object *objectHeader);
Object *Object_nextObject(Object *objectHeader);
Object *Object_getObject(word_t *address);
Object *Object_getLargeObject(LargeAllocator *largeAllocator,
                                    word_t *address);
void Object_mark(Object *objectHeader);
size_t Object_chunkSize(Object *objectHeader);

#endif // IMMIX_OBJECT_H
