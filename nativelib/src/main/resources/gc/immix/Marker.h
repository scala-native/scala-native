#ifndef IMMIX_MARKER_H
#define IMMIX_MARKER_H

#include "Heap.h"
#include "datastructures/Stack.h"

void Marker_markRoots(Heap *heap, Stack *stack);
void Marker_mark(Heap *heap, Stack *stack);

#endif // IMMIX_MARKER_H
