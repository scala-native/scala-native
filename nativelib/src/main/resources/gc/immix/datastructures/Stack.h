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

Stack* stack_alloc(size_t size);

bool stack_push(Stack* stack, Stack_Type word);

Stack_Type stack_pop(Stack* stack);

bool stack_isEmpty(Stack* stack);

void stack_doubleSize(Stack* stack);

#endif //IMMIX_STACK_H
