#ifndef IMMIX_OBJECT_H
#define IMMIX_OBJECT_H

#include "headers/ObjectHeader.h"
#include "LargeAllocator.h"

ObjectHeader* Object_nextLargeObject(ObjectHeader *objectHeader);
ObjectHeader* Object_nextObject(ObjectHeader *objectHeader);
ObjectHeader* Object_getObject(word_t *address);
ObjectHeader* Object_getLargeObject(LargeAllocator *largeAllocator, word_t *address);
void Object_mark(ObjectHeader *objectHeader);
size_t Object_chunkSize(ObjectHeader *objectHeader);

#endif //IMMIX_OBJECT_H
