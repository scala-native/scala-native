#include "State.h"

Heap *heap = NULL;
Stack *stack = NULL;

// For stackoverflow handling
bool overflow = false;
word_t *currentOverflowAddress = NULL;