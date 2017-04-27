#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include "Stack.h"
#include "../Log.h"

Stack* stack_alloc(size_t size) {
    assert(size % sizeof(Stack_Type) == 0);
    Stack* stack = malloc(sizeof(Stack));
    stack->current = 0;
    stack->bottom = malloc(size);
    stack->nb_words = size / sizeof(Stack_Type);
    return stack;
}

bool stack_push(Stack* stack, Stack_Type word) {
    if(stack->current < stack->nb_words) {
        stack->bottom[stack->current++] = word;
        return false;
    } else {
#ifdef PRINT_STACK_OVERFLOW
        printf("Overflow !\n");
#endif

        return true;
    }
}

Stack_Type stack_pop(Stack* stack) {
    assert(stack->current > 0);
    return stack->bottom[--stack->current];
}

bool stack_isEmpty(Stack* stack) {
    return stack->current == 0;
}


void stack_doubleSize(Stack* stack) {
    assert(stack->current == 0);
    size_t nb_words = stack->nb_words * 2;
    stack->nb_words = nb_words;
    free(stack->bottom);
    stack->bottom = malloc(nb_words * sizeof(Stack_Type));
}