#ifndef IMMIX_STACK_H
#define IMMIX_STACK_H

#include "../GCTypes.h"
#include "../headers/ObjectHeader.h"

#define PRINT_STACK_OVERFLOW

#define INITIAL_STACK_SIZE (512*1024*1024)

typedef ObjectHeader* Stack_Type;

typedef struct {
    Stack_Type* bottom;
    size_t nb_words;
    int current;
} Stack;

Stack* Stack_alloc(size_t size);

bool Stack_push(Stack *stack, Stack_Type word);

Stack_Type Stack_pop(Stack *stack);

bool Stack_isEmpty(Stack *stack);

void Stack_doubleSize(Stack *stack);

#endif //IMMIX_STACK_H
