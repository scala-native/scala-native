#ifndef IMMIX_OBJECT_H
#define IMMIX_OBJECT_H

#include "headers/ObjectHeader.h"
#include "LargeAllocator.h"

ObjectHeader* objectNextLargeObject(ObjectHeader* objectHeader);
ObjectHeader* object_nextObject(ObjectHeader *);
ObjectHeader* object_getObject(word_t*);
ObjectHeader* object_getLargeObject(LargeAllocator*, word_t*);
void object_mark(ObjectHeader* objectHeader);
size_t object_chunkSize(ObjectHeader*);

#endif //IMMIX_OBJECT_H
