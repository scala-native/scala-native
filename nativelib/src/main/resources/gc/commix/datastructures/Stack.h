#ifndef IMMIX_STACK_H
#define IMMIX_STACK_H

#include "../GCTypes.h"
#include "../headers/ObjectHeader.h"
#include <pthread.h>

#define INITIAL_STACK_SIZE (256 * 1024)

typedef void *Stack_Type;

typedef struct {
    Stack_Type *bottom;
    size_t nb_words;
    uint32_t current;
} Stack;

void Stack_Init(Stack *stack, size_t size);

void Stack_Push(Stack *stack, Stack_Type word);

Stack_Type Stack_Pop(Stack *stack);

bool Stack_IsEmpty(Stack *stack);

void Stack_Clear(Stack *stack);

#endif // IMMIX_STACK_H
