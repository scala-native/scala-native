#ifndef MEMORYMAP_H
#define MEMORYMAP_H

#include "GCTypes.h"
#include <stddef.h>
#include <stdbool.h>

word_t *memoryMap(size_t memorySize);
bool memoryCommit(void *ref, size_t memorySize);

word_t *memoryMapPrealloc(size_t memorySize, size_t doPrealloc);

void memoryUnmap(void *address, size_t memorySize);

#endif // MEMORYMAP_H
