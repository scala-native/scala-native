#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include "Stack.h"
#include "../Log.h"

Stack *Stack_Alloc(size_t size) {
    assert(size % sizeof(Stack_Type) == 0);
    Stack *stack = malloc(sizeof(Stack));
    stack->current = 0;
    stack->bottom = malloc(size);
    stack->nb_words = size / sizeof(Stack_Type);
    return stack;
}

bool Stack_Push(Stack *stack, Stack_Type word) {
    if (stack->current < stack->nb_words) {
        stack->bottom[stack->current++] = word;
        return false;
    } else {
#ifdef PRINT_STACK_OVERFLOW
        printf("Overflow !\n");
#endif

        return true;
    }
}

Stack_Type Stack_Pop(Stack *stack) {
    assert(stack->current > 0);
    return stack->bottom[--stack->current];
}

bool Stack_IsEmpty(Stack *stack) { return stack->current == 0; }

void Stack_DoubleSize(Stack *stack) {
    assert(stack->current == 0);
    size_t nb_words = stack->nb_words * 2;
    stack->nb_words = nb_words;
    free(stack->bottom);
    stack->bottom = malloc(nb_words * sizeof(Stack_Type));
}