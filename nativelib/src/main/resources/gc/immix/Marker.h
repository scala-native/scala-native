#ifndef IMMIX_MARKER_H
#define IMMIX_MARKER_H

#include "Heap.h"
#include "datastructures/Stack.h"

void Mark_markRoots(Heap *heap, Stack *stack);
bool Marker_overflowMark(Heap* heap, Stack* stack, Object* object);

#endif // IMMIX_MARKER_H
