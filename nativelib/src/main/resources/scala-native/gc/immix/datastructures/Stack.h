#ifndef IMMIX_STACK_H
#define IMMIX_STACK_H

#include "../GCTypes.h"
#include "../headers/ObjectHeader.h"

#define INITIAL_STACK_SIZE (256 * 1024)

typedef Object *Stack_Type;

typedef struct {
    Stack_Type *bottom;
    size_t nb_words;
    int current;
} Stack;

void Stack_Init(Stack *stack, size_t size);

void Stack_Push(Stack *stack, Stack_Type word);

Stack_Type Stack_Pop(Stack *stack);

bool Stack_IsEmpty(Stack *stack);

#endif // IMMIX_STACK_H
