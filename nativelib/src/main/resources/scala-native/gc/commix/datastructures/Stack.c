#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include "Stack.h"
#include "../Log.h"

void Stack_doubleSize(Stack *stack);

void Stack_Init(Stack *stack, size_t size) {
    assert(size % sizeof(Stack_Type) == 0);
    stack->current = 0;
    stack->bottom = malloc(size);
    stack->nb_words = size / sizeof(Stack_Type);
}

INLINE
void Stack_Push(Stack *stack, Stack_Type word) {
    if (stack->current >= stack->nb_words) {
        Stack_doubleSize(stack);
    }
    stack->bottom[stack->current++] = word;
}

INLINE
Stack_Type Stack_Pop(Stack *stack) {
    assert(stack->current > 0);
    return stack->bottom[--stack->current];
}

INLINE
bool Stack_IsEmpty(Stack *stack) { return stack->current == 0; }

NOINLINE
void Stack_doubleSize(Stack *stack) {
    size_t nb_words = stack->nb_words * 2;
    stack->nb_words = nb_words;
    stack->bottom = realloc(stack->bottom, nb_words * sizeof(Stack_Type));
}
