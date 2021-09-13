#ifndef WEAK_REF_STACK_H
#define WEAK_REF_STACK_H
#include "Object.h"
#include "Heap.h"

void WeakRefStack_Init(size_t size);
void WeakRefStack_Push(Object *object);
void WeakRefStack_Nullify(Heap *heap);
void WeakRefStack_CallHandlers(Heap *heap);

#endif // WEAK_REF_STACK_H