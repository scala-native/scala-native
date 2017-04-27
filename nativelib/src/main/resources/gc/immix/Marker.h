#ifndef IMMIX_MARKER_H
#define IMMIX_MARKER_H

#include "Heap.h"
#include "datastructures/Stack.h"

void marker_mark(Heap* heap, Stack*);
void mark_roots(Heap* heap, Stack* stack);

#endif //IMMIX_MARKER_H
